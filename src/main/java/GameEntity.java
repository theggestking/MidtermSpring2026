import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;

@Entity
@Table(name = "games")
class GameEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "started_at", nullable = false)
    private Instant startedAt;

    @Column(name = "completed_at", nullable = false)
    private Instant completedAt;

    @Column(name = "requested_rounds", nullable = false)
    private int requestedRounds;

    @Column(name = "completed_rounds", nullable = false)
    private int completedRounds;

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("seatNumber")
    private List<GamePlayerEntity> players = new ArrayList<>();

    @OneToMany(mappedBy = "game", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("roundNumber")
    private List<RoundEntity> rounds = new ArrayList<>();

    protected GameEntity() {
    }

    GameEntity(Instant startedAt, Instant completedAt, int requestedRounds, int completedRounds) {
        this.startedAt = startedAt;
        this.completedAt = completedAt;
        this.requestedRounds = requestedRounds;
        this.completedRounds = completedRounds;
    }

    void addPlayer(GamePlayerEntity player) {
        players.add(player);
        player.setGame(this);
    }

    void addRound(RoundEntity round) {
        rounds.add(round);
        round.setGame(this);
    }

    Long id() {
        return id;
    }

    Instant startedAt() {
        return startedAt;
    }

    Instant completedAt() {
        return completedAt;
    }

    int requestedRounds() {
        return requestedRounds;
    }

    int completedRounds() {
        return completedRounds;
    }

    List<GamePlayerEntity> players() {
        return players;
    }

    List<RoundEntity> rounds() {
        return rounds;
    }
}
