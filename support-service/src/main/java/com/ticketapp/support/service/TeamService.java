package com.ticketapp.support.service;

import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.interfaces.TeamMapper;
import com.ticketapp.support.interfaces.TeamMemberMapper;
import com.ticketapp.support.model.Department;
import com.ticketapp.support.model.Team;
import com.ticketapp.support.model.TeamMember;
import com.ticketapp.support.repository.DepartmentRepository;
import com.ticketapp.support.repository.TeamMemberRepository;
import com.ticketapp.support.repository.TeamRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class TeamService {

    private final TeamRepository teamRepository;
    private final DepartmentRepository departmentRepository;
    private final TeamMapper teamMapper;
    private final TeamMemberRepository teamMemberRepository;
    private final TeamMemberMapper teamMemberMapper;

    public TeamResponseDto createTeam(TeamRequestDto teamRequestDto) {

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

    public TeamMemberResDto addMember(Long teamId, String keycloakUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new RuntimeException("Team bulunamadı!"));

        if (teamMemberRepository.existsByTeamIdAndKeycloakUserId(teamId, keycloakUserId)) {
            throw new RuntimeException("Bu kullanıcı zaten ekipte!");
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setKeycloakUserId(keycloakUserId);

        TeamMember saved = teamMemberRepository.save(teamMember);

        return teamMemberMapper.memberDto(saved);
    }

    public void removeMember(Long teamId, String keycloakUserId) {
        if (!teamMemberRepository.existsByTeamIdAndKeycloakUserId(teamId, keycloakUserId)) {
            throw new RuntimeException("Kullanıcı bu ekipten değil!");
        }

        TeamMember teamMember = teamMemberRepository.findByTeamIdAndKeycloakUserId(teamId, keycloakUserId);
        teamMemberRepository.delete(teamMember);
    }

    public List<TeamMemberResDto> getMembersByTeam(Long teamId) {
        List<TeamMember> memberList = teamMemberRepository.findByTeamId(teamId);

        return teamMemberMapper.toResDto(memberList);
    }

    public boolean isUserInTeam(Long teamId, String keycloakUserId) {
        return teamMemberRepository.existsByTeamIdAndKeycloakUserId(teamId, keycloakUserId);
    }
}

