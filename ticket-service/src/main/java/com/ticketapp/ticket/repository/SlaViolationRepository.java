package com.ticketapp.ticket.repository;

import com.ticketapp.ticket.model.SlaViolationRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface SlaViolationRepository extends JpaRepository<SlaViolationRecord,String> {
    long countByViolatingAgentId(String agentId); // Bir agent'ın toplam kaç SLA ihlali yaptığını sayar.
    long countByViolatingTeamId(Long teamId); // Bir ekibin toplam ihlal sayısını sayar.

    List<SlaViolationRecord> findByViolatingAgentIdAndViolatedAtBetween(String agentId,
                                                                        LocalDateTime from, LocalDateTime to);
    //Belirli tarih aralığında bir agent'ın ihlallerini getirir. "Bu ay / bu hafta kaç ihlal var?" sorusunu yanıtlar.
}
