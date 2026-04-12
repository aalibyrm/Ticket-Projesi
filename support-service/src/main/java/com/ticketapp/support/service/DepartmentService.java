package com.ticketapp.support.service;

import com.ticketapp.support.dto.*;
import com.ticketapp.support.exception.DepartmentNotFoundException;
import com.ticketapp.support.exception.DuplicateResourceException;
import com.ticketapp.support.exception.TopicNotFoundException;
import com.ticketapp.support.interfaces.DepartmentMapper;
import com.ticketapp.support.interfaces.TeamMapper;
import com.ticketapp.support.interfaces.TopicMapper;
import com.ticketapp.support.model.Department;
import com.ticketapp.support.model.Team;
import com.ticketapp.support.model.Topic;
import com.ticketapp.support.repository.DepartmentRepository;
import com.ticketapp.support.repository.TeamRepository;
import com.ticketapp.support.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TopicRepository topicRepository;
    private final DepartmentMapper departmentMapper;
    private final TopicMapper topicMapper;
    private final TeamRepository teamRepository;
    private final TeamMapper teamMapper;

    public DepartmentResponseDto findDepartmentByTopic(Long topicId) {

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new TopicNotFoundException(topicId.toString()));

        // Topic zaten department tuttuğu için return edilir
        return departmentMapper.departmentResponseDto(topic.getDepartment());
    }

    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto) {

        if (departmentRepository.existsByName(departmentRequestDto.getName())) {
            throw new DuplicateResourceException(
                    "DUPLICATE_DEPARTMENT",
                    "Bu departman zaten var: " + departmentRequestDto.getName()
            );
        }

        Department department = departmentMapper.departmentDto(departmentRequestDto);
        Department savedDepartment = departmentRepository.save(department);

        return departmentMapper.departmentResponseDto(savedDepartment);
    }

    public TopicResponseDto addTopicToDepartment(Long departmentId, TopicRequestDto topicRequestDto) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new DepartmentNotFoundException(departmentId.toString()));

        if (department.getTopicList().stream()
                .anyMatch(t -> t.getName().equals(topicRequestDto.getName()))) {
            throw new DuplicateResourceException(
                    "DUPLICATE_TOPIC",
                    "Departman zaten bu topic'e sahip: " + topicRequestDto.getName()
            );
        }

        Topic topic = topicMapper.topicDto(topicRequestDto);
        topic.setDepartment(department);
        Topic savedTopic = topicRepository.save(topic);

        return topicMapper.topicResponseDto(savedTopic);
    }

    public List<DepartmentResponseDto> listAllDepartments() {
        return departmentMapper.toResponseDtoList(departmentRepository.findAll());
    }

    public List<TeamResponseDto> getTeamsByDepartment(Long departmentId) {
        List<Team> teams = teamRepository.findTeamsByDepartmentId(departmentId);
        return teamMapper.toResponseDto(teams);
    }
}
