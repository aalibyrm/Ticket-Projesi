package com.ticketapp.notification.service;

import com.ticketapp.common.dto.TicketEventDto;
import com.ticketapp.common.dto.UserDto;
import com.ticketapp.notification.client.UserClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationConsumer {

	private final UserClient userClient;
	private final EmailService emailService;

	@KafkaListener(topics = "ticket-status-topic", groupId = "notification-group")
	public void consumeMessage(TicketEventDto event) {

		try {
			UserDto user = userClient.getUserById(event.getUserId());

			log.info("BILDIRIM: Sayin {} {}, Mesaj: {}, Biletinizin Guncel Durumu: {}",
					user.getFirstName(), user.getLastName(),
					event.getMessage(), event.getStatus());

			emailService.sendTicketStatusEmail(
					user.getEmail(),
					user.getFirstName(),
					user.getLastName(),
					event.getStatus(),
					event.getMessage()
			);

		} catch (Exception e) {
			log.error("Feign Hatasi - kullanici bilgileri cekilemedi. userId={}, hata={}",
					event.getUserId(), e.getMessage(), e);
			log.warn("Ham veri isleniyor: userId={}, status={}", event.getUserId(), event.getStatus());
		}
	}
}
