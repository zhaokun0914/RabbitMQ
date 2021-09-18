package com.example.rabbitmq._03_publish_subscribe._02_exchange.topic;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * Topic 交换机
 *     要求：它必须是一个单词列表，以点号分隔开。
 *          这些单词可以是任意单词，比如说："stock.usd.nyse", "nyse.vmw", "quick.orange.rabbit"这种类型的。
 *          这个单词列表最多不能超过 255 个字节。
 *     细节：
 *         * (星号)可以代替一个单词
 *         # (井号)可以替代零个或多个单词
 *
 *     2021-09-18 10:59:55,375 [pool-2-thread-4]  INFO Consumer1:41 - ==> 发送的消息配到了 -> *.orange.* 队列，消息内容为:被队列Q1、Q2接收到
 *     2021-09-18 11:00:00,650 [pool-2-thread-5]  INFO Consumer1:41 - ==> 发送的消息配到了 -> *.orange.* 队列，消息内容为:被队列Q1、Q2接收到
 *     2021-09-18 11:00:06,192 [pool-2-thread-6]  INFO Consumer1:41 - ==> 发送的消息配到了 -> *.orange.* 队列，消息内容为:被队列Q1接收到
 *
 * @author Kavin
 * @date 2021-09-18 10:18:14
 */
public class Consumer1 {
    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer1.class);

    public static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] args) throws Exception {

        Channel channel = RabbitUtils.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        String queueName = channel.queueDeclare().getQueue();

        channel.queueBind(queueName,EXCHANGE_NAME,"*.orange.*");

        channel.basicConsume(queueName,true, (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("==> 发送的消息配到了 -> *.orange.* 队列，消息内容为:{}", message);
        }, consumerTag -> {
        });
    }
}
