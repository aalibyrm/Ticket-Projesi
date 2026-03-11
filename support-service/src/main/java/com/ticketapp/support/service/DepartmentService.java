package com.ticketapp.support.service;

import com.ticketapp.support.dto.DepartmentRequestDto;
import com.ticketapp.support.dto.DepartmentResponseDto;
import com.ticketapp.support.interfaces.DepartmentMapper;
import com.ticketapp.support.model.Department;
import com.ticketapp.support.model.Topic;
import com.ticketapp.support.repository.DepartmentRepository;
import com.ticketapp.support.repository.TopicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class DepartmentService {

    private final DepartmentRepository departmentRepository;
    private final TopicRepository topicRepository;
    private final DepartmentMapper departmentMapper;

    //Dto düzenle
    public Department findDepartmentByTopic(Long topicId){

        Topic topic = topicRepository.findById(topicId)
                .orElseThrow(() -> new RuntimeException("Topic bulunamadı: " + topicId));

        //Topic zaten deparment tuttuğu için return edilir
        return topic.getDepartment();
    }

    public DepartmentResponseDto createDepartment(DepartmentRequestDto departmentRequestDto){

        if(departmentRepository.existsByName(departmentRequestDto.getName()) ) {
            throw new RuntimeException("Bu departman zaten var!");
        }

        Department department = departmentMapper.departmentDto(departmentRequestDto);
        Department savedDepartment = departmentRepository.save(department);

        return departmentMapper.departmentResponseDto(savedDepartment);


    }


}
