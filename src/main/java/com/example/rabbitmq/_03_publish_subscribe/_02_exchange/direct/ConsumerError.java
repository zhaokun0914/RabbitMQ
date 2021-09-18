package com.example.rabbitmq._03_publish_subscribe._02_exchange.direct;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.FileWriterWithEncoding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.charset.StandardCharsets;

/**
 * direct交换机的消费者
 *
 * @author Kavin
 * @date 2021-09-18 09:04:55
 */
public class ConsumerError {

    public static final Logger LOGGER = LoggerFactory.getLogger(ConsumerError.class);

    public static final String EXCHANGE_NAME = "direct_exchange";

    public static void main(String[] args) throws Exception {
        // 1、获取channel
        Channel channel = RabbitUtils.createChannel();

        // 2、声明交换机
        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.DIRECT);

        // 3、声明临时队列
        String queueName = channel.queueDeclare().getQueue();

        // 4、绑定到交换机
        channel.queueBind(queueName, EXCHANGE_NAME, "error");

        // 5、接收消息
        channel.basicConsume(queueName, true, (consumerTag, delivery) -> {

            String message = new String(delivery.getBody(), StandardCharsets.UTF_8);

            File fileName = new File("C:\\Users\\Kavin\\Desktop\\log.txt");
            FileUtils.writeStringToFile(fileName, message, StandardCharsets.UTF_8, true);

            LOGGER.info("==> 已将消息:『{}』写入文件", message);
        }, consumerTag -> {
        });
    }

}
