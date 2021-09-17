package com.example.rabbitmq._02_work_queues._05_prefetch_count;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * RabbitMQ 预期值，解决不公平分发
 *
 * channel.basicQos(1)指该消费者在接收到队列里的消息但没有返回确认结果之前，队列不会将新的消息分发给该消费者。
 * 队列中没有被消费的消息不会被删除，还存在于队列中。
 *
 * channel.basicQos(1)和channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false)
 * 是配套使用，只有在channel.basicQos被使用的时候channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false)才起到作用。
 *
 * channel.basicQos(1)
 * 请求特定的“quality of service(服务的质量)”设置。
 * 限制生产者在收到确认之前发送给消费者的消息数量，从而实现在消费者端进行流量控制
 *
 * @author Kavin
 * @date 2021-09-17 13:17:46
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


        // 3、设置qos，解决不公平分发
        /*
         * channel.basicQos(1)指该消费者在接收到队列里的消息但没有返回确认结果之前，队列不会将新的消息分发给该消费者。
         * 队列中没有被消费的消息不会被删除，还存在于队列中。
         *
         * channel.basicQos(1)和channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false)
         * 是配套使用，只有在channel.basicQos被使用的时候channel.basicAck(delivery.getEnvelope().getDeliveryTag(),false)才起到作用。
         *
         * 请求特定的“quality of service(服务的质量)”设置。
         * 限制服务器在收到确认之前发送给消费者的消息数量，从而实现在消费者端进行流量控制
         *
         * prefetchSize - 服务器将提供的最大内容量（以八位字节为单位），如果无限制则为 0
         * prefetchCount - 服务器将传递的最大消息数，如果没有限制则为 0
         * global - 如果设置应该应用于整个channel而不是每个consumer，则为 true
         */
        channel.basicQos(2);


        // 4、基本消费
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
