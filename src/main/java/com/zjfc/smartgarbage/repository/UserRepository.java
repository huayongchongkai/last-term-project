package com.zjfc.smartgarbage.repository;

import com.zjfc.smartgarbage.model.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

    Optional<User> findByStudentId(String studentId);

    Optional<User> findByPhone(String phone);

    Optional<User> findByUsername(String username);

    boolean existsByStudentId(String studentId);
}