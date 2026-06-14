package com.example.jackpot.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest(
        properties = {
            "jackpot.messaging.mode=log",
            "spring.datasource.url=jdbc:h2:mem:flyway-persistence;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
        })
@DisplayName("Flyway persistence")
class FlywayPersistenceTest {
    @Autowired JdbcTemplate jdbc;

    @Test
    @DisplayName("Should initialize an empty database with exactly one successful migration")
    void shouldInitializeEmptyDatabaseWithExactlyOneSuccessfulMigration() {
        Integer migrationCount =
                jdbc.queryForObject(
                        "select count(*) from \"flyway_schema_history\" where \"version\" is not null and \"success\" = true",
                        Integer.class);

        assertThat(migrationCount).isOne();
    }

    @Test
    @DisplayName("Should create the final tables and columns without obsolete contribution fields")
    void shouldCreateFinalTablesAndColumnsWithoutObsoleteContributionFields() {
        var tables =
                jdbc.queryForList(
                        "select table_name from information_schema.tables where table_schema='PUBLIC'",
                        String.class);
        var jackpotColumns =
                jdbc.queryForList(
                        "select column_name from information_schema.columns where table_schema='PUBLIC' and table_name='JACKPOTS'",
                        String.class);
        var betColumns =
                jdbc.queryForList(
                        "select column_name from information_schema.columns where table_schema='PUBLIC' and table_name='BETS'",
                        String.class);

        assertThat(tables)
                .contains(
                        "BETS",
                        "JACKPOTS",
                        "JACKPOT_CONTRIBUTIONS",
                        "JACKPOT_REWARD_EVALUATIONS",
                        "JACKPOT_REWARDS");
        assertThat(jackpotColumns)
                .contains(
                        "CONTRIBUTION_STRATEGY",
                        "CONTRIBUTION_PARAMETERS",
                        "REWARD_STRATEGY",
                        "REWARD_PARAMETERS")
                .doesNotContain(
                        "CONTRIBUTION_TYPE",
                        "FIXED_CONTRIBUTION_PERCENTAGE",
                        "VARIABLE_CONTRIBUTION_INITIAL_PERCENTAGE");
        assertThat(betColumns)
                .contains(
                        "FAILURE_CODE",
                        "FAILURE_MESSAGE",
                        "FAILED_AT",
                        "PUBLICATION_ATTEMPTS",
                        "LAST_PUBLICATION_ERROR");
    }

    @Test
    @DisplayName("Should enforce the jackpot foreign key for accepted bets")
    void shouldEnforceJackpotForeignKeyForAcceptedBets() {
        assertThatThrownBy(() -> insertBet(UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should enforce one contribution per bet")
    void shouldEnforceOneContributionPerBet() {
        var jackpotId = insertJackpot();
        var betId = UUID.randomUUID();
        insertBet(betId, jackpotId);
        insertContribution(UUID.randomUUID(), betId, jackpotId);

        assertThatThrownBy(() -> insertContribution(UUID.randomUUID(), betId, jackpotId))
                .isInstanceOf(Exception.class);
    }

    @Test
    @DisplayName("Should persist fractional contributions and pools at ledger scale")
    void shouldPersistFractionalContributionsAndPoolsAtLedgerScale() {
        var jackpotId = insertJackpot();
        var betId = UUID.randomUUID();
        insertBet(betId, jackpotId);
        insertContribution(UUID.randomUUID(), betId, jackpotId);

        BigDecimal contribution =
                jdbc.queryForObject(
                        "select contribution_amount from jackpot_contributions where bet_id = ?",
                        BigDecimal.class,
                        betId);
        BigDecimal contributionPool =
                jdbc.queryForObject(
                        "select current_jackpot_amount from jackpot_contributions where bet_id = ?",
                        BigDecimal.class,
                        betId);
        BigDecimal currentPool =
                jdbc.queryForObject(
                        "select current_pool_amount from jackpots where id = ?",
                        BigDecimal.class,
                        jackpotId);

        assertThat(contribution).isEqualTo(new BigDecimal("0.00010000"));
        assertThat(contributionPool).isEqualTo(new BigDecimal("1000.00010000"));
        assertThat(currentPool).isEqualTo(new BigDecimal("1000.00010000"));
    }

    private UUID insertJackpot() {
        var id = UUID.randomUUID();
        var now = Instant.now();
        jdbc.update(
                """
                insert into jackpots (
                    id, name, initial_pool_amount, current_pool_amount,
                    contribution_strategy, contribution_parameters,
                    reward_strategy, reward_parameters, created_at, updated_at, version
                ) values (?,?,?,?,?,?,?,?,?,?,?)
                """,
                id,
                "jackpot-" + id,
                new BigDecimal("1000.00000000"),
                new BigDecimal("1000.00010000"),
                "FIXED",
                "{\"percentage\":1}",
                "FIXED",
                "{\"chance\":1}",
                now,
                now,
                0);
        return id;
    }

    private void insertBet(UUID betId, UUID jackpotId) {
        var now = Instant.now();
        jdbc.update(
                """
                insert into bets (
                    id, user_id, jackpot_id, bet_amount, status,
                    publication_attempts, created_at, updated_at
                ) values (?,?,?,?,?,?,?,?)
                """,
                betId,
                UUID.randomUUID(),
                jackpotId,
                new BigDecimal("0.01"),
                "PENDING_PUBLICATION",
                0,
                now,
                now);
    }

    private void insertContribution(UUID id, UUID betId, UUID jackpotId) {
        jdbc.update(
                """
                insert into jackpot_contributions (
                    id, bet_id, user_id, jackpot_id, stake_amount,
                    contribution_amount, current_jackpot_amount, created_at
                ) values (?,?,?,?,?,?,?,?)
                """,
                id,
                betId,
                UUID.randomUUID(),
                jackpotId,
                new BigDecimal("0.01"),
                new BigDecimal("0.00010000"),
                new BigDecimal("1000.00010000"),
                Instant.now());
    }
}
