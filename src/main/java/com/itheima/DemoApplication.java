package com.itheima;

import com.itheima.mqtt.client.EmqClient;
import com.itheima.mqtt.enums.QosEnum;
import com.itheima.mqtt.properties.MqttProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;
import java.util.concurrent.TimeUnit;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Autowired
    private EmqClient emqClient;

    @Autowired
    private MqttProperties properties;

//    @PostConstruct
    public void init() {
        //连接服务器
        emqClient.connect(properties.getUsername(), properties.getPassword());
        //订阅一个主题
        emqClient.subscribe("testtopic/#", QosEnum.QoS2);
        //开启一个新的线程，每隔五秒去向testtopic/123发送消息
        new Thread(() ->{
            while (true) {
                emqClient.publish("testtopic/123", "我是大帅哥", QosEnum.QoS2, true);

                try {
                    TimeUnit.SECONDS.sleep(5);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
