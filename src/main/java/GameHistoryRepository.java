import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;

final class GameHistoryRepository {
    private final EntityManagerFactory entityManagerFactory;

    GameHistoryRepository(EntityManagerFactory entityManagerFactory) {
        this.entityManagerFactory = Objects.requireNonNull(entityManagerFactory, "entityManagerFactory");
    }

    void save(CompletedGame completedGame) {
        Objects.requireNonNull(completedGame, "completedGame");
        withTransaction(entityManager -> {
            Map<String, PlayerEntity> playersByName = resolvePlayers(entityManager, completedGame.finalScores());
            GameEntity game = new GameEntity(
                    completedGame.startedAt(),
                    completedGame.completedAt(),
                    completedGame.requestedRounds(),
                    completedGame.completedRounds());

            for (FinalPlayerScore score : completedGame.finalScores()) {
                PlayerEntity player = playersByName.get(normalizeName(score.playerName()));
                game.addPlayer(new GamePlayerEntity(
                        player, score.seatNumber(), score.finalScore(), score.winner()));
            }

            for (CompletedRound completedRound : completedGame.rounds()) {
                PlayerEntity winner = completedRound.winnerName() == null
                        ? null
                        : requiredPlayer(playersByName, completedRound.winnerName());
                RoundEntity round = new RoundEntity(
                        completedRound.roundNumber(),
                        completedRound.startedAt(),
                        completedRound.completedAt(),
                        completedRound.status().name(),
                        winner,
                        completedRound.awardedPoints());

                for (RoundPlayerScore score : completedRound.playerScores()) {
                    round.addScore(new RoundScoreEntity(
                            requiredPlayer(playersByName, score.playerName()),
                            score.scoreBefore(),
                            score.scoreDelta(),
                            score.scoreAfter()));
                }
                game.addRound(round);
            }

            entityManager.persist(game);
            return null;
        });
    }

    List<RecentGameReport> recentGames(int limit) {
        validateLimit(limit);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            List<GameEntity> games = entityManager.createQuery("""
                    select g
                    from GameEntity g
                    order by g.completedAt desc, g.id desc
                    """, GameEntity.class)
                    .setMaxResults(limit)
                    .getResultList();

            List<RecentGameReport> reports = new ArrayList<>();
            for (GameEntity game : games) {
                List<FinalPlayerScore> scores = game.players().stream()
                        .map(player -> new FinalPlayerScore(
                                player.player().displayName(),
                                player.seatNumber(),
                                player.finalScore(),
                                player.winner()))
                        .toList();
                reports.add(new RecentGameReport(
                        game.id(),
                        game.completedAt(),
                        game.requestedRounds(),
                        game.completedRounds(),
                        scores));
            }
            return List.copyOf(reports);
        }
    }

    long playerWinCount(String playerName) {
        String normalizedName = normalizeName(playerName);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("""
                    select count(gamePlayer)
                    from GamePlayerEntity gamePlayer
                    where gamePlayer.winner = true
                      and gamePlayer.player.normalizedName = :normalizedName
                    """, Long.class)
                    .setParameter("normalizedName", normalizedName)
                    .getSingleResult();
        }
    }

    List<HighScoreReport> highestScores(int limit) {
        validateLimit(limit);
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            return entityManager.createQuery("""
                    select gamePlayer
                    from GamePlayerEntity gamePlayer
                    join fetch gamePlayer.player
                    join fetch gamePlayer.game
                    order by gamePlayer.finalScore desc,
                             gamePlayer.game.completedAt desc,
                             gamePlayer.id desc
                    """, GamePlayerEntity.class)
                    .setMaxResults(limit)
                    .getResultList()
                    .stream()
                    .map(gamePlayer -> new HighScoreReport(
                            gamePlayer.game().id(),
                            gamePlayer.player().displayName(),
                            gamePlayer.finalScore(),
                            gamePlayer.game().completedAt(),
                            gamePlayer.winner()))
                    .toList();
        }
    }

    private Map<String, PlayerEntity> resolvePlayers(
            EntityManager entityManager, List<FinalPlayerScore> finalScores) {
        Map<String, PlayerEntity> playersByName = new HashMap<>();
        for (FinalPlayerScore score : finalScores) {
            String normalizedName = normalizeName(score.playerName());
            PlayerEntity player = playersByName.get(normalizedName);
            if (player == null) {
                player = findPlayer(entityManager, normalizedName);
                if (player == null) {
                    player = new PlayerEntity(score.playerName(), normalizedName);
                    entityManager.persist(player);
                }
                playersByName.put(normalizedName, player);
            }
        }
        return playersByName;
    }

    private PlayerEntity findPlayer(EntityManager entityManager, String normalizedName) {
        return entityManager.createQuery("""
                select player
                from PlayerEntity player
                where player.normalizedName = :normalizedName
                """, PlayerEntity.class)
                .setParameter("normalizedName", normalizedName)
                .getResultStream()
                .findFirst()
                .orElse(null);
    }

    private PlayerEntity requiredPlayer(Map<String, PlayerEntity> playersByName, String playerName) {
        PlayerEntity player = playersByName.get(normalizeName(playerName));
        if (player == null) {
            throw new IllegalArgumentException("Round references unknown player: " + playerName);
        }
        return player;
    }

    private static String normalizeName(String playerName) {
        Objects.requireNonNull(playerName, "playerName");
        String normalizedName = playerName.trim().toLowerCase(Locale.ROOT);
        if (normalizedName.isEmpty()) {
            throw new IllegalArgumentException("Player name cannot be blank");
        }
        return normalizedName;
    }

    private static void validateLimit(int limit) {
        if (limit < 1 || limit > 100) {
            throw new IllegalArgumentException("Report limit must be between 1 and 100");
        }
    }

    private <T> T withTransaction(TransactionWork<T> work) {
        try (EntityManager entityManager = entityManagerFactory.createEntityManager()) {
            EntityTransaction transaction = entityManager.getTransaction();
            transaction.begin();
            try {
                T result = work.execute(entityManager);
                transaction.commit();
                return result;
            } catch (RuntimeException exception) {
                if (transaction.isActive()) {
                    transaction.rollback();
                }
                throw exception;
            }
        }
    }

    @FunctionalInterface
    private interface TransactionWork<T> {
        T execute(EntityManager entityManager);
    }
}
