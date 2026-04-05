package edu.diploma.biovectorbot.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import edu.diploma.biovectorbot.entities.UserEntity;

@Repository
public interface BioVectorBotUserRepository extends JpaRepository<UserEntity, Long>{
	
}
