package com.example.rabbitmq._02_work_queues._01_demo;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * work queues 模式
 * 消息轮询，启动三个消费者，监听同一个队列，则MQ会将消息依次送给每个消费者
 *
 * @author Kavin
 * @date 2021-9-13 18:24:13
 */
public class Task01 {

    public static final Logger LOGGER = LoggerFactory.getLogger(Task01.class);

    public static void main(String[] args) throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();

        // 2、队列声明
        /*
         * queue - queue的名称
         * durable - 如果我们声明一个持久队列，则为 true（该队列将在服务器重启后继续存在）
         * exclusive - 如果我们声明独占队列（仅限于此连接），则为 true
         * autoDelete - 如果我们声明一个自动删除队列，则为 true（服务器将在不再使用时将其删除）
         * arguments - 队列的其他属性（构造参数）
         */
        channel.queueDeclare(RabbitUtils.WORK_QUEUE_NAME, false, false, false, null);


        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();

            // 3、发送消息
            /*
             * exchange - 将消息发布到的交换机
             * routingKey - 路由key
             * props - 消息的其他属性 - 路由标头等
             * body - 消息正文
             */
            channel.basicPublish("", RabbitUtils.WORK_QUEUE_NAME, null, message.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("<== 消息发送完成：{}", message);
        }
    }

}
