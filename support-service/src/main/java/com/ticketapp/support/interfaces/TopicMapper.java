package com.ticketapp.support.interfaces;

import com.ticketapp.support.dto.TopicRequestDto;
import com.ticketapp.support.dto.TopicResponseDto;
import com.ticketapp.support.model.Topic;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface TopicMapper {
    Topic topicDto(TopicRequestDto topicRequestDto);
    TopicResponseDto topicResponseDto(Topic topic);
}
