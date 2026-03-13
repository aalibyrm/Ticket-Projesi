package com.ticketapp.support.controller;

import com.ticketapp.support.dto.TeamMemberReqDto;
import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.service.TeamService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService teamService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponseDto createTeam(@RequestBody TeamRequestDto dto){
        return teamService.createTeam(dto);
    }

    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberResDto addMember(@PathVariable Long teamId,@RequestBody TeamMemberReqDto dto){
        return teamService.addMember(teamId , dto.getKeycloakUserId());
    }
}
