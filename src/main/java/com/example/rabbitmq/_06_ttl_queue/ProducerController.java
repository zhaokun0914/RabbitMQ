package com.example.rabbitmq._06_ttl_queue;

import com.example.rabbitmq.config.DelayedQueueConfig;
import com.example.rabbitmq.config.MsgTtlQueueConfig;
import com.example.rabbitmq.config.TtlQueueConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kavin
 * @date 2021-09-23 15:46:46
 */
@Slf4j
@RestController
@RequestMapping("ttl")
public class ProducerController {

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @RequestMapping("sendMsg/{message}")
    public void producer(@PathVariable String message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info("==> 当前时间：{}，发送一条信息给两个TTL队列：{}", format, message);
        rabbitTemplate.convertAndSend(TtlQueueConfig.X_EXCHANGE, TtlQueueConfig.QUEUE_A_ROUTING_KEY, "消息发送给 TTL 为10s的队列" + message);
        rabbitTemplate.convertAndSend(TtlQueueConfig.X_EXCHANGE, TtlQueueConfig.QUEUE_B_ROUTING_KEY, "消息发送给 TTL 为40s的队列" + message);
    }

    /**
     * 看起来似乎没什么问题，但是在TtlQueueConfig中介绍过，如果使用[在消息属性上设置TTL]的方式，消息可能并不会按时“死亡“，
     * 因为RabbitMQ只会[检查第一个消息是否过期]，如果过期则丢到死信队列，如果第一个消息的延时时长很长，而第二个消息的延时时长很短，第二个消息并不会优先得到执行。
     *
     * @param message 要发送的消息
     * @param ttlTime 设置的TTL时间
     */
    @GetMapping("sendExpirationMsg/{message}/{ttlTime}")
    public void sendMsg(@PathVariable String message, @PathVariable String ttlTime) {
        rabbitTemplate.convertAndSend(TtlQueueConfig.X_EXCHANGE,
                                      MsgTtlQueueConfig.QUEUE_C_ROUTING_KEY,
                                      message,
                                      correlationData -> {
                                          // 关联数据，通过此对象设置该消息的一些属性
                                          correlationData.getMessageProperties()
                                                         .setExpiration(ttlTime);
                                          return correlationData;
                                      });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info("==> 当前时间：{}，发送一条信息给两个TTL队列：{}", format, message);
    }

    /**
     * 使用 rabbitmq_delayed_message_exchange 插件来解决上面的问题
     * 下载地址：https://www.rabbitmq.com/community-plugins.html
     *
     * @param message   要发送的消息
     * @param delayTime 设置的TTL时间
     */
    @GetMapping("sendDelayMsg/{message}/{delayTime}")
    public void sendMsgDelayMsg(@PathVariable String message, @PathVariable Integer delayTime) {
        rabbitTemplate.convertAndSend(DelayedQueueConfig.DELAYED_EXCHANGE_NAME,
                                      DelayedQueueConfig.DELAYED_ROUTING_KEY,
                                      message,
                                      correlationData -> {
                                          correlationData.getMessageProperties().setDelay(delayTime);
                                          return correlationData;
                                      });

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss:SSS");
        String format = dateFormat.format(new Date());
        log.info(" 当前时间：{}, 发送一条延迟{}毫秒的信息给队列 delayed.queue:{}", format, delayTime, message);
    }
}
