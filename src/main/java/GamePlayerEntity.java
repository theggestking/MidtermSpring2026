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
@Table(name = "game_players")
class GamePlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false)
    private GameEntity game;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerEntity player;

    @Column(name = "seat_number", nullable = false)
    private int seatNumber;

    @Column(name = "final_score", nullable = false)
    private int finalScore;

    @Column(name = "winner", nullable = false)
    private boolean winner;

    protected GamePlayerEntity() {
    }

    GamePlayerEntity(PlayerEntity player, int seatNumber, int finalScore, boolean winner) {
        this.player = player;
        this.seatNumber = seatNumber;
        this.finalScore = finalScore;
        this.winner = winner;
    }

    void setGame(GameEntity game) {
        this.game = game;
    }

    Long id() {
        return id;
    }

    GameEntity game() {
        return game;
    }

    PlayerEntity player() {
        return player;
    }

    int seatNumber() {
        return seatNumber;
    }

    int finalScore() {
        return finalScore;
    }

    boolean winner() {
        return winner;
    }
}
