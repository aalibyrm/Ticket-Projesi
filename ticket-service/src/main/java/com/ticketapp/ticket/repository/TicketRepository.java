package com.ticketapp.ticket.repository;

import java.util.List;

import com.ticketapp.ticket.model.TicketStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ticketapp.ticket.model.Ticket;

@Repository
public interface TicketRepository extends JpaRepository<Ticket , String> {
	List<Ticket> findByUserId(String userId);

	long countByAssigneeIdAndStatus(String assigneeId, TicketStatus status); //Çözdüğü ticket sayısı bulmak için

	@Query(value = """
    SELECT AVG(
        (EXTRACT(EPOCH FROM (resolved_at - sla_started_at)) * 1000) - sla_total_paused_millis
    )
    FROM tickets
    WHERE assignee_id = :assigneeId
    AND resolved_at IS NOT NULL
    AND sla_started_at IS NOT NULL
    """, nativeQuery = true)
	Double findAverageResolutionTimeByAssigneeId(@Param("assigneeId") String assigneeId); //Agent'ın ticket çözümleme ortalamasını getirir
}
