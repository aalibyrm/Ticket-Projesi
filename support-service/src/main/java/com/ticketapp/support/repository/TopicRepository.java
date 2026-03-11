package com.ticketapp.support.repository;

import com.ticketapp.support.model.Topic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TopicRepository extends JpaRepository<Topic,Long> {
    Topic findTopicByName(String name);
}
