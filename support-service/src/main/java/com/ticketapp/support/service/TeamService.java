package com.ticketapp.support.service;

import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.exception.DepartmentNotFoundException;
import com.ticketapp.support.exception.DuplicateResourceException;
import com.ticketapp.support.exception.TeamNotFoundException;
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
import java.util.concurrent.ThreadLocalRandom;

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
            throw new DuplicateResourceException(
                    "DUPLICATE_TEAM",
                    "Bu team zaten var: " + teamRequestDto.getName()
            );
        }

        Department department = departmentRepository.findDepartmentByName(teamRequestDto.getDepartmentName())
                .orElseThrow(() -> new DepartmentNotFoundException(teamRequestDto.getDepartmentName()));

        Team team = teamMapper.teamDto(teamRequestDto);
        team.setDepartment(department);
        Team savedTeam = teamRepository.save(team);

        return teamMapper.teamResponseDto(savedTeam);
    }

    public TeamMemberResDto addMember(Long teamId, String keycloakUserId) {
        Team team = teamRepository.findById(teamId)
                .orElseThrow(() -> new TeamNotFoundException(teamId.toString()));

        if (teamMemberRepository.existsByTeamIdAndKeycloakUserId(teamId, keycloakUserId)) {
            throw new DuplicateResourceException(
                    "DUPLICATE_TEAM_MEMBER",
                    "Bu kullanici zaten ekipte: " + keycloakUserId
            );
        }

        TeamMember teamMember = new TeamMember();
        teamMember.setTeam(team);
        teamMember.setKeycloakUserId(keycloakUserId);

        TeamMember saved = teamMemberRepository.save(teamMember);

        return teamMemberMapper.memberDto(saved);
    }

    public void removeMember(Long teamId, String keycloakUserId) {
        if (!teamMemberRepository.existsByTeamIdAndKeycloakUserId(teamId, keycloakUserId)) {
            throw new TeamNotFoundException(
                    "Kullanici bu ekipte bulunamadi: " + keycloakUserId + " (team: " + teamId + ")"
            );
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

    public TeamResponseDto assignTeam(Long departmentId){
        List<Team> teams = teamRepository.findTeamsByDepartmentId(departmentId);
        if (teams.isEmpty()) {
            throw new TeamNotFoundException("Bu departmanda ekip bulunamadi: " + departmentId);
        }

        int randomIndex = ThreadLocalRandom.current().nextInt(teams.size());
        Team team = teams.get(randomIndex);

        return teamMapper.teamResponseDto(team);
    }
}

