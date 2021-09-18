package com.example.rabbitmq._03_publish_subscribe._02_exchange.fanout;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;

/**
 * 所有消费者都能接收到Fanout交换机发出的消息，即使队列和交换机绑定的routing key不同，也能接收到消息
 *
 * @author Kavin
 * @date 2021-09-17 16:50:33
 */
public class Consumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    public static final String EXCHANGE_NAME = "logs";

    public static void main(String[] args) throws Exception {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();

        // 2、声明交换机
        /*
         * exchange - exchange的名称
         * type - 交换类型
         */
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.FANOUT);

        // 3、获取临时队列，当消费者断开和该队列的连接时 队列自动删除
        String queueName = channel.queueDeclare().getQueue();

        // 4、将队列绑定到交换机
        channel.queueBind(queueName, EXCHANGE_NAME, "1");

        // 5、消费消息
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("<== 控制台打印接收到的消息" + message);
        }, consumerTag -> {

        });
    }

}
