package com.hmdp;

import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLOutput;

@SpringBootTest
class HmDianPingApplicationTests {
    public static void main(String[] args) {
        //获取当前时间戳
        long currentTimeMillis = System.currentTimeMillis();
        System.out.println("当前时间戳：" + currentTimeMillis);
    }


}
