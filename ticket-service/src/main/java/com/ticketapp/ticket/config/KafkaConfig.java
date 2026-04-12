package com.ticketapp.ticket.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;

@Configuration
public class KafkaConfig {

    /**
     * Spring Boot'un auto-configured ProducerFactory'sini alıp observation desteğini
     * açıkça etkinleştiriyoruz. bootstrap-servers, serializer ve diğer producer
     * ayarları application.yaml'daki spring.kafka.producer.* bloğundan okunur.
     * Bu sayede spring.kafka.template.observation-enabled=true da geçerli olur.
     */
    @Bean
    public KafkaTemplate<String, Object> kafkaTemplate(ProducerFactory<String, Object> producerFactory) {
        KafkaTemplate<String, Object> template = new KafkaTemplate<>(producerFactory);
        template.setObservationEnabled(true);
        return template;
    }
}
