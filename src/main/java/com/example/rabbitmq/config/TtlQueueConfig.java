package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * TTL 是 RabbitMQ 中一个[消息]或者[队列]的属性，表明[一条消息]或者[该队列中的所有消息]的[最大存活时间]，单位是毫秒。
 * 如果[一条消息设置了TTL属性]或者[进入了设置TTL属性的队列]，那么这条消息如果在TTL设置的时间内[没有被消费]，则会成为"死信"。
 * 如果[同时]配置了[队列的TTL]和[消息的TTL]，那么较小的那个值将会被使用，有两种方式设置TTL。
 *     消息设置TTL
 *         correlationData -> {
 *             // 对比数据，通过此对象设置该消息的一些属性
 *             correlationData.getMessageProperties()
 *                            .setExpiration(ttlTime); // 设置该消息的TTL时间
 *             return correlationData;
 *         });
 *     队列设置TTL
 *         QueueBuilder.durable(QUEUE_B)
 *                     .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE) // 给该队列绑定死信队列
 *                     .deadLetterRoutingKey(DEAD_LETTER_QUEUE_ROUTING_KEY) // 给该队列绑定死信队列的routing key
 *                     .ttl(40000) // 设置该队列的TTL时间
 *                     .build();
 *     两者的区别
 *         第一种方式，消息即使过期，也不一定会被马上丢弃，因为消息是否过期是在即将投递到消费者之前判定的，如果当前队列有严重的消息积压情况，则已过期的消息也许还能存活较长时间
 *             另外，还需要注意的一点是，
 *                 如果不设置TTL， 表示消息永远不会过期。
 *                 如果将TTL设置为0，则表示除非此时可以直接投递该消息到消费者，否则该消息将会被丢弃。
 *         第二种方式，如果设置了队列的TTL属性，那么一旦消息过期，就会被队列丢弃(如果配置了死信队列被丢到死信队列中)。
 *
 * @author Kavin
 * @date 2021-09-23 15:14:10
 */
@Configuration
public class TtlQueueConfig {

    public static final String X_EXCHANGE = "X";
    public static final String QUEUE_A = "QA";
    public static final String QUEUE_A_ROUTING_KEY = "XA";
    public static final String QUEUE_B = "QB";
    public static final String QUEUE_B_ROUTING_KEY = "XB";

    public static final String Y_DEAD_LETTER_EXCHANGE = "Y";
    public static final String DEAD_LETTER_QUEUE = "QD";
    public static final String DEAD_LETTER_QUEUE_ROUTING_KEY = "YD";

    /**
     * 声明死信交换机
     */
    @Bean
    public DirectExchange yExchange() {
        return new DirectExchange(Y_DEAD_LETTER_EXCHANGE);
    }

    /**
     * 声明死信队列
     */
    @Bean
    public Queue queueD() {
        return QueueBuilder.durable(DEAD_LETTER_QUEUE).build();
    }

    /**
     * 将死信队列绑定到死信交换机
     *
     * @param queueD
     * @param yExchange
     * @return
     */
    @Bean
    public Binding queueDBindingY(Queue queueD, DirectExchange yExchange) {
        return BindingBuilder.bind(queueD).to(yExchange).with(DEAD_LETTER_QUEUE_ROUTING_KEY);
    }


    /**
     * 声明普通交换机
     */
    @Bean
    public DirectExchange xExchange() {
        return new DirectExchange(X_EXCHANGE);
    }

    /**
     * 声明普通队列A
     */
    @Bean
    public Queue queueA() {
        return QueueBuilder.durable(QUEUE_A)
                           .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE)
                           .deadLetterRoutingKey(DEAD_LETTER_QUEUE_ROUTING_KEY)
                           .ttl(10000)
                           .build();
    }

    /**
     * 声明普通队列B
     */
    @Bean
    public Queue queueB() {
        return QueueBuilder.durable(QUEUE_B)
                           .deadLetterExchange(Y_DEAD_LETTER_EXCHANGE)
                           .deadLetterRoutingKey(DEAD_LETTER_QUEUE_ROUTING_KEY)
                           .ttl(40000)
                           .build();
    }

    /**
     * 将队列A绑定到交换机X
     *
     * @param queueA
     * @param xExchange
     * @return
     */
    @Bean
    public Binding queueABindingX(Queue queueA, DirectExchange xExchange) {
        return BindingBuilder.bind(queueA).to(xExchange).with(QUEUE_A_ROUTING_KEY);
    }

    /**
     * 将队列B绑定到交换机X
     *
     * @param queueB
     * @param xExchange
     * @return
     */
    @Bean
    public Binding queueBBindingX(Queue queueB, DirectExchange xExchange) {
        return BindingBuilder.bind(queueB).to(xExchange).with(QUEUE_B_ROUTING_KEY);
    }

}
