package com.ticketapp.support.repository;

import com.ticketapp.support.model.TeamMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface TeamMemberRepository extends JpaRepository<TeamMember,Long> {
    boolean existsByTeamIdAndKeycloakUserId(Long teamId, String keycloakUserId);
    TeamMember findByTeamIdAndKeycloakUserId(Long teamId, String keycloakUserId);
    List<TeamMember> findByTeamId(Long teamId);
}
