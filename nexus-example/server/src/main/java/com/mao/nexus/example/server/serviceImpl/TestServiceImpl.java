package com.mao.nexus.example.server.serviceImpl;

import com.mao.nexus.annotation.NexusService;
import com.mao.nexus.example.service.TestService;
import com.mao.nexus.example.service.dto.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.SecurityProperties;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


/**
 * @author ：StephenMao
 * @date ：2022/6/15 14:04
 */
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
