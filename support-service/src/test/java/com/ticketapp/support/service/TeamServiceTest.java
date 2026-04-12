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
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

/**
 * TeamService birim testleri.
 */
@ExtendWith(MockitoExtension.class)
class TeamServiceTest {

    @Mock TeamRepository teamRepository;
    @Mock DepartmentRepository departmentRepository;
    @Mock TeamMapper teamMapper;
    @Mock TeamMemberRepository teamMemberRepository;
    @Mock TeamMemberMapper teamMemberMapper;

    @InjectMocks
    TeamService teamService;

    // ── createTeam ────────────────────────────────────────────────────────────

    @Test
    void createTeam_whenNameAlreadyExists_shouldThrowDuplicateResource() {
        TeamRequestDto request = new TeamRequestDto("Alpha", "IT");

        given(teamRepository.existsByName("Alpha")).willReturn(true);

        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void createTeam_whenDepartmentNotFound_shouldThrowDepartmentNotFoundException() {
        TeamRequestDto request = new TeamRequestDto("Beta", "Bilinmeyen");

        given(teamRepository.existsByName("Beta")).willReturn(false);
        given(departmentRepository.findDepartmentByName("Bilinmeyen")).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.createTeam(request))
                .isInstanceOf(DepartmentNotFoundException.class);
    }

    @Test
    void createTeam_whenValid_shouldSaveAndReturnDto() {
        TeamRequestDto request = new TeamRequestDto("Gamma", "IT");
        Department dept = new Department(1L, "IT", List.of(), List.of());
        Team team = new Team(null, "Gamma", dept, null, List.of());
        Team savedTeam = new Team(10L, "Gamma", dept, null, List.of());
        TeamResponseDto expected = new TeamResponseDto(10L, "Gamma", "IT", null);

        given(teamRepository.existsByName("Gamma")).willReturn(false);
        given(departmentRepository.findDepartmentByName("IT")).willReturn(Optional.of(dept));
        given(teamMapper.teamDto(request)).willReturn(team);
        given(teamRepository.save(team)).willReturn(savedTeam);
        given(teamMapper.teamResponseDto(savedTeam)).willReturn(expected);

        TeamResponseDto result = teamService.createTeam(request);

        assertThat(result.getId()).isEqualTo(10L);
        assertThat(result.getName()).isEqualTo("Gamma");
        verify(teamRepository).save(team);
    }

    // ── addMember ─────────────────────────────────────────────────────────────

    @Test
    void addMember_whenTeamNotFound_shouldThrowTeamNotFoundException() {
        given(teamRepository.findById(99L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> teamService.addMember(99L, "user-123"))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void addMember_whenUserAlreadyInTeam_shouldThrowDuplicateResource() {
        Team team = new Team(1L, "Alpha", null, null, List.of());

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndKeycloakUserId(1L, "user-123")).willReturn(true);

        assertThatThrownBy(() -> teamService.addMember(1L, "user-123"))
                .isInstanceOf(DuplicateResourceException.class);
    }

    @Test
    void addMember_whenValid_shouldSaveAndReturnDto() {
        Team team = new Team(1L, "Alpha", null, null, List.of());
        TeamMember savedMember = new TeamMember();
        TeamMemberResDto expected = new TeamMemberResDto();

        given(teamRepository.findById(1L)).willReturn(Optional.of(team));
        given(teamMemberRepository.existsByTeamIdAndKeycloakUserId(1L, "user-123")).willReturn(false);
        given(teamMemberRepository.save(any(TeamMember.class))).willReturn(savedMember);
        given(teamMemberMapper.memberDto(savedMember)).willReturn(expected);

        TeamMemberResDto result = teamService.addMember(1L, "user-123");

        assertThat(result).isEqualTo(expected);
        verify(teamMemberRepository).save(any(TeamMember.class));
    }

    // ── removeMember ──────────────────────────────────────────────────────────

    @Test
    void removeMember_whenUserNotInTeam_shouldThrowTeamNotFoundException() {
        given(teamMemberRepository.existsByTeamIdAndKeycloakUserId(1L, "user-123")).willReturn(false);

        assertThatThrownBy(() -> teamService.removeMember(1L, "user-123"))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void removeMember_whenValid_shouldDeleteMember() {
        TeamMember member = new TeamMember();

        given(teamMemberRepository.existsByTeamIdAndKeycloakUserId(1L, "user-123")).willReturn(true);
        given(teamMemberRepository.findByTeamIdAndKeycloakUserId(1L, "user-123")).willReturn(member);

        teamService.removeMember(1L, "user-123");

        verify(teamMemberRepository).delete(member);
    }

    // ── isUserInTeam ──────────────────────────────────────────────────────────

    @Test
    void isUserInTeam_shouldDelegateToRepository() {
        given(teamMemberRepository.existsByTeamIdAndKeycloakUserId(2L, "user-xyz")).willReturn(true);

        boolean result = teamService.isUserInTeam(2L, "user-xyz");

        assertThat(result).isTrue();
    }

    // ── assignTeam ────────────────────────────────────────────────────────────

    @Test
    void assignTeam_whenNoteamsInDepartment_shouldThrowTeamNotFoundException() {
        given(teamRepository.findTeamsByDepartmentId(1L)).willReturn(List.of());

        assertThatThrownBy(() -> teamService.assignTeam(1L))
                .isInstanceOf(TeamNotFoundException.class);
    }

    @Test
    void assignTeam_whenTeamsExist_shouldReturnOneRandomTeam() {
        Team teamA = new Team(1L, "Alpha", null, null, List.of());
        Team teamB = new Team(2L, "Beta", null, null, List.of());
        TeamResponseDto expectedDto = new TeamResponseDto(1L, "Alpha", "IT", null);

        given(teamRepository.findTeamsByDepartmentId(5L)).willReturn(List.of(teamA, teamB));
        given(teamMapper.teamResponseDto(any(Team.class))).willReturn(expectedDto);

        TeamResponseDto result = teamService.assignTeam(5L);

        // Rastgele seçim: sonuç null değil ve mapper'dan gelmiş olmalı
        assertThat(result).isNotNull();
    }
}
