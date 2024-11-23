package com.example.userservice.model;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;

import java.io.Serializable;
import java.time.LocalDateTime;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "T_REFRESH_TOKEN")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken implements Serializable {
    @Id
    @Column(name = "refresh_token_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long refreshTokenId;
    
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "refresh_token")
    private String refreshToken;

    @Column(name = "expiration")
    private LocalDateTime expiration;

    @Column(name = "create_time")
    @CreationTimestamp
    private LocalDateTime createTime;
}
