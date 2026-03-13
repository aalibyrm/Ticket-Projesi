package com.ticketapp.support.service;

import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.interfaces.TeamMapper;
import com.ticketapp.support.model.Department;
import com.ticketapp.support.model.Team;
import com.ticketapp.support.repository.DepartmentRepository;
import com.ticketapp.support.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamMapper teamMapper;

    public TeamResponseDto createTeam(TeamRequestDto teamRequestDto){

        if (teamRepository.existsByName(teamRequestDto.getName())) {
            throw new RuntimeException("Bu team zaten var!");
        }

        Department department = departmentRepository.findDepartmentByName(teamRequestDto.getDepartmentName())
                .orElseThrow(() -> new RuntimeException("Department bulunamadı"));

        Team team = teamMapper.teamDto(teamRequestDto);
        team.setDepartment(department);
        Team savedTeam = teamRepository.save(team);

        return teamMapper.teamResponseDto(savedTeam);
    }

}

