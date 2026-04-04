package edu.diploma.biovectorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.diploma.biovectorbot.entities.TaskEntity;

@Repository
public interface BioVectorBotTaskRepository extends JpaRepository<TaskEntity, Long>{
	
	TaskEntity findByNumber(Integer number);
}
