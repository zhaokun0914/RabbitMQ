package com.example.rabbitmq._02_work_queues._04_unfair_dispatch;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ不公平分发 消费者
 * 不公平分发
 *     场景：在默认情况下，队列是将消息[轮询]分发给多个消费者的，当某个消费者执行的时候需要花费大量时间就会导致，队列后面分配给该消费者的消息大量堆积。
 *         理想情况应该是假如该消费者正在处理，则队列将该消息分配给另外一个空闲的消费者。
 *     解决办法：设置qos(channel未确认缓冲区)的大小，来限制每个channel的未确认消息数量，如果超过设定的数量，则该消费者拒绝接收消息，服务器自动将其分配给另外一个空闲的消费者
 *     实现方式：channel.basicQos(1);
 *
 * @author Kavin
 * @date 2021-09-14 20:02:32
 */
public class Consumer01 {

    public static final Logger LOGGER = LoggerFactory.getLogger(Consumer01.class);

    /**
     * 接收消息
     */
    public static void main(String[] args) throws Exception {
        LOGGER.info("==> C1 工作线程等待接收消息，处理速度很快，1秒");

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
        boolean durable = true;
        channel.queueDeclare(RabbitUtils.ACK_QUEUE, durable, false, false, null);


        // 3、基本消费
        /*
         * queue - queue的名称
         * autoAck - 自动应答ture，手动应答false
         * deliverCallback(consumerTag, message) - 交付消息时的回调
         * cancelCallback(consumerTag) - 消费者被取消时的回调
         * 返回：服务器生成的consumerTag
         */
        channel.basicConsume(RabbitUtils.ACK_QUEUE, false, (consumerTag, delivery) -> {
            try {TimeUnit.SECONDS.sleep(1);} catch (InterruptedException e) {e.printStackTrace();}

            // 接收消息时的回调
            String msg = new String(delivery.getBody(), StandardCharsets.UTF_8);
            LOGGER.info("work queues 接受到的消息：" + msg);
            channel.basicAck(delivery.getEnvelope().getDeliveryTag(), false);
        }, (consumerTag) -> {
            // 消息被取消时执行
            LOGGER.info("work queues 消息被取消时执行:" + new String(consumerTag.getBytes(StandardCharsets.UTF_8)));
        });
    }

}
