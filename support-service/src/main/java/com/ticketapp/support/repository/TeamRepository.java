package com.ticketapp.support.repository;

import com.ticketapp.support.model.Team;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TeamRepository extends JpaRepository<Team,Long> {
    List <Team> findTeamsByDepartmentId (Long departmentId);
}
