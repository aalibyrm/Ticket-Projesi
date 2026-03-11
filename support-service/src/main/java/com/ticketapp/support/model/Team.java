package com.ticketapp.support.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "teams")
public class Team {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @ManyToOne
    @JoinColumn(name = "department_id",nullable = false)
    private Department department;

    @Column(name = "leader_id")
    private String leaderId;

    @OneToMany(mappedBy = "team")
    private List<TeamMember> memberList = new ArrayList<>();
}
