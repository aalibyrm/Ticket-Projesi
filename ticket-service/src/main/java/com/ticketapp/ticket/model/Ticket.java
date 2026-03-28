package com.ticketapp.ticket.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Entity
@Table(name = "tickets")

public class Ticket {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	
	@Column(nullable = false)
	private String title;
	
	@Column(length = 4000)
    private String description;
	
	@Enumerated(EnumType.STRING)
    private TicketStatus status;
	
	@Enumerated(EnumType.STRING)
    private TicketPriority priority;

    @Column(name = "created_date")
    private LocalDateTime createdDate;
	
	@Column(name = "user_id")
	private String userId;
	
	@Column(name = "assignee_id")
	private String assigneeId;

	@OneToMany(mappedBy = "ticket")
	private List<Comment> comments = new ArrayList<>();

	@Column(name = "department_id")
	private Long departmentId;

	@Column(name = "team_id")
	private Long teamId;

	@Column(name = "sla_started_at")
	private LocalDateTime slaStartedAt;

	@Column(name="sla_paused_at")
	private LocalDateTime slaPausedAt;

	@Column(name="sla_total_paused_millis")
	private Long slaTotalPausedMillis = 0L;

	@Column(name= "sla_breached")
	private boolean slaBreached = false;

	@Column(name="sla_breached_at")
	private LocalDateTime slaBreachedAt;

	@Column(name="resolved_at")
	private LocalDateTime resolvedAt;

	@Column(name="closed_at")
	private LocalDateTime closedAt;

}
