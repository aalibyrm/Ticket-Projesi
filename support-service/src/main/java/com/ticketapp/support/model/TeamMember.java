package com.ticketapp.support.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "team_members",uniqueConstraints = @UniqueConstraint(columnNames = {"team_id", "keycloak_user_id"}))
public class TeamMember {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "team_id",nullable = false)
    private Team team;

    @Column(name = "keycloak_user_id", nullable = false)
    private String keycloakUserId;
}
