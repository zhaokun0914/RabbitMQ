package com.example.rabbitmq.work_queues._02_consumer;

import com.example.rabbitmq.common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 这是一个工作线程，相当于之前的消费者
 *
 * @author Kavin
 * @date 2021-9-13 17:31:43
 */
public class Worker01 {

    public static final Logger LOGGER = LoggerFactory.getLogger(Worker01.class);

    /**
     * 接收消息
     */
    public static void main(String[] args) throws IOException {
        LOGGER.info("==> C3 工作线程等待接收消息");

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

        // 3、基本消费
        /*
         * queue - queue的名称
         * autoAck - 如果服务器应该考虑消息一旦发送就确认为真； 如果服务器应该期待明确的确认，则为 false
         * deliverCallback(consumerTag, message) - 交付消息时的回调
         * cancelCallback(consumerTag) - 消费者被取消时的回调
         * 返回：服务器生成的consumerTag
         */
        channel.basicConsume(RabbitUtils.WORK_QUEUE_NAME, true, (consumerTag, message) -> {
            // 接收消息时的回调
            LOGGER.info("worke queues 接受到的消息：" + new String(message.getBody()));
        }, (consumerTag) -> {
            // 消息被取消时执行
            LOGGER.info("worke queues 消息被取消时执行:" + new String(consumerTag.getBytes(StandardCharsets.UTF_8)));
        });
    }

}
