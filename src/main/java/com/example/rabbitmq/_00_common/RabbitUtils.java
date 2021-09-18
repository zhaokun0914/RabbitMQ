package com.example.rabbitmq._00_common;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * RabbitMQ 工具类
 *
 * @author Kavin
 * @date 2021-9-13 17:18:51
 */
public class RabbitUtils {


    /**
     * hello world 队列名称
     */
    public static final String QUEUE_NAME = "hello";

    /**
     * work queues 队列名称
     */
    public static final String WORK_QUEUE_NAME = "work_queue_hello";

    /**
     * work queues 队列名称
     */
    public static final String ACK_QUEUE = "ack_queue";

    /**
     * publish subscribe 队列名称
     */
    public static final String PUBLISH_SUBSCRIBE = "publish_subscribe";

    public static final String NORMAL_EXCHANGE = "normal_exchange";
    public static final String DEAD_EXCHANGE = "dead_exchange";

    public static final String NORMAL_QUEUE = "normal_queue";
    public static final String DEAD_QUEUE = "dead_queue";

    public static final String NORMAL_ROUTING_KEY = "normal_key";
    public static final String DEAD_ROUTING_KEY = "dead_key";



    /**
     * IP地址
     */
    public static final String HOST = "127.0.0.1";
    /**
     * 端口号
     */
    public static final int PORT = 5672;
    /**
     * 用户名
     */
    public static final String USER_NAME = "admin";
    /**
     * 密码
     */
    public static final String PASS_WORD = "admin";

    public static Channel createChannel() {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        // RabbitMQ服务器IP
        connectionFactory.setHost(HOST);
        // 端口
        connectionFactory.setPort(PORT);
        // 用户名
        connectionFactory.setUsername(USER_NAME);
        // 密码
        connectionFactory.setPassword(PASS_WORD);

        Channel channel = null;
        try {
            // 获取连接
            Connection connection = connectionFactory.newConnection();
            // 获取信道
            channel = connection.createChannel();
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }

        return channel;

    }

}
