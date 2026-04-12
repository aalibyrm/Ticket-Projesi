package com.ticketapp.ticket.service;

import com.ticketapp.ticket.dto.TicketEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TicketProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String TOPIC = "ticket-status-topic";

	public void sendMessage(TicketEventDto event) {
		log.info("Kafka'ya mesaj gonderiliyor: topic={}, event={}", TOPIC, event);
		kafkaTemplate.send(TOPIC, event);
	}
}
