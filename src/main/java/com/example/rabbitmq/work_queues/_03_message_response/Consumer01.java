package com.example.rabbitmq.work_queues._03_message_response;

import com.example.rabbitmq.common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * 消息应答
 *     现象：MQ中的消息发送给消费者之后，如果消费者在执行的过程中挂掉了，那么这个消息就会丢失，并且无法接受到后续发送给他的消息
 *     解决办法：采用消息应答模式，即消费者在接收到消息并且处理该消息之后，告诉RabbitMQ它已经处理了，RabbitMQ可以把该消息删除掉了
 *     实现方式：
 *         自动应答：
 *             消息发送后立即被认为已经传送成功，这种模式需要在高吞吐量和数据传输安全性方面做权衡，因为这种模式如果消息在接收到之前，消费者那边出现connection或者channel关闭，那么消息就丢失了。
 *             另一方面，这种模式会给消费者传递过载的消息（没有对传递的消息数量进行限制），这样可能会使消费者接受太多来不及处理的消息导致消息积压，最终内存耗尽，被操作系统杀死。
 *             所以，这种模式仅使用在消费者可以高效并以某种速率能够处理这些消息的情况下使用。
 *         手动应答：
 *             1. channel.basicAck(long deliveryTag, boolean multiple)(肯定确认)
 *                 RabbitMQ已知道该消息并且成功地处理消息，可以将其丢弃了
 *             2. channel.basicNack(long deliveryTag, boolean multiple, boolean requeue)(否定确认)
 *             3. channel.basicReject(long deliveryTag, boolean requeue)(否定确认)
 *                 与channel.basicNack相比少一个multiple参数，不处理该消息了直接拒绝，可以将其丢弃了
 *             multiple(多重的)的解释：
 *                 手动应答的好处是可以 批量应答/拒绝 并且减少网络拥堵
 *                 true
 *                     将一次性 确认/拒绝 所有小于deliveryTag的消息
 *                     比如说channel上有传送tag的消息5、6、7，当前tag是8，那么此时小于等于8的消息都会被确认
 *                 false
 *                     同上面相比，只会应答tag=8的消息，5、6、7这三个消息依然不会被确认收到消息应答
 *             requeue(重新入队)的解释：
 *                 如果消费者由于某些原因失去connection(其channel已关闭，连接已关闭或TCP连接丢失)，导致消息未发送ack，RabbitMQ将了解到消息未完全处理，并将对其重新排队，
 *                 如果此时其他消费者可以处理，它就很快将其重新分发给另一个消费者，这样，即使某个消费者偶尔死亡，也可以确保不会丢失任何消息。
 *
 * @author Kavin
 * @date 2021-9-13 21:07:06
 */
public class Consumer01 {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer01.class);
    public static final String ACK = "ack";
    public static final String NACK = "nack";
    public static final String REJECT = "reject";

    /**
     * 接收消息
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("==> C1 工作线程等待接收消息");

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
        channel.queueDeclare(RabbitUtils.ACK_QUEUE, false, false, false, null);

        // 3、基本消费
        /*
         * queue - queue的名称
         * autoAck - 自动应答ture，手动应答false
         * deliverCallback(consumerTag, message) - 交付消息时的回调
         * cancelCallback(consumerTag) - 消费者被取消时的回调
         * 返回：服务器生成的consumerTag
         */
        boolean autoAck = false;
        channel.basicConsume(RabbitUtils.ACK_QUEUE, autoAck, (consumerTag, delivery) -> {
            // 接收消息时的回调
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);

            try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}
            LOGGER.info("work queues 接受到的消息：" + msg);

            /*
             * deliveryTag - 交付标签
             * multiple - true 确认所有消息，包括当前的交付标签； false 仅确认当前的交付标签。
             */
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false);

        }, (consumerTag) -> {
            // 消息被取消时执行
            LOGGER.info("work queues 消息被取消时执行:" + new String(consumerTag.getBytes(StandardCharsets.UTF_8)));
        });
    }

}
