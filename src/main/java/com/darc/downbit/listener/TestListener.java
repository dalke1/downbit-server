package com.darc.downbit.listener;

import org.springframework.stereotype.Component;

/**
 * @author darc
 * @version 0.1
 * @createDate 2024/11/18-21:37:16
 * @description
 */
@Component
public class TestListener {

//    /**
//     * 监听test主题的消息
//     * 方法参数:String或者ConsumerRecord
//     * 前者返回消息的值,后者返回消息的所有内容
//     */
//    @KafkaListener(topics = "test", groupId = "testConsumer")
//    public void testListen(ConsumerRecord<String, String> message) {
//        //打印消息的key和value
//        System.out.println("key:" + message.key() + " value:" + message.value());
//    }
//
//    /**
//     * 监听test主题0分区的所有消息
//     */
//    @KafkaListener(topicPartitions = {
//            @TopicPartition(topic = "test", partitionOffsets = {
//                    @PartitionOffset(partition = "0", initialOffset = "0")
//            })
//    }, groupId = "testConsumerGetAll")
//    public void testListen2(ConsumerRecord<String, String> message) {
//        //打印消息的key和value
//        System.out.println("key:" + message.key() + " value:" + message.value());
//    }
}
