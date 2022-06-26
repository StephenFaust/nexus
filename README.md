# Nexus-RPC

## 介绍
Nexus是一个简单的rpc中间件，基本实现rpc的主要功能，目前须结合spring使用，其中使用Netty为通信框架，使用kryo作为默认序列化协议，通过spi来选配序列化协议、 负载均衡器、注册中心；
##

### 1、为什么要写这个RPC
+ 最近在学习nio的知识，个人学习使用，在netty学习过程中，算是一个学习成果；
+ 学习一些rpc设计上的思想，想仿造一个dubbo；
+ 学会造轮子；
### 2、为什么叫Nexus
 准哥真男人！！！！  

### 3、项目架构
- 【annotation】
 客户端/服务端 服务注解；
- 【cluster】
 客户端负载均衡和服务治理（暂无）
- 【config】
 配置类(springboot自动装配)
- 【discovery】
 服务发现
- 【exception】
 异常类
- 【interceptor】
 拦截器
- 【invocation】
 动态代理
- 【io】
 io通信层
- 【property】
 配置属性
- 【registry】
 注册中心
- 【serialize】
 序列化
- 【spi】
 spi层
- 【spring】
 spring监听器和BeaPostProcessor
 
 ##### 服务端
 
 服务端端使用netty最为常用的多主从Reactor模型，如下
```java
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();      
```
bossGroup(即主Reactor)负责接受请求，workerGroup(即从Reactor)负责io读写,EventLoopGroup为netty对线程池的封装，bossGroup在只监听一个端口号的时候，默认开启一个线程，workerGroup主要做的是io读写，即为i0密集型，默认为核心cpu数*2;


业务线程池，如下
```java
        EventExecutorGroup businessGroup = new DefaultEventExecutorGroup(maxWorkThreadCount, new ThreadFactory() {
            @Override
            public Thread newThread(@NotNull Runnable r) {
                Thread thread = new Thread(r);
                thread.setName("work-th-" + Thread.currentThread().getId());
                return thread;
            }
        });
```
使用netty本身的EventExecutorGroup（官方推荐），而不是jdk的ThreadPoolExecutor,EventExecutorGroup是与chanel绑定，因此不会发生锁竞争，最大线程数可以通过配置文件设置；


 ##### 客户端

客户端为netty客户端的标准实现

```java
    private final EventLoopGroup group = new NioEventLoopGroup();
    private final Bootstrap strap = new Bootstrap();
``` 

 ##### 客户端连接池

```java
    new FixedChannelPool(strap.remoteAddress(key), new DefaultChannelPoolHandler(), maxConnection);
```
nexus使用netty的FixedChannelPool，最大连接数通过配置文件配置，此连接池中每个连接（channel）和大多数db连接池类似，为线程独占，在nio中，这种连接池的性能其实并不优秀，因为在netty中writeAndFlush方法是一个异步方法，即调用之后会立即返回，因此在nio中每个channel是可以得到复用，目前的思路是每一个channel绑定一个回调事件的队列，自定义一个编码器，在每一次发送数据前，对数据包进行encode，加入一个序列号在里面，收到数据时，通过自定义解码器，decode出序列号，通过序列号找到回调事件，处理业务逻辑，这个一个channel可以被多个线程使用，提高了吞吐量，后续会根据这个思路，尝试去写一个可以复用的连接池；
   
 ##### 编解码器

```java
    .addLast("encode", new LengthFieldPrepender(8))
    .addLast("decode", new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0,   
```
使用LengthFieldPrepender和LengthFieldBasedFrameDecoder解决Tcp传输中粘包和半包问题，这种方式会在数据包加一个8个字节的数据，用来表示数据包的长度；

 ##### 服务注册发现

  nexus默认并仅仅支持使用redis作为服务注册中心，后续会加入zk,nacos,consul;
  
 ##### 动态代理

使用jdk动态代理生成代理对象，供服务端使用
 ```java
    public <T> T getProxyInstance(Class<T> clazz) {
            return (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, new InvocationHandler() {
                @Override
                public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                    String serviceName = clazz.getName();
                    final List<MateInfo> serviceInfos = serviceDiscovery.listServices(serviceName);
                    logger.info("Rpc server instance list: {}", serviceInfos);
                    if (CollectionUtils.isEmpty(serviceInfos)) {
                        throw new RpcException("No rpc servers found.");
                    }
                    final MateInfo mateInfo = loadBalancer.getService(serviceInfos);
                    final RpcRequest rpcRequest = new RpcRequest();
                    rpcRequest.setServiceName(serviceName);
                    rpcRequest.setMethod(method.getName());
                    rpcRequest.setParameterTypes(method.getParameterTypes());
                    rpcRequest.setParameters(args);
                    // 编码请求消息， TODO: 这里可以配置多种编码方式
                    byte[] data = serializer.serialize(rpcRequest);
                    // 发送消息
                    byte[] byteResponse = rpcClient.sendMessage(data, mateInfo);
                    // 解码响应消息
                    final RpcResponse rpcResponse = serializer.deserialize(byteResponse, RpcResponse.class);
                    if (rpcResponse.getException() != null) {
                        throw rpcResponse.getException();
                    }
                    // 解析返回结果进行处理
                    return rpcResponse.getData();
                }
            });
        }
```  
     
 ##### 序列化协议
nexus有两种序列化协议可以选择，一种时jdk自带的，一种是kryo,默认使用kryo，可以通过配置文件修改，由于是使用spi进行加载，也可实现自己的协议，通过实现com.mao.nexus.serialize.Serializer接口，并在 META-INF\nexus\internal\com.mao.nexus.serialize.Serializer文件放入自己的实现类的全限定名和协议名称；

 ##### spi
  - spi介绍
  
    SPI全称为Service Provider Interface，对应中文为服务发现机制。 SPI类似一种可插拔机制，首先需要定义一个接口或一个约定，然后不同的场景可以对其进行实现，调用方在使用的时候无需过多关注具体的实现细节。 在Java中，SPI体现了面向接口编程的思想，满足开闭设计原则。
    
  - nexus中spi
  
    nexus的spi使用了和dubbo的相同的实现，拿负载均衡器举例，nexus默认使用负载均衡策略为轮询，在ExtensionLoader通过配置文件clusterProperties拿到实例对象，如下：
    
```java
     ExtensionLoader<LoadBalancer> extensionLoader = ExtensionLoader.getExtensionLoader(LoadBalancer.class);
     return extensionLoader.getExtension(clusterProperties.getLoadBalance());
``` 
   此时如果想实现随机的策略，只需要实现LoadBalancer接口，重写getService方法,如下
    
```java
public class RandomLoadBalancer implements LoadBalancer {

    private static final Random counter = new Random();

    @Override
    public MateInfo getService(List<MateInfo> services) {
        return services.get(counter.nextInt(services.size()));
    }
}
```

   并在META-INF\nexus\internal\com.mao.nexus.cluster.loadbalance.LoadBalancer文件中该类的全限定名和协议名称,如下：
```java
    random=com.mao.nexus.cluster.loadbalance.RandomLoadBalancer
    train=com.mao.nexus.cluster.loadbalance.TrainLoadBalancer
```
   接着在配置文件中指定协议名称，如下：
```yaml
nexus:
  cluster:
    load-balance: random
```
   即实现随机策略；
  
 ##### spring
  - 在spring.factories中加入配置类全限定名，进行自动装配；
  - 使用ApplicationListener中onApplicationEvent方法启用netty服务端并扫描容器中是否有@NexusService注解的bean，有就注册到注册中心，onApplicationEvent方法会在spring对容器刷新时(refresh)调用;
  - 使用BeanPostProcessor的后置处理方法对客户端所有bean中含有@NexusClient属性，进行注入代理对象；
  
 ##### 案例
 案例在nexus-example中；