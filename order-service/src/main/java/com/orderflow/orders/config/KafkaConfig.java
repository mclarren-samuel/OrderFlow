package com.orderflow.orders.config;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.serializer.JacksonJsonSerializer;
import tools.jackson.databind.ObjectMapper;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class KafkaConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Bean
    public ProducerFactory<String, Object> producerFactory(){
        Map<String, Object> properties = new HashMap<>();
        properties.put(
                ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );

        properties.put(
                ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
                StringSerializer.class
        );

        properties.put(
                ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
                JacksonJsonSerializer.class
        );

        properties.put(
                JacksonJsonSerializer.ADD_TYPE_INFO_HEADERS,
                false
        );

        return new DefaultKafkaProducerFactory<String, Object>(
                properties,
                new StringSerializer(),
                new JacksonJsonSerializer<Object>()
        );
    }

    @Bean
    public ConsumerFactory<String, String>
    consumerFactory(){
        Map<String, Object> properties = new HashMap<>();

        properties.put(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG,
                bootstrapServers
        );
        properties.put(
                ConsumerConfig.GROUP_ID_CONFIG,
                "order-service"
        );

        properties.put(
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG,
                "earliest"
        );

        properties.put(
                ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG,
                false
        );

        return new DefaultKafkaConsumerFactory<>(
                properties,
                new StringDeserializer(),
                new StringDeserializer()
        );
    }
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(
            ProducerFactory<String, Object> producerFactory
    ) {
        return new KafkaTemplate<>(producerFactory);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, String>
    kafkaListenerContainerFactory(
            ConsumerFactory<String, String> consumerFactory
    ) {
        var factory =
                new ConcurrentKafkaListenerContainerFactory<String, String>();

        factory.setConsumerFactory(consumerFactory);

        factory.getContainerProperties().setAckMode(
                ContainerProperties.AckMode.RECORD
        );

        return factory;
    }
}
