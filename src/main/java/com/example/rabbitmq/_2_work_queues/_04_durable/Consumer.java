package com.example.rabbitmq._2_work_queues._04_durable;

import com.example.rabbitmq._0_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * RabbitMQ持久化 消费者
 *     创建一个消费者，消费持久化队列中的消息
 *
 * @author Kavin
 * @date 2021-09-14 20:02:32
 */
public class Consumer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer.class);

    /**
     * 接收消息
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("==> C1 工作线程等待接收消息");

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
        boolean durable = true;
        channel.queueDeclare(RabbitUtils.ACK_QUEUE, durable, false, false, null);

        // 3、基本消费
        /*
         * queue - queue的名称
         * autoAck - 自动应答ture，手动应答false
         * deliverCallback(consumerTag, message) - 交付消息时的回调
         * cancelCallback(consumerTag) - 消费者被取消时的回调
         * 返回：服务器生成的consumerTag
         */
        channel.basicConsume(RabbitUtils.ACK_QUEUE, true, (consumerTag, delivery) -> {
            // 接收消息时的回调
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("work queues 接受到的消息：" + msg);
        }, (consumerTag) -> {
            // 消息被取消时执行
            LOGGER.info("work queues 消息被取消时执行:" + new String(consumerTag.getBytes(StandardCharsets.UTF_8)));
        });
    }

}
