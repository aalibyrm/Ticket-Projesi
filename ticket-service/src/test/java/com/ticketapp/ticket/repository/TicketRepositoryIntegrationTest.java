package com.ticketapp.ticket.repository;

import com.ticketapp.ticket.model.Ticket;
import com.ticketapp.ticket.model.TicketPriority;
import com.ticketapp.ticket.model.TicketStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TicketRepository entegrasyon testi.
 *
 * Öğrenilen kavramlar:
 *  - @DataJpaTest: Sadece JPA katmanını yükler (Controller, Service, Kafka, Zeebe vs. yüklenmez)
 *  - @AutoConfigureTestDatabase(replace = NONE): H2 yerine gerçek DB kullan
 *  - @Testcontainers: JUnit 5 Testcontainers uzantısı
 *  - @Container: Testcontainers container tanımı (static = tüm testler için tek container)
 *  - @ServiceConnection: Spring Boot 3.1+ özelliği - container'ı datasource olarak otomatik bağlar
 *
 * Neden Testcontainers?
 *  - H2 (gömülü DB) PostgreSQL'in tüm özelliklerini desteklemez (örn: UUID generation, native queries)
 *  - Testcontainers ile üretim ortamıyla birebir aynı PostgreSQL versiyonu kullanılır
 *  - Test sonrası container otomatik silinir, temiz ortam garantisi
 */
// replace=NONE: H2 yerine Testcontainers PostgreSQL kullanılacak
// ddl-auto=create-drop: Her test çalışmasında schema oluştur, sonra sil
@DataJpaTest(properties = {"spring.jpa.hibernate.ddl-auto=create-drop"})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class TicketRepositoryIntegrationTest {

    // Tüm test sınıfı için tek bir container başlatılır (static)
    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    @Autowired
    TicketRepository ticketRepository;

    @BeforeEach
    void setUp() {
        ticketRepository.deleteAll();
    }

    // ── findByUserId ──────────────────────────────────────────────────────────

    @Test
    void findByUserId_shouldReturnOnlyUserTickets() {
        // given
        Ticket t1 = buildTicket("user-A", TicketStatus.NEW, TicketPriority.HIGH);
        Ticket t2 = buildTicket("user-A", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM);
        Ticket t3 = buildTicket("user-B", TicketStatus.NEW, TicketPriority.LOW);

        ticketRepository.saveAll(List.of(t1, t2, t3));

        // when
        List<Ticket> result = ticketRepository.findByUserId("user-A");

        // then
        assertThat(result).hasSize(2);
        assertThat(result).allMatch(t -> t.getUserId().equals("user-A"));
    }

    // ── countByAssigneeId ─────────────────────────────────────────────────────

    @Test
    void countByAssigneeId_shouldReturnCorrectCount() {
        Ticket t1 = buildTicket("user-1", TicketStatus.IN_PROGRESS, TicketPriority.HIGH);
        t1.setAssigneeId("agent-X");
        Ticket t2 = buildTicket("user-2", TicketStatus.NEW, TicketPriority.MEDIUM);
        t2.setAssigneeId("agent-X");
        Ticket t3 = buildTicket("user-3", TicketStatus.NEW, TicketPriority.LOW);
        t3.setAssigneeId("agent-Y");

        ticketRepository.saveAll(List.of(t1, t2, t3));

        assertThat(ticketRepository.countByAssigneeId("agent-X")).isEqualTo(2);
        assertThat(ticketRepository.countByAssigneeId("agent-Y")).isEqualTo(1);
    }

    // ── countByAssigneeIdAndStatus ─────────────────────────────────────────────

    @Test
    void countByAssigneeIdAndStatus_shouldFilterByStatus() {
        Ticket t1 = buildTicket("user-1", TicketStatus.RESOLVED, TicketPriority.HIGH);
        t1.setAssigneeId("agent-X");
        Ticket t2 = buildTicket("user-2", TicketStatus.IN_PROGRESS, TicketPriority.MEDIUM);
        t2.setAssigneeId("agent-X");

        ticketRepository.saveAll(List.of(t1, t2));

        long resolvedCount = ticketRepository.countByAssigneeIdAndStatus("agent-X", TicketStatus.RESOLVED);
        assertThat(resolvedCount).isEqualTo(1);
    }

    // ── save & findById ───────────────────────────────────────────────────────

    @Test
    void saveTicket_shouldPersistAndGenerateUUID() {
        Ticket ticket = buildTicket("user-uuid-test", TicketStatus.NEW, TicketPriority.HIGH);

        Ticket saved = ticketRepository.save(ticket);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getId()).isNotBlank();   // UUID otomatik atanmış olmalı

        Ticket fromDb = ticketRepository.findById(saved.getId()).orElseThrow();
        assertThat(fromDb.getTitle()).isEqualTo("Test Ticket");
        assertThat(fromDb.getPriority()).isEqualTo(TicketPriority.HIGH);
    }

    // ── findAverageResolutionTime (native query) ───────────────────────────────

    @Test
    void findAverageResolutionTime_whenNoResolvedTickets_shouldReturnNull() {
        // Çözülmüş ticket yok
        Ticket t = buildTicket("user-1", TicketStatus.IN_PROGRESS, TicketPriority.HIGH);
        t.setAssigneeId("agent-Z");
        ticketRepository.save(t);

        Double avg = ticketRepository.findAverageResolutionTimeByAssigneeId("agent-Z");

        // resolved_at NULL olduğundan WHERE koşulu eşleşmez → null döner
        assertThat(avg).isNull();
    }

    @Test
    void findAverageResolutionTime_whenTicketsResolved_shouldReturnPositiveValue() {
        Ticket t = buildTicket("user-1", TicketStatus.RESOLVED, TicketPriority.HIGH);
        t.setAssigneeId("agent-W");
        t.setSlaStartedAt(LocalDateTime.now().minusHours(4));
        t.setResolvedAt(LocalDateTime.now());
        t.setSlaTotalPausedMillis(0L);
        ticketRepository.save(t);

        Double avg = ticketRepository.findAverageResolutionTimeByAssigneeId("agent-W");

        assertThat(avg).isNotNull();
        assertThat(avg).isGreaterThan(0);
    }

    // ── Yardımcı metot ────────────────────────────────────────────────────────

    private Ticket buildTicket(String userId, TicketStatus status, TicketPriority priority) {
        Ticket ticket = new Ticket();
        ticket.setTitle("Test Ticket");
        ticket.setDescription("Test açıklaması");
        ticket.setUserId(userId);
        ticket.setStatus(status);
        ticket.setPriority(priority);
        ticket.setCreatedDate(LocalDateTime.now());
        ticket.setSlaTotalPausedMillis(0L);
        return ticket;
    }
}
