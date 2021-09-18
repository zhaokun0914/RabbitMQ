package com.example.rabbitmq._04_exchange.fanout;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.BuiltinExchangeType;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * Exchange
 *     概念：RabbitMQ消息传递模型的核心思想是：[生产者生产的消息从不会直接发送到队列]，相反，[生产者只能将消息发送到交换机]。
 *          交换机工作的内容非常简单，一方面它接收来自生产者的消息，另一方面将它们放到队列。
 *          交换机必须确切知道如何处理收到的消息。
 *          是应该把这些消息放到特定队列，还是把他们放到许多队列中，还是说应该丢弃它们。
 *          这就的由交换机的类型来决定。
 *     类型：直接(direct)，主题(topic)，标题(headers)，扇出(fanout)
 *     默认交换机：将消息根据指定的routingKey(binding key)发送到队列中
 *     临时队列：每当我们连接到 Rabbit 时，让服务器为我们选择一个随机队列名称，一旦我们断开了消费者的连接，队列将被自动删除。
 *         String queueName = channel.queueDeclare().getQueue();
 *     绑定(bindings)：binding 其实是 exchange 和 queue 之间的桥梁，它告诉我们 exchange 和那个 queue 进行了绑定。
 *     Fanout：
 *         介绍：它是将接收到的[所有消息][广播]到它知道的[所有队列中]，即使队列和交换机绑定的routing key不同，也能接收到消息
 *
 * @author Kavin
 * @date 2021-09-17 16:50:33
 */
public class Producer {

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

        // 3、发送消息
        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();
            channel.basicPublish(EXCHANGE_NAME, "", null, message.getBytes(StandardCharsets.UTF_8));
        }
    }

}
