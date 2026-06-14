import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "rounds")
class RoundEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @Column(name = "round_number", nullable = false)
    private int roundNumber;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "status", nullable = false, length = 30)
    private String status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_player_id")
    private PlayerEntity winner;

    @Column(name = "awarded_points", nullable = false)
    private int awardedPoints;

    @OneToMany(mappedBy = "round", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RoundScoreEntity> scores = new ArrayList<>();

    protected RoundEntity() {
    }

    RoundEntity(int roundNumber, Instant startedAt, Instant completedAt,
            String status, PlayerEntity winner, int awardedPoints) {
        this.roundNumber = roundNumber;
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.status = status;
        this.winner = winner;
        this.awardedPoints = awardedPoints;
    }

    void setGame(GameEntity game) {
        this.game = game;
    }

    void addScore(RoundScoreEntity score) {
        scores.add(score);
        score.setRound(this);
    }
}
