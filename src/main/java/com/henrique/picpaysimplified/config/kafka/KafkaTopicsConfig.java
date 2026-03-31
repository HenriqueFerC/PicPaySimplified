package com.henrique.picpaysimplified.config.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicsConfig {

    @Bean
    public NewTopic mailSenderTopic() {
        return TopicBuilder.name("mail-sender")
                .partitions(1)
                .replicas(1)
                .build();
    }

}
