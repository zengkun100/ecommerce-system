package com.example.userservice.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Table(name = "T_ACCESS_TOKEN")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AccessToken implements Serializable {
    @Id
    @Column(name = "token_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tokenId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "token")
    private String token;

    @Column(name = "expiration")
    private LocalDateTime expiration;

    @Column(name = "create_time")
    @CreationTimestamp
    private LocalDateTime createTime;
}
