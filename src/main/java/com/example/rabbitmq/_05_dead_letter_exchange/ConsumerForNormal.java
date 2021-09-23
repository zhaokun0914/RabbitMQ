package com.example.rabbitmq._05_dead_letter_exchange;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Delivery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 正常队列 消费者
 *
 * @author Kavin
 * @date 2021-09-18 13:16:02
 */
public class ConsumerForNormal {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerForNormal.class);

    public static void main(String[] args) throws Exception {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();


        // 2、声明交换机
        channel.exchangeDeclare(RabbitUtils.NORMAL_EXCHANGE, BuiltinExchangeType.DIRECT);


        // 3、其他属性（绑定参数）
        Map<String, Object> arguments = new HashMap<>(16);
        arguments.put("x-dead-letter-exchange", RabbitUtils.DEAD_EXCHANGE);
        arguments.put("x-dead-letter-routing-key", RabbitUtils.DEAD_ROUTING_KEY);
        arguments.put("x-max-length", 10);// 队列达到最大长度


        // 4、声明正常队列，后面的参数是如果该队列中的消息成为死信消息，则将其转发到 死信交换机 和 routing key
        channel.queueDeclare(RabbitUtils.NORMAL_QUEUE, false, false, false, arguments);
        // 4、队列绑定到交换机
        channel.queueBind(RabbitUtils.NORMAL_QUEUE, RabbitUtils.NORMAL_EXCHANGE, RabbitUtils.NORMAL_ROUTING_KEY);


        // 5、发送消息
        channel.basicConsume(RabbitUtils.NORMAL_QUEUE, false, (String consumerTag, Delivery delivery) -> {
            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);
            if ("5".equals(message)) {
                LOGGER.info("==> 正常队列拒接该消息，使其成为死信消息，消息内容为：{}", message);
                channel.basicReject(delivery.getEnvelope().getDeliveryTag(), false);
            } else {
                LOGGER.info("==> 正常队列接收到消息：{}", message);
                channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
            }
        }, delivery -> {

        });
    }

}
