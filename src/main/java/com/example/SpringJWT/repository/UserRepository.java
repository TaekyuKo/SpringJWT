package com.example.SpringJWT.repository;

import com.example.SpringJWT.entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Integer> {
    //existsBy 구문으로 Username이 이미 존재하는지 확인
    boolean existsByUsername(String username);

    UserEntity findByUsername(String username);

}
