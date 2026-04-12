package com.ticketapp.support.controller;

import com.ticketapp.support.dto.TeamMemberReqDto;
import com.ticketapp.support.dto.TeamMemberResDto;
import com.ticketapp.support.dto.TeamRequestDto;
import com.ticketapp.support.dto.TeamResponseDto;
import com.ticketapp.support.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(name = "Ekip Yönetimi", description = "Destek ekiplerinin oluşturulması ve üye yönetimi")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/teams")
public class TeamController {
    private final TeamService teamService;

    @Operation(summary = "Ekip oluştur")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TeamResponseDto createTeam(@Valid @RequestBody TeamRequestDto dto) {
        return teamService.createTeam(dto);
    }

    @Operation(summary = "Ekibe üye ekle")
    @PostMapping("/{teamId}/members")
    @ResponseStatus(HttpStatus.CREATED)
    public TeamMemberResDto addMember(@PathVariable Long teamId, @Valid @RequestBody TeamMemberReqDto dto) {
        return teamService.addMember(teamId, dto.getKeycloakUserId());
    }

    @Operation(summary = "Ekipten üye çıkar")
    @DeleteMapping("/{teamId}/members/{keycloakUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeMember(@PathVariable Long teamId, @PathVariable String keycloakUserId) {
        teamService.removeMember(teamId, keycloakUserId);
    }

    @Operation(summary = "Ekip üyelerini listele")
    @GetMapping("/{teamId}/members")
    public List<TeamMemberResDto> getMembersByTeam(@PathVariable Long teamId) {
        return teamService.getMembersByTeam(teamId);
    }

    @Operation(summary = "Kullanıcı ekipte mi kontrol et")
    @GetMapping("/{teamId}/members/{keycloakUserId}/check")
    public boolean isUserInTeam(@PathVariable Long teamId, @PathVariable String keycloakUserId) {
        return teamService.isUserInTeam(teamId, keycloakUserId);
    }

    @Operation(summary = "Departmana ekip ata", description = "Belirtilen departmana uygun bir ekip döner.")
    @GetMapping("/assign-team/{departmentId}")
    public TeamResponseDto assignTeam(@PathVariable Long departmentId){
        return teamService.assignTeam(departmentId);
    }
}
