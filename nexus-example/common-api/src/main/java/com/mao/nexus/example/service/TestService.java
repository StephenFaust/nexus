package com.mao.nexus.example.service;


import com.mao.nexus.example.service.dto.User;

/**
 * @author ：huangjinmao
 * @date ：2022/6/15 17:29
 */
public interface TestService {

    String doTest(String var);

    User getUser(String name);
}
