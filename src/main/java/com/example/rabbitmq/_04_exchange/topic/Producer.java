package com.example.rabbitmq._04_exchange.topic;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Topic 交换机 生产者
 *     Q1
 *         *.orange.*
 *     Q2
 *         *.*.rabbit
 *         lazy.#
 *
 *     quick.orange.rabbit           被队列 Q1、Q2 接收到
 *     lazy.orange.elephant          被队列 Q1、Q2 接收到
 *     quick.orange.fox              被队列 Q1 接收到
 *     lazy.brown.fox                被队列 Q2 接收到
 *
 *     lazy.pink.rabbit              虽然满足两个绑定，但只被队列 Q2 接收一次
 *     quick.brown.fox               不匹配任何绑定，不会被任何队列接收到，会被丢弃
 *     quick.orange.male.rabbit      是四个单词不匹配任何绑定会被丢弃
 *     lazy.orange.male.rabbit       是四个单词但匹配 Q2
 *
 *     quick.orange.rabbit|被队列Q1、Q2接收到
 *     lazy.orange.elephant|被队列Q1、Q2接收到
 *     quick.orange.fox|被队列Q1接收到
 *     lazy.brown.fox|被队列Q2接收到
 *
 *     lazy.pink.rabbit|虽然满足两个绑定，但只被队列Q2接收一次
 *     quick.brown.fox|不匹配任何绑定，不会被任何队列接收到，会被丢弃
 *     quick.orange.male.rabbit|是四个单词不匹配任何绑定会被丢弃
 *     lazy.orange.male.rabbit|是四个单词但匹配Q2
 *
 * @author Kavin
 * @date 2021-09-18 10:29:26
 */
public class Producer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    public static final String EXCHANGE_NAME = "topic_exchange";

    public static void main(String[] args) throws Exception {

        Channel channel = RabbitUtils.createChannel();

        channel.exchangeDeclare(EXCHANGE_NAME, BuiltinExchangeType.TOPIC);

        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();
            String[] split = message.split("\\|");
            channel.basicPublish(EXCHANGE_NAME, split[0], null, split[1].getBytes(StandardCharsets.UTF_8));
        }
    }

}
