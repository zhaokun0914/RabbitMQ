package com.example.rabbitmq._03_publish_subscribe;

import com.example.rabbitmq._00_common.RabbitUtils;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConfirmCallback;
import com.rabbitmq.client.MessageProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;

/**
 * RabbitMQ 发布订阅实现真正持久化
 *     发布订阅实现真正持久化
 *         原理：生产者将channel设置成confirm模式，一旦channel进入confirm模式，所有在该channel上面发布的消息都将会被指派一个唯一的ID(从1开始)。
 *              一旦消息被投递到所有匹配的队列之后，broker就会发送一个确认给生产者(包含消息的唯一ID)，这就使得生产者知道消息已经正确到达目的队列了。
 *              如果[消息]和[队列]是[可持久化]的，那么[确认消息]会在[将消息写入磁盘之后发出]。
 *              broker回传给生产者的确认消息中delivery-tag域包含了确认消息的序列号，此外broker也可以设置basic.ack的multiple域，表示到这个序列号之前的所有消息都已经得到了处理。
 *              confirm模式最大的好处在于他是异步的，一旦发布一条消息，生产者应用程序就可以在等channel返回确认的同时继续发送下一条消息。
 *              当消息最终得到确认之后，生产者应用便可以通过回调方法来处理该确认消息，如果RabbitMQ因为自身内部错误导致消息丢失，就会发送一条nack消息，生产者应用程序同样可以在回调方法中处理该nack消息。
 *         实现方式：
 *             1：开启发布确认的方法
 *                 发布确认默认没有开启，如果需要开启需要调用方法channel.confirmSelect()
 *             2：3种发布确认的实现方式
 *                 1、单个确认发布
 *                     定义：这是一种[同步确认发布]的方式，也就是发布一个消息之后只有它被确认，后续的消息才能继续发布.
 *                     实现方式：waitForConfirmsOrDie(long)这个方法只有在消息被确认的时候才返回，如果在指定时间范围内这个消息没有被确认那么它将抛出异常。
 *                     缺点：发布速度特别的慢，而且如果没有确认的消息就会阻塞所有后续消息的发布，这种方式最多提供每秒不超过数百条发布消息的吞吐量。
 *                          当然对于某些应用程序来说这可能已经足够了。
 *                 2、批量发布确认
 *                     定义：与单个等待确认消息相比，批量发布确认先发布一批消息然后一起确认可以极大地提高吞吐量
 *                     缺点：当发生故障导致发布出现问题时，不知道是哪个消息出现问题了，我们必须将整个批处理保存在内存中，以记录重要的信息而后重新发布消息。
 *                          当然这种方案仍然是[同步的]，也一样阻塞消息的发布。
 *                 3、异步确认发布
 *                     异步确认虽然编程逻辑比上两个要复杂，但是性价比最高，无论是可靠性还是效率都没得说，他是利用回调函数来达到消息可靠性传递的，这个中间件也是通过函数回调来保证是否投递成功
 *             3：如何处理异步未确认消息
 *                 最好的解决的解决方案就是把未确认的消息放到一个基于内存的能被发布线程访问的队列，比如说用ConcurrentSkipListMap这个队列在 confirm callbacks 与 发布线程 之间进行消息的传递。
 *             4：以上3种发布确认速度对比
 *                 单独发布消息
 *                     同步等待确认，简单，但吞吐量非常有限。
 *                 批量发布消息
 *                     批量同步等待确认，简单，合理的吞吐量，一旦出现问题但很难推断出是那条消息出现了问题。
 *                 异步处理：
 *                     最佳性能和资源使用，在出现错误的情况下可以很好地控制，但是实现起来稍微难些
 *
 * @author kevin
 * @date 2021-09-17 15:05:15
 */
public class Producer {

    public static final Logger LOGGER = LoggerFactory.getLogger(Producer.class);
    public static final int MESSAGE_COUNT = 1000;

    public static void main(String[] args) throws Exception {

        // 单个确认发布，发布1000条[单独确认]消息，总耗时：1430ms
        // singleConfirm();

        // 批量确认发布，发布1000条[批量确认]消息，总耗时：64ms
        // batchConfirm();

        // 异步确认发布，发布1000条[异步确认]消息，总耗时：24ms
        asynchronizationConfirm();

    }

    private static void asynchronizationConfirm() throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();

        // 2、开启发布确认
        channel.confirmSelect();

        // 3、声明[持久化队列]
        channel.queueDeclare(RabbitUtils.PUBLISH_SUBSCRIBE, true, false, false, null);

        // 4、声明一个Map用来存储发送的消息
        /*
         * 线程安全有序的一个哈希表，适用于高并发的情况
         * 1.轻松的将序号与消息进行关联
         * 2.轻松批量删除条目 只要给到序列号
         * 3.支持并发访问
         */
        ConcurrentSkipListMap<Long, String> skipListMap = new ConcurrentSkipListMap<>();

        /*
         * 收到ack的回调
         * 1.消息序列号
         * 2.是否为批量确认，true 可以确认小于等于当前序列号的消息，false 确认当前序列号消息
         */
        ConfirmCallback ackCallback = (sequenceNumber, multiple) -> {
            LOGGER.info("<== 收到服务器确认消息序号{}", sequenceNumber);
            if (multiple) {
                // headMap返回的是小于等于当前序列号的未确认消息map
                ConcurrentNavigableMap<Long, String> confirmed = skipListMap.headMap(sequenceNumber, true);
                LOGGER.info("==> 批量确认模式，其中未确认的消息条数为 {} 条", confirmed.size());
                // 清除未确认的消息
                confirmed.clear();
            } else {
                // 只清除当前序列号的消息
                skipListMap.remove(sequenceNumber);
            }
        };

        /*
         * 收到nack的回调
         * 1.消息序列号
         * 2.是否为批量确认，true 可以确认小于等于当前序列号的消息，false 确认当前序列号消息
         */
        ConfirmCallback nackCallback = (sequenceNumber, multiple) -> {
            String message = skipListMap.get(sequenceNumber);
            LOGGER.info("发布的消息[{}]未被确认，序列号[{}]", message, sequenceNumber);
        };

        // 5、将 ack/nack 回调函数添加到 channel 中
        /*
         * 添加一个异步确认的监听器
         * ackCallback - ack回调
         * nackCallback - nack回调
         */
        channel.addConfirmListener(ackCallback, nackCallback);

        // 6、发送消息，同时声明[消息持久化]
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MESSAGE_COUNT; i++) {
            // 7、将要发送的消息添加到 Map 容器中
            /*
             * channel.getNextPublishSeqNo()获取下一个消息的序列号
             * 通过序列号与消息体进行一个关联
             * 全部都是未确认的消息体
             */
            skipListMap.put(channel.getNextPublishSeqNo(), String.valueOf(i));

            // 发送消息
            channel.basicPublish("", RabbitUtils.PUBLISH_SUBSCRIBE, MessageProperties.PERSISTENT_TEXT_PLAIN, String.valueOf(i).getBytes());
            LOGGER.info("==> 发送消息{}",i);
        }

        long elapsedTime = (System.currentTimeMillis() - startTime);
        LOGGER.info("<== 发布{}条[异步确认]消息，总耗时：{}ms", MESSAGE_COUNT, elapsedTime);
    }

    private static void batchConfirm() throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();

        // 2、开启发布确认
        channel.confirmSelect();

        // 3、声明[持久化队列]
        channel.queueDeclare(RabbitUtils.PUBLISH_SUBSCRIBE, true, false, false, null);

        // 批量确认消息的阈值
        int batchSize = 100;

        // 4、发送消息，同时声明[消息持久化]
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MESSAGE_COUNT; i++) {

            // 发送消息
            channel.basicPublish("", RabbitUtils.PUBLISH_SUBSCRIBE, MessageProperties.PERSISTENT_TEXT_PLAIN, String.valueOf(i).getBytes());

            if (i % batchSize == 0) {
                // 批量接收确认
                if (channel.waitForConfirms()) {
                    LOGGER.info("<== 消息发送成功");
                }
            }

        }
        long elapsedTime = (System.currentTimeMillis() - startTime);
        LOGGER.info("<== 发布{}条[批量确认]消息，总耗时：{}ms", MESSAGE_COUNT, elapsedTime);
    }

    private static void singleConfirm() throws Exception {
        // 1、获取信道
        Channel channel = RabbitUtils.createChannel();

        // 2、开启发布确认
        channel.confirmSelect();

        // 3、声明[持久化队列]
        channel.queueDeclare(RabbitUtils.PUBLISH_SUBSCRIBE, true, false, false, null);

        // 4、发送消息，同时声明[消息持久化]
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < MESSAGE_COUNT; i++) {

            // 发送消息
            channel.basicPublish("", RabbitUtils.PUBLISH_SUBSCRIBE, MessageProperties.PERSISTENT_TEXT_PLAIN, String.valueOf(i).getBytes());

            // 接收确认
            if (channel.waitForConfirms()) {
                LOGGER.info("<== 消息发送成功");
            }
        }
        long elapsedTime = (System.currentTimeMillis() - startTime);
        LOGGER.info("<== 发布{}条[单独确认]消息，总耗时：{}ms", MESSAGE_COUNT, elapsedTime);

    }

}
