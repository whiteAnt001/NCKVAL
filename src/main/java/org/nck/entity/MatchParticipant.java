package org.nck.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "match_participants")
public class MatchParticipant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id")
    private Match match;

    private String puuid;
    private String displayName;   // 닉네임#태그
    private String team;          // "Red" or "Blue"
    private String agent;
    private int kills;
    private int deaths;
    private int assists;
    private int score;
    private boolean isWin;
}
