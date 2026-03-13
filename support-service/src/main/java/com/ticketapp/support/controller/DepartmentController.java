package com.ticketapp.support.controller;

import com.ticketapp.support.dto.DepartmentRequestDto;
import com.ticketapp.support.dto.DepartmentResponseDto;
import com.ticketapp.support.dto.TopicRequestDto;
import com.ticketapp.support.dto.TopicResponseDto;
import com.ticketapp.support.service.DepartmentService;
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
    public DepartmentResponseDto createDepartment(@RequestBody DepartmentRequestDto dto){
        return departmentService.createDepartment(dto);
    }

    @PostMapping("/{departmentId}/topics")
    @ResponseStatus(HttpStatus.CREATED)
    public TopicResponseDto addTopicToDepartment(@PathVariable Long departmentId,@RequestBody TopicRequestDto topicDto){
        return departmentService.addTopicToDepartment(departmentId,topicDto);
    }

    @GetMapping
    public List <DepartmentResponseDto> listAllDepartments(){
        return departmentService.listAllDepartments();
    }

    @GetMapping("/by-topic/{topicId}")
    public DepartmentResponseDto getDepartmentByTopic(@PathVariable Long topicId){
        return departmentService.findDepartmentByTopic(topicId);
    }
}
