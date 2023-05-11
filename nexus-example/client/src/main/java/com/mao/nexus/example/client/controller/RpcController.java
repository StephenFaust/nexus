package com.mao.nexus.example.client.controller;


import com.mao.nexus.annotation.NexusClient;
import com.mao.nexus.example.service.TestService;
import com.mao.nexus.example.service.dto.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Random;

/**
 * @author ：StephenMao
 * @date ：2022/6/15 14:13
 */

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
