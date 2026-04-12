package com.ticketapp.support.controller;

import com.ticketapp.support.dto.*;
import com.ticketapp.support.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Departman Yönetimi", description = "Departman ve konu (topic) oluşturma ve listeleme işlemleri")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/departments")
public class DepartmentController {
    private final DepartmentService departmentService;

    @Operation(summary = "Departman oluştur")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public DepartmentResponseDto createDepartment(@Valid @RequestBody DepartmentRequestDto dto) {
        return departmentService.createDepartment(dto);
    }

    @Operation(summary = "Departmana konu ekle")
    @PostMapping("/{departmentId}/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponseDto addTopicToDepartment(@PathVariable Long departmentId, @Valid @RequestBody TopicRequestDto topicDto) {
        return departmentService.addTopicToDepartment(departmentId, topicDto);
    }

    @Operation(summary = "Tüm departmanları listele")
    @GetMapping
    public List <DepartmentResponseDto> listAllDepartments(){
        return departmentService.listAllDepartments();
    }

    @Operation(summary = "Topic'e göre departman bul")
    @GetMapping("/by-topic/{topicId}")
    public DepartmentResponseDto getDepartmentByTopic(@PathVariable Long topicId){
        return departmentService.findDepartmentByTopic(topicId);
    }

    @Operation(summary = "Departmanın ekiplerini listele")
    @GetMapping("/{departmentId}/teams")
    public List<TeamResponseDto> getTeamsByDepartment(@PathVariable Long departmentId){
        return departmentService.getTeamsByDepartment(departmentId);
    }
}
