package com.example.rabbitmq.config;

import com.rabbitmq.client.BuiltinExchangeType;
import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author Kavin
 * @date 2021-09-23 17:25:38
 */
@Configuration
public class DelayedQueueConfig {

    public static final String DELAYED_EXCHANGE_NAME = "delayed.exchange";
    public static final String X_DELAYED_MESSAGE = "x-delayed-message";
    public static final String DELAYED_QUEUE_NAME = "delayed.queue";
    public static final String DELAYED_ROUTING_KEY = "delayed.routingkey";

    /**
     * 自定义交换机 我们在这里定义的是一个延迟交换机
     * 在我们自定义的交换机中，这是一种新的交换类型，该类型消息支持延迟投递机制。
     * 消息传递后并不会立即投递到目标队列中，而是存储在mnesia(一个分布式数据系统)表中，当达到投递时间时，才投递到目标队列中。
     *
     * @return
     */
    @Bean
    public CustomExchange delayedExchange() {
        CustomExchange customExchange = new CustomExchange(DELAYED_EXCHANGE_NAME, X_DELAYED_MESSAGE, true, false);
        customExchange.addArgument("x-delayed-type", BuiltinExchangeType.DIRECT);
        return customExchange;
    }

    @Bean
    public Queue delayedQueue() {
        return QueueBuilder.durable(DELAYED_QUEUE_NAME).build();
    }

    @Bean
    public Binding bindingDelayedQueue(Queue delayedQueue, CustomExchange delayedExchange) {
        return BindingBuilder.bind(delayedQueue).to(delayedExchange).with(DELAYED_ROUTING_KEY).noargs();
    }

}
