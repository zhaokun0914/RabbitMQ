package com.example.rabbitmq._02_work_queues._03_durable;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

/**
 * RabbitMQ持久化
 *     场景：如何保障当 RabbitMQ 服务停掉以后消息生产者发送过来的消息不丢失。默认情况下 RabbitMQ 退出或由于某种原因崩溃时，它会自动忽视队列和消息
 *     解决办法：我们需要将[队列]和[消息]都[标记为持久化]。
 *     实现方式：
 *         队列持久化
 *             在声明队列的时候将 [durable] 参数设置为 [true] 即可
 *             需要注意的就是如果之前声明的队列不是持久化的，需要把原队列删除，然后重新创建一个持久化的队列，不然就会出现错误
 *         消息持久化
 *             需要在消息[生产者端]修改代码，[props]参数设置为[MessageProperties.PERSISTENT_TEXT_PLAIN]这个属性。
 *             PS:将消息标记为持久化并不能完全保证不会丢失消息。尽管它告诉 RabbitMQ 将消息保存到磁盘，但是这里依然存在当消息刚准备存储在磁盘的时候但是还没有存储完，消息还在缓存的一个间隔点。
 *             此时并没有真正写入磁盘。持久性保证并不强，但是对于我们的简单任务队列而言，这已经绰绰有余了。如果需要更强有力的持久化策略，参考后面的发布确认demo
 *
 * @author kevin
 * @date 2021-09-14 20:59:36
 */
public class Producer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);

    public static void main(String[] args) throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();


        // 2、声明[持久化队列]
        /*
         * queue - queue的名称
         * durable - 如果我们声明一个持久队列，则为 true（该队列将在服务器重启后继续存在）
         * exclusive - 如果我们声明独占队列（仅限于此连接），则为 true
         * autoDelete - 如果我们声明一个自动删除队列，则为 true（服务器将在不再使用时将其删除）
         * arguments - 队列的其他属性（构造参数）
         */
        // 队列持久化
        boolean durable = true;
        channel.queueDeclare(RabbitUtils.ACK_QUEUE, durable, false, false, null);


        Scanner scanner = new Scanner(System.in);
        while (scanner.hasNext()) {
            String message = scanner.next();

            // 3、发送消息，同时声明[消息持久化]
            /*
             * exchange - 将消息发布到的交换机
             * routingKey - 路由key
             * props - 消息的其他属性 - 路由标头等
             *   消息持久化：MessageProperties.PERSISTENT_TEXT_PLAIN
             * body - 消息正文
             *
             */
            channel.basicPublish("", RabbitUtils.ACK_QUEUE, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes(StandardCharsets.UTF_8));
            LOGGER.info("<== 消息发送完成：{}", message);
        }
    }

}
