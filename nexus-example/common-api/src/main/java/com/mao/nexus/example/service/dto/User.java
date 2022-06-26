package com.mao.nexus.example.service.dto;

/**
 * @author StephenMao
 * @date 2022/6/26 17:46
 */
public class User {
    private String name;
    private int age;
    private int sex;
    private int tag;



    public User(String name, int age, int sex, int tag) {
        this.name = name;
        this.age = age;
        this.sex = sex;
        this.tag = tag;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public int getSex() {
        return sex;
    }

    public void setSex(int sex) {
        this.sex = sex;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getTag() {
        return tag;
    }

    public void setTag(int tag) {
        this.tag = tag;
    }
}
