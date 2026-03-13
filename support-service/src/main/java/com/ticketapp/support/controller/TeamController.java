package com.ticketapp.support.controller;

import com.ticketapp.support.dto.TeamMemberReqDto;
import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponseDto createTeam(@RequestBody TeamRequestDto dto) {
        return teamService.createTeam(dto);
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberResDto addMember(@PathVariable Long teamId, @RequestBody TeamMemberReqDto dto) {
        return teamService.addMember(teamId, dto.getKeycloakUserId());
    }

    @DeleteMapping("/{teamId}/members/{keycloakUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long teamId, @PathVariable String keycloakUserId) {
        teamService.removeMember(teamId, keycloakUserId);
    }

    @GetMapping("/{teamId}/members")
    public List<TeamMemberResDto> getMembersByTeam(@PathVariable Long teamId) {
        return teamService.getMembersByTeam(teamId);
    }

    @GetMapping("/{teamId}/members/{keycloakUserId}/check")
    public boolean isUserInTeam(@PathVariable Long teamId, @PathVariable String keycloakUserId) {
        return teamService.isUserInTeam(teamId, keycloakUserId);
    }
}
