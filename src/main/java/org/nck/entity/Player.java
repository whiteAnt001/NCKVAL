package org.nck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "players")
public class Player {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;        // 닉네임

    @Column(nullable = false)
    private String tag;         // 태그 (#1234)

    @Column(nullable = false, unique = true)
    private String puuid;       // 라이엇 고유 ID

    private String region;      // kr, ap 등

    private int accountLevel;

    private String tier;        // 현재 티어 (Gold 2 등)
    private int tierRank;       // 밸런싱용 점수 (숫자로 변환)

    private String cardSmall;   // 프로필 카드 이미지

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
