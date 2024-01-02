package com.example.prj3be.repository;

import com.example.prj3be.domain.FreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface FreshTokenRepository extends JpaRepository<FreshToken, String> {
    FreshToken findByEmail(String email);

    @Query("SELECT f.email FROM FreshToken f WHERE f.token = :refreshToken")
    String findEmailByToken(String refreshToken);
}
