package com.ticketapp.ticket.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "sla_violations")
public class SlaViolationRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;

    @Column(name="ticket_id")
    private String ticketId;

    @Column(name="violating_agent_id")
    private String violatingAgentId;

    @Column(name="violating_team_id")
    private Long violatingTeamId;

    @Column(name="priority")
    private String priority;

    @Column(name="violated_at")
    private LocalDateTime violatedAt;

    @Column(name="elapsed_millis")
    private Long elapsedMillis; // ihlal aninda gecen sure
}
