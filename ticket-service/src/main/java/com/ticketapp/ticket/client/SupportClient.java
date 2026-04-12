package com.ticketapp.ticket.client;

import com.ticketapp.ticket.client.fallback.SupportClientFallback;
import com.ticketapp.ticket.dto.DepartmentResponseDto;
import com.ticketapp.ticket.dto.TeamResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "support-service", fallback = SupportClientFallback.class)
public interface SupportClient {

    @GetMapping("/api/v1/departments/by-topic/{topicId}")
    DepartmentResponseDto getDepartmentByTopic(@PathVariable Long topicId);

    @GetMapping("/api/v1/teams/{teamId}/members/{userId}/check")
    boolean isUserInTeam(@PathVariable Long teamId, @PathVariable String userId);

    @GetMapping("/api/v1/teams/assign-team/{departmentId}")
    TeamResponseDto assignTeam(@PathVariable Long departmentId);
}
