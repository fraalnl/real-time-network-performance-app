package com.ericsson.repository;

import com.ericsson.model.EngineerEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<EngineerEntity, Long> {
	Optional<EngineerEntity> findByUsername(String username);
}

