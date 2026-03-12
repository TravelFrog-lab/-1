package org.example.springboot.entity;

public class TestLombokTest {
    public static void main(String[] args) {
        TestLombok test = new TestLombok();
        test.setUsername("张三");
        System.out.println("用户名是：" + test.getUsername());
    }
}