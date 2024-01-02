package com.example.prj3be.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@EntityListeners(value = {AuditingEntityListener.class})
@Table(name="fresh_token")
@Getter @Setter
public class FreshToken {
    @Id
    @Column(name="email")
    private String email;
    @Column(name="token")
    private String token;
    @CreatedDate
    private LocalDateTime regTime;
}
