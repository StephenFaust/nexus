# Nexus

## 介绍

Nexus是一个简单的rpc框架，基本实现rpc的主要功能，目前须结合spring使用，其中使用Netty为通信框架，使用kryo作为默认序列化协议，通过spi来选配序列化协议、
负载均衡器、注册中心；

##

### 使用教程

- 将项目拉到本地，并安装到本地maven仓库；
- 创建一个空的springboot项目；
- pom引入依赖；

```xml

<dependency>
  <groupId>com.mao.nexus</groupId>
  <artifactId>nexus-spring-boot-start</artifactId>
  <version>1.0.0</version>
</dependency>
```

- 在yml配置相关内容

```yaml
nexus:
  service-name: server-example
  port: 1234
  registry:
    protocol: consul
  cluster:
    load-balance: random
  max-connection: 20
```

- 在服务端编写服务；

```java

@NexusService
public class TestServiceImpl implements TestService {

  @Value("${nexus.port}")
  private int port;

  private final List<User> userList = new ArrayList<>();

  private Random random = new Random();

  @PostConstruct
  public void init() {

    for (int i = 0; i < 10; i++) {
      User user = new User("Test" + i, i, random.nextInt(2), port);
      userList.add(user);
    }
  }

  @Override
  public String doTest(String var) {
    return String.format("收到了，%s,我的回答是：够了！~我是%d", var, port);
  }

  @Override
  public User getUser(String name) {
    return userList.stream().filter(user -> user.getName().equals(name)).findAny().orElse(null);
  }
}

```

- 客户端controller；

```java

@RestController
public class RpcController {

  @NexusClient(serviceName = "server-example")
  private TestService testService;


  @GetMapping("/test/{par}")
  public String test2(@PathVariable String par) throws IOException {
    Random random = new Random();
    long l = random.nextLong();
    return testService.doTest(String.valueOf(l));

  }

  @GetMapping("/test3/{name}")
  public User test3(@PathVariable String name) {
    return testService.getUser(name);
  }

}

```

- 启动服务端和客户端，并在浏览器调用服务接口
  ![img.png](nexus-example/img/img.png)
  ![](nexus-example/img/img_1.png)
- 调用成功！！！

##### 案例

更多详细案例在nexus-example中；

