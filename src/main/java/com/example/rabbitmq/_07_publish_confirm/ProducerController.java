package com.example.rabbitmq._07_publish_confirm;

import com.alibaba.fastjson.JSON;
import com.example.rabbitmq.config._02_publish_confirm_config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * http://localhost:8080/confirm/sendMessage/啦啦啦啦
 *
 * @author Kavin
 * @date 2021-09-24 15:41:11
 */
@Slf4j
@RestController("confirmProducerController")
@RequestMapping("/confirm")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void setCallback() {
        // 该回调函数会在交换机接收到消息的时候调用，一个 RabbitTemplate 只能设置一个 ConfirmCallback 回调函数
        RabbitTemplate.ConfirmCallback confirmCallback = (correlationData, ack, cause) -> {
            String id = correlationData != null ? correlationData.getId() : "";
            if (ack) {
                log.info("交换机已经收到 id 为:{}的消息", id);
            } else {
                log.info("交换机还未收到 id 为:{}消息，由于原因:{}", id, cause);
            }
        };
        rabbitTemplate.setConfirmCallback(confirmCallback);

        /*
         * spring.rabbitmq.publisher-confirm-type=correlated
         * 在[仅开启了生产者确认机制]的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息，如果发现该消息不可路由，那么消息会被直接丢弃，此时生产者是不知道消息被丢弃这个事件的。
         * 那么如何让无法被路由的消息帮我想办法处理一下？最起码通知我一声，我好自己处理啊。
         * 通过设置 [mandatory] 参数可以在[当消息传递过程中不可达目的地]时[将消息返回给生产者]。
         *
         * 发送消息时设置[强制标志]； 仅当提供了[returnCallback]时才适用
         * true：  如果发现消息无法进行路由，会[将该消息返回给生产者]
         * false： 如果发现消息无法进行路由，则[直接丢弃]
         *
         * 效果如下
         *     生产者：==> 给routing key为：key1 的队列发送消息：啦啦啦啦，消息的ID为：d140d27e-72f0-4260-8238-1bef8dae7ed5
         *     生产者：==> 给routing key为：key2 的队列发送消息：啦啦啦啦，消息的ID为：08ec4c89-0252-4697-abf1-c5df3fbccec3
         *
         *     消费者：<== 接收到routing key为：key1 的队列的消息:啦啦啦啦
         *
         *     ReturnsCallback：==> ReturnedMessage:{"exchange":"confirm.exchange","message":{"body":"5ZWm5ZWm5ZWm5ZWm","messageProperties":{"contentEncoding":"UTF-8","contentLength":0,"contentType":"text/plain","deliveryTag":0,"finalRetryForMessageWithNoId":false,"headers":{"spring_returned_message_correlation":"08ec4c89-0252-4697-abf1-c5df3fbccec3"},"lastInBatch":false,"priority":0,"publishSequenceNumber":0,"receivedDeliveryMode":"PERSISTENT"}},"replyCode":312,"replyText":"NO_ROUTE","routingKey":"key2"}
         *     ReturnsCallback：消息:[啦啦啦啦]被服务器退回，退回原因:NO_ROUTE, 交换机是:confirm.exchange, 路由 key:key2
         *
         *     ConfirmCallback：交换机已经收到 id 为:08ec4c89-0252-4697-abf1-c5df3fbccec3的消息
         *     ConfirmCallback：交换机已经收到 id 为:d140d27e-72f0-4260-8238-1bef8dae7ed5的消息
         */
        rabbitTemplate.setMandatory(true);

        // 设置[回退消息]交给[谁处理]
        RabbitTemplate.ReturnsCallback returnsCallback = returned -> {
            log.warn("==> ReturnedMessage:{}", JSON.toJSONString(returned));
            log.info("消息:[{}]被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}",
                     new String(returned.getMessage().getBody(), StandardCharsets.UTF_8),
                     returned.getReplyText(),
                     returned.getExchange(),
                     returned.getRoutingKey());
        };
        rabbitTemplate.setReturnsCallback(returnsCallback);
    }

    /**
     * 生产者：==> 给routing key为：key1 的队列发送消息：啦啦啦，消息的ID为：401f5625-f053-4d17-9282-03382cba9899
     * 生产者：==> 给routing key为：key2 的队列发送消息：啦啦啦，消息的ID为：10095069-d122-4595-a672-bd50249e491d
     *
     * ConfirmCallback：交换机已经收到 id 为:10095069-d122-4595-a672-bd50249e491d的消息
     * ConfirmCallback：交换机已经收到 id 为:401f5625-f053-4d17-9282-03382cba9899的消息
     *
     * 消费者：<== 接收到routing key为：key1 的队列的消息:啦啦啦
     *
     * 可以看到，发送了两条消息，第一条消息的 RoutingKey 为 "key1"，第二条消息的 RoutingKey 为"key2"，
     * 两条消息都[成功被交换机接收]，也收到了交换机的确认回调，但消费者只收到了一条消息，
     * 因为第二条消息的 RoutingKey 与队列的 BindingKey 不一致，也没有其它队列能接收这个消息，所有第二条消息被直接丢弃了。（这就是问题！因为生产者不知道第二条消息被丢弃了）
     */
    @GetMapping("sendMessage/{message}")
    public void sendMessage(@PathVariable String message) {
        //指定消息ID为1，如果不指定会默认生成一个UUID
        CorrelationData correlationData1 = new CorrelationData();
        String routingKey = ConfirmConfig.CONFIRM_QUEUE_KEY1;
        log.info("==> 给routing key为：{} 的队列发送消息：{}，消息的ID为：{}", routingKey, message, correlationData1.getId());
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME, routingKey, message, correlationData1);


        CorrelationData correlationData2 = new CorrelationData();
        String nonexistence = "key2";
        log.info("==> 给routing key为：{} 的队列发送消息：{}，消息的ID为：{}", nonexistence, message, correlationData2.getId());
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME, nonexistence, message, correlationData2);
    }

}
