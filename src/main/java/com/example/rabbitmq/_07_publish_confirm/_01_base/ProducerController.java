package com.example.rabbitmq._07_publish_confirm._01_base;

import com.example.rabbitmq.config._02_publish_confirm_config.ConfirmConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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

    public void setCallback() {
        // 该回调函数会在交换机接收到消息的时候调用
        RabbitTemplate.ConfirmCallback callback = (correlationData, ack, cause) -> {
            String id = correlationData != null ? correlationData.getId() : "";
            if (ack) {
                log.info("交换机已经收到 id 为:{}的消息", id);
            } else {
                log.info("交换机还未收到 id 为:{}消息，由于原因:{}", id, cause);
            }
        };
        rabbitTemplate.setConfirmCallback(callback);
    }

    /**
     * 2021-09-24 17:04:59,980 [http-nio-8080-exec-9]  INFO ProducerController:56 - ==> 给routing key为：key1 的队列发送消息：啦啦啦，消息的ID为：401f5625-f053-4d17-9282-03382cba9899
     * 2021-09-24 17:04:59,981 [http-nio-8080-exec-9]  INFO ProducerController:62 - ==> 给routing key为：key2 的队列发送消息：啦啦啦，消息的ID为：10095069-d122-4595-a672-bd50249e491d
     * 2021-09-24 17:04:59,982 [rabbitConnectionFactory6]  INFO ProducerController:35 - 交换机已经收到 id 为:10095069-d122-4595-a672-bd50249e491d的消息
     * 2021-09-24 17:04:59,982 [org.springframework.amqp.rabbit.RabbitListenerEndpointContainer#2-1]  INFO ConfirmConsumer:16 - <== 接收到routing key为：key1 的队列的消息:啦啦啦
     * 2021-09-24 17:04:59,983 [rabbitConnectionFactory6]  INFO ProducerController:35 - 交换机已经收到 id 为:401f5625-f053-4d17-9282-03382cba9899的消息
     *
     * 可以看到，发送了两条消息，第一条消息的 RoutingKey 为 "key1"，第二条消息的 RoutingKey 为"key2"，
     * 两条消息都[成功被交换机接收]，也收到了交换机的确认回调，但消费者只收到了一条消息，
     * 因为第二条消息的 RoutingKey 与队列的 BindingKey 不一致，也没有其它队列能接收这个消息，所有第二条消息被直接丢弃了。（这就是问题！因为生产者不知道第二条消息被丢弃了）
     */
    @GetMapping("sendMessage/{message}")
    public void sendMessage(@PathVariable String message) {
        setCallback();

        //指定消息ID为1，如果不指定会默认生成一个UUID
        CorrelationData correlationData1 = new CorrelationData();
        String routingKey = ConfirmConfig.CONFIRM_QUEUE_KEY;
        log.info("==> 给routing key为：{} 的队列发送消息：{}，消息的ID为：{}", routingKey, message, correlationData1.getId());
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME, routingKey, message, correlationData1);


        CorrelationData correlationData2 = new CorrelationData();
        String nonexistence = "key2";
        log.info("==> 给routing key为：{} 的队列发送消息：{}，消息的ID为：{}", nonexistence, message, correlationData2.getId());
        rabbitTemplate.convertAndSend(ConfirmConfig.CONFIRM_EXCHANGE_NAME, nonexistence, message, correlationData2);
    }

}
