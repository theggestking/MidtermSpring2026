import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "players")
class PlayerEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "display_name", nullable = false, length = 100)
    private String displayName;

    @Column(name = "normalized_name", nullable = false, unique = true, length = 100)
    private String normalizedName;

    protected PlayerEntity() {
    }

    PlayerEntity(String displayName, String normalizedName) {
        this.displayName = displayName;
        this.normalizedName = normalizedName;
    }

    Long id() {
        return id;
    }

    String displayName() {
        return displayName;
    }

    String normalizedName() {
        return normalizedName;
    }
}
