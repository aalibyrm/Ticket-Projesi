package com.ticketapp.ticket.service;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.ticketapp.ticket.dto.TicketEventDto;

@Service
@RequiredArgsConstructor
public class TicketProducer {

	private final KafkaTemplate<String, Object> kafkaTemplate;

	private static final String TOPIC = "ticket-status-topic";

	public void sendMessage(TicketEventDto event) {
		System.out.println("Kafka'ya mesaj gönderiliyor: " + event);
		kafkaTemplate.send(TOPIC, event);
	}
}
