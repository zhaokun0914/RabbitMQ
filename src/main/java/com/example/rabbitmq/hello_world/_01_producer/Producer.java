package com.example.rabbitmq.hello_world._01_producer;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 生产者，发送消息
 *
 * @author Kavin
 * @date 2021-9-13 15:48:27
 */
public class Producer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    
    // 队列名称
    public static final String QUEUE_NAME = "hello";

    public static final String HOST = "127.0.0.1";
    public static final int PORT = 5672;
    public static final String USER_NAME = "admin";
    public static final String PASS_WORD = "admin";

    // 发送消息
    public static void main(String[] args) {
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

        try (Connection connection = connectionFactory.newConnection();// 创建Connection
             // 获取Channel
             Channel channel = connection.createChannel()) {

            /*
             * queue - queue的名称
             * durable - 如果我们声明一个持久队列，则为 true（该队列将在服务器重启后继续存在）
             * exclusive - 如果我们声明独占队列（仅限于此连接），则为 true
             * autoDelete - 如果我们声明一个自动删除队列，则为 true（服务器将在不再使用时将其删除）
             * arguments - 队列的其他属性（构造参数）
             */
            // 队列声明
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            // 准备发送消息
            String message = "Hello World!";

            /*
             * exchange - 将消息发布到的交换机
             * routingKey - 路由key
             * props - 消息的其他属性 - 路由标头等
             * body - 消息正文
             */
            // 基本发布
            channel.basicPublish("", QUEUE_NAME, null, message.getBytes());

            LOGGER.info(" [x] Sent '" + message + "'");
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
    }
}
