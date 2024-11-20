package com.example.userservice.repository;

import com.example.userservice.model.AccessToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccessTokenRepository extends JpaRepository<AccessToken, Long> {
    Optional<AccessToken> findByToken(String token);

    void deleteAllByUserId(Long userId);
}
