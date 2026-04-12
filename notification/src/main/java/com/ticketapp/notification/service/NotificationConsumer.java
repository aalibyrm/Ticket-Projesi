package com.ticketapp.notification.service;

import com.ticketapp.notification.client.UserClient;
import com.ticketapp.notification.dto.TicketEventDto;
import com.ticketapp.notification.dto.UserDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

	private final UserClient userClient;

	@KafkaListener(topics = "ticket-status-topic", groupId = "notification-group")
	public void consumeMessage(TicketEventDto event) {

		try {
			UserDto user = userClient.getUserById(event.getUserId());

			log.info("BILDIRIM: Sayin {} {}, Mesaj: {}, Biletinizin Guncel Durumu: {}",
					user.getFirstName(), user.getLastName(),
					event.getMessage(), event.getStatus());

		} catch (Exception e) {
			log.error("Feign Hatasi - kullanici bilgileri cekilemedi. userId={}, hata={}",
					event.getUserId(), e.getMessage(), e);
			log.warn("Ham veri isleniyor: userId={}, status={}", event.getUserId(), event.getStatus());
		}

		// Mail atma eklenecek
	}
}
