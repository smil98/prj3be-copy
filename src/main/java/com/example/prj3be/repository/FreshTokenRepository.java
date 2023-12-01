package com.example.prj3be.repository;

import com.example.prj3be.domain.FreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;


public interface FreshTokenRepository extends JpaRepository<FreshToken, String> {
    FreshToken findByLogId(String id);

//    @Modifying
//    @Query("DELETE FROM FreshToken f where f.logId = :logId")
//    void deleteByLogId(String logId);
}
