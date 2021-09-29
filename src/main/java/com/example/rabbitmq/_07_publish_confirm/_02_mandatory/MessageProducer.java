package com.example.rabbitmq._07_publish_confirm._02_mandatory;

import com.alibaba.fastjson.JSON;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ReturnedMessage;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;

import javax.annotation.PostConstruct;
import java.nio.charset.StandardCharsets;

/**
 * @author Kavin
 * @date 2021-09-28 13:31:36
 */
@Slf4j
@Component
public class MessageProducer implements RabbitTemplate.ConfirmCallback, RabbitTemplate.ReturnsCallback {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @GetMapping("sendMessage")
    public void sendMessage(String message) {
        // 让消息绑定一个ID值
        CorrelationData correlationData = new CorrelationData();
        // rabbitTemplate.convertAndSend();
    }


    @PostConstruct
    private void init() {
        rabbitTemplate.setConfirmCallback(this);

        /*
         * spring.rabbitmq.publisher-confirm-type=correlated
         * 在[仅开启了生产者确认机制]的情况下，交换机接收到消息后，会直接给消息生产者发送确认消息，如果发现该消息不可路由，那么消息会被直接丢弃，此时生产者是不知道消息被丢弃这个事件的。
         * 那么如何让无法被路由的消息帮我想办法处理一下？最起码通知我一声，我好自己处理啊。
         * 通过设置 [mandatory] 参数可以在[当消息传递过程中不可达目的地]时[将消息返回给生产者]。
         *
         * 发送消息时设置[强制标志]； 仅当提供了[returnCallback]时才适用
         * true：  如果发现消息无法进行路由，会[将该消息返回给生产者]
         * false： 如果发现消息无法进行路由，则[直接丢弃]
         */
        rabbitTemplate.setMandatory(true);
        // 设置[回退消息]交给[谁处理]
        rabbitTemplate.setReturnsCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        // 该回调函数会在交换机接收到消息的时候调用
        String id = correlationData != null ? correlationData.getId() : "";
        if (ack) {
            log.info("交换机已经收到 id 为:{}的消息", id);
        } else {
            log.error("消息 id:{}未成功投递到交换机，原因是:{}", id, cause);
        }
    }

    @Override
    public void returnedMessage(ReturnedMessage returned) {
        log.warn("==> ReturnedMessage:{}", JSON.toJSONString(returned));
        log.info("消息:{}被服务器退回，退回原因:{}, 交换机是:{}, 路由 key:{}",
                 new String(returned.getMessage().getBody(), StandardCharsets.UTF_8),
                 returned.getReplyText(),
                 returned.getExchange(),
                 returned.getRoutingKey());
    }
}
