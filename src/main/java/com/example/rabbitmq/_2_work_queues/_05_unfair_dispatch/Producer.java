package com.example.rabbitmq._2_work_queues._05_unfair_dispatch;

import com.example.rabbitmq._0_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * RabbitMQ 不公平分发 生产者
 *
 * @author Kavin
 * @date 2021-09-15 13:17:46
 */
public class Producer {
    public static final Logger LOGGER = LoggerFactory.getLogger(com.example.rabbitmq._2_work_queues._04_durable.Producer.class);

    public static void main(String[] args) throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();


        // 2、声明[持久化队列]
        /*
         * queue - queue的名称
         * durable - 如果我们声明一个持久队列，则为 true（该队列将在服务器重启后继续存在）
         * exclusive - 如果我们声明独占队列（仅限于此连接），则为 true
         * autoDelete - 如果我们声明一个自动删除队列，则为 true（服务器将在不再使用时将其删除）
         * arguments - 队列的其他属性（构造参数）
         */
        // 队列持久化
        boolean durable = true;
        channel.queueDeclare(RabbitUtils.ACK_QUEUE, durable, false, false, null);


        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();

            // 3、发送消息，同时声明[消息持久化]
            /*
             * exchange - 将消息发布到的交换机
             * routingKey - 路由key
             * props - 消息的其他属性 - 路由标头等
             *   消息持久化：MessageProperties.PERSISTENT_TEXT_PLAIN
             * body - 消息正文
             */
            channel.basicPublish("", RabbitUtils.ACK_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("<== 消息发送完成：{}", message);
        }
    }

}
