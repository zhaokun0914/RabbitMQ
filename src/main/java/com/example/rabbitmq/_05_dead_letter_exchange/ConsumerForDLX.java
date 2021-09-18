package com.example.rabbitmq._05_dead_letter_exchange;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列 消费者
 *
 * @author Kavin
 * @date 2021-09-18 13:16:02
 */
public class ConsumerForDLX {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerForDLX.class);

    public static void main(String[] args) throws Exception {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();


        // 2、声明死信交换机
        channel.exchangeDeclare(RabbitUtils.DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);


        // 3、声明死信队列
        channel.queueDeclare(RabbitUtils.DEAD_QUEUE, false, false, false, null);
        // 3、死信队列绑定死信交换机
        channel.queueBind(RabbitUtils.DEAD_QUEUE, RabbitUtils.DEAD_EXCHANGE, RabbitUtils.DEAD_ROUTING_KEY);


        // 4、接收消息
        channel.basicConsume(RabbitUtils.DEAD_QUEUE, true, (String consumerTag, Delivery delivery) -> {
            LOGGER.info("==> 死信队列接收到消息：{}", new String(delivery.getBody(), StandardCharsets.UTF_8));
        }, delivery -> {

        });
    }

}
