package com.example.rabbitmq._08_priority;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 消费者，接收消息
 *
 * @author Kavin
 * @date 2021-9-13 15:48:27
 */
public class Consumer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    // 队列名称
    public static final String QUEUE_NAME = "hello";

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 5672;
    public static final String USER_NAME = "admin";
    public static final String PASS_WORD = "admin";


    public static void main(String[] args) throws Exception {
        // 创建连接工厂
        ConnectionFactory connectionFactory = new ConnectionFactory();
        // 工厂IP，连接RabbitMQ的队列
        connectionFactory.setHost(HOST);
        // 端口
        connectionFactory.setPort(PORT);
        // 用户名
        connectionFactory.setUsername(USER_NAME);
        // 密码
        connectionFactory.setPassword(PASS_WORD);

        // 创建Connection
        Connection connection = connectionFactory.newConnection();
        // 获取Channel
        Channel channel = connection.createChannel();

        /*
         * queue - queue的名称
         * autoAck - 自动应答ture，手动应答false
         * deliverCallback(consumerTag, message) - 交付消息时的回调
         * cancelCallback(consumerTag) - 消费者被取消时的回调
         * 返回：服务器生成的consumerTag
         */
        // 基本消费
        String consumerTags = channel.basicConsume(QUEUE_NAME, true, (consumerTag, delivery) -> {
            // 接收消息时的回调
            LOGGER.info(" [√] Sent '" + new String(delivery.getBody()) + "'");
        }, (consumerTag) -> {
            // 消息被取消时执行
            LOGGER.info("消息被取消时执行:" + new String(consumerTag.getBytes(StandardCharsets.UTF_8)));
        });

    }

}
