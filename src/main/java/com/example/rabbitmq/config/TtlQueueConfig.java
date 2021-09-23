package com.example.rabbitmq.config;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
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
