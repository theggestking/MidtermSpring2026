import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "round_scores")
class RoundScoreEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "round_id", nullable = false)
    private RoundEntity round;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Column(name = "score_before", nullable = false)
    private int scoreBefore;

    @Column(name = "score_delta", nullable = false)
    private int scoreDelta;

    @Column(name = "score_after", nullable = false)
    private int scoreAfter;

    protected RoundScoreEntity() {
    }

    RoundScoreEntity(PlayerEntity player, int scoreBefore, int scoreDelta, int scoreAfter) {
        this.player = player;
        this.scoreBefore = scoreBefore;
        this.scoreDelta = scoreDelta;
        this.scoreAfter = scoreAfter;
    }

    void setRound(RoundEntity round) {
        this.round = round;
    }

    PlayerEntity player() {
        return player;
    }

    int scoreBefore() {
        return scoreBefore;
    }

    int scoreDelta() {
        return scoreDelta;
    }

    int scoreAfter() {
        return scoreAfter;
    }
}
