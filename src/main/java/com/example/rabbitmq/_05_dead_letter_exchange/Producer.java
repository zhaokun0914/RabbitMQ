package com.example.rabbitmq._05_dead_letter_exchange;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 死信队列 demo 生产者
 *     死信的来源
 *         1、消息的TTL超时
 *         2、队列达到最大长度
 *         3、消息被 nack 或 reject，并且 requeue=false
 *
 * @author Kavin
 * @date 2021-09-18 13:16:02
 */
public class Producer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    public static final int COUNT = 20;

    public static void main(String[] args) throws Exception {
        // 消息TTL过期
        // TTLTest();

        // 队列达到最大长度，后进入的消息会把之前进入的消息挤到死信队列中去
        // queueFull();

        // 消息被 nack 或 reject，并且 requeue=false
        messageNackOrReject();

    }

    private static void messageNackOrReject() throws IOException {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();

        // 2、发送消息
        for (int i = 1; i <= COUNT; i++) {
            channel.basicPublish(RabbitUtils.NORMAL_EXCHANGE,
                                 RabbitUtils.NORMAL_ROUTING_KEY,
                                 null,
                                 String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void queueFull() throws IOException {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();

        // 2、发送消息
        for (int i = 1; i <= COUNT; i++) {
            channel.basicPublish(RabbitUtils.NORMAL_EXCHANGE,
                                 RabbitUtils.NORMAL_ROUTING_KEY,
                                 null,
                                 String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

    private static void TTLTest() throws IOException {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();


        /*// 2、声明交换机
        channel.exchangeDeclare(RabbitUtils.NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);
        // 2、声明死信交换机
        channel.exchangeDeclare(RabbitUtils.DEAD_EXCHANGE, BuiltinExchangeType.DIRECT);


        // 3、声明死信队列
        channel.queueDeclare(RabbitUtils.DEAD_QUEUE, false, false, false, null);
        // 3、死信队列绑定死信交换机
        channel.queueBind(RabbitUtils.DEAD_QUEUE, RabbitUtils.DEAD_EXCHANGE, RabbitUtils.DEAD_ROUTING_KEY);


        // 4、其他属性（绑定参数）
        Map<String, Object> arguments = new HashMap<>();
        arguments.put("x-dead-letter-exchange", RabbitUtils.DEAD_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", RabbitUtils.DEAD_ROUTING_KEY);
        arguments.put("x-max-length", 6);


        // 5、声明正常队列，后面的参数是如果该队列中的消息成为死信消息，则将其转发到死信交换机
        channel.queueDeclare(RabbitUtils.NORMAL_QUEUE, false, false, false, arguments);
        // 5、队列绑定到交换机
        channel.queueBind(RabbitUtils.NORMAL_QUEUE, RabbitUtils.NORMAL_EXCHANGE, RabbitUtils.NORMAL_ROUTING_KEY);*/


        // 6、设置消息的TTL时间
        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().expiration("10000").build();


        // 7、发送消息
        for (int i = 1; i <= COUNT; i++) {
            channel.basicPublish(RabbitUtils.NORMAL_EXCHANGE,
                                 RabbitUtils.NORMAL_ROUTING_KEY,
                                 properties,
                                 String.valueOf(i).getBytes(StandardCharsets.UTF_8));
        }
    }

}
