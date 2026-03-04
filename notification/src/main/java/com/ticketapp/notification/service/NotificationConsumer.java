package com.ticketapp.notification.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.ticketapp.notification.client.UserClient;
import com.ticketapp.notification.dto.TicketEventDto;
import com.ticketapp.notification.dto.UserDto;

@Service
public class NotificationConsumer {

	@Autowired
	UserClient userClient;
	
	@KafkaListener(topics = "ticket-status-topic", groupId = "notification-group")
	public void consumeMessage(TicketEventDto event) {
		
		try {
			
			UserDto user = userClient.getUserById(event.getUserId());
			
			System.out.println("--------------------------------------------------");
            System.out.println("🔔 BİLDİRİM: Sayın " + user.getFirstName() + " " + user.getLastName());
            System.out.println("Mesaj: " + event.getMessage());
            System.out.println("Biletinizin Güncel Durumu: " + event.getStatus());
            System.out.println("--------------------------------------------------");
            
		} catch (Exception e) {
			System.err.println("❌ Feign Hatası: " + e.getMessage());
			System.out.println("Kullanıcı bilgileri çekilemedi, ham veri basılıyor: " + event.getUserId());
		}
		
        
     // Mail atma eklencek
	}
}
