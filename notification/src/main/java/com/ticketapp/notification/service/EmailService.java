package com.ticketapp.notification.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${notification.mail.from:noreply@ticketapp.com}")
    private String fromAddress;

    public void sendTicketStatusEmail(String toEmail, String firstName, String lastName,
                                      String status, String message) {
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setFrom(fromAddress);
            mail.setTo(toEmail);
            mail.setSubject("Ticket Durumu Güncellendi: " + status);
            mail.setText(buildEmailBody(firstName, lastName, status, message));

            mailSender.send(mail);
            log.info("E-posta gonderildi: to={}, status={}", toEmail, status);

        } catch (MailException e) {
            log.error("E-posta gonderilemedi: to={}, hata={}", toEmail, e.getMessage(), e);
        }
    }

    private String buildEmailBody(String firstName, String lastName, String status, String message) {
        return String.format("""
                Sayin %s %s,

                Destek talebinizle ilgili bir güncelleme bulunmaktadir.

                Mesaj   : %s
                Durum   : %s

                Detaylar icin sisteme giris yapabilirsiniz.

                Iyi gunler,
                TicketApp Destek Ekibi
                """, firstName, lastName, message, status);
    }
}
