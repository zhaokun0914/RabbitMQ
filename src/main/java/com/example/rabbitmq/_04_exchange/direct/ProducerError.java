package com.example.rabbitmq._04_exchange.direct;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * direct交换机生产者
 *
 * @author Kavin
 * @date 2021-09-18 09:07:19
 */
public class ProducerError {
    public static final Logger LOGGER = LoggerFactory.getLogger(ProducerError.class);

    public static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] args) throws Exception {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();

        // 2、声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // 3、发送消息
        Scanner scanner = new Scanner(System.in);
        LOGGER.info("==> 请输入即将发送的消息，此消息发送时候的routing key为: error");
        while (scanner.hasNext()) {
            String message = scanner.next();
            channel.basicPublish(EXCHANGE_NAME, "error", null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

}
