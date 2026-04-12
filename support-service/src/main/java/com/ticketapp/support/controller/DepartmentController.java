package com.ticketapp.support.controller;

import com.ticketapp.support.dto.*;
import com.ticketapp.support.service.DepartmentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponseDto createDepartment(@Valid @RequestBody DepartmentRequestDto dto) {
        return departmentService.createDepartment(dto);
    }

    @PostMapping("/{departmentId}/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponseDto addTopicToDepartment(@PathVariable Long departmentId, @Valid @RequestBody TopicRequestDto topicDto) {
        return departmentService.addTopicToDepartment(departmentId, topicDto);
    }

    @GetMapping
    public List <DepartmentResponseDto> listAllDepartments(){
        return departmentService.listAllDepartments();
    }

    @GetMapping("/by-topic/{topicId}")
    public DepartmentResponseDto getDepartmentByTopic(@PathVariable Long topicId){
        return departmentService.findDepartmentByTopic(topicId);
    }

    @GetMapping("/{departmentId}/teams")
    public List<TeamResponseDto> getTeamsByDepartment(@PathVariable Long departmentId){
        return departmentService.getTeamsByDepartment(departmentId);
    }
}
