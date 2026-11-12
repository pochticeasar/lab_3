package org.example.service;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.example.model.PlayerCostEfficiency;

import static org.apache.spark.sql.functions.*;

public class CostEfficiencyCalculator {

    private static final String[] PLAYER_KEYS = {"year", "name", "age", "team"};

    public Dataset<PlayerCostEfficiency> calculate(Dataset<Row> players,
                                                   Dataset<Row> salaries,
                                                   Dataset<Row> stats) {
        Dataset<Row> playerTeamSeasons = playerTeamSeasons(salaries, players);
        Dataset<Row> statsWithPlayer = attachPlayer(stats, playerTeamSeasons);
        return seasonResults(statsWithPlayer);
    }

    Dataset<Row> playerTeamSeasons(Dataset<Row> salaries, Dataset<Row> players) {
        return salaries
                .groupBy("playerId", "year", "teamName")
                .agg(sum("salary").as("salary"))
                .join(players, "playerId")
                .withColumn("age", ageAtSeason(col("birthDate"), col("year")))
                .withColumn("team", TeamAbbreviations.abbreviation(col("teamName"), col("year")))
                .select("playerId", "year", "name", "age", "team", "salary");
    }

    Dataset<Row> attachPlayer(Dataset<Row> stats, Dataset<Row> playerTeamSeasons) {
        return stats
                .join(playerTeamSeasons, PLAYER_KEYS)
                .select(
                        col("playerId"), col("name"), col("year"), col("age"), col("team"),
                        col("salary"),
                        coalesce(col("pts"), lit(0)).as("pts"),
                        coalesce(col("trb"), lit(0)).as("trb"),
                        coalesce(col("ast"), lit(0)).as("ast"));
    }

    Dataset<PlayerCostEfficiency> seasonResults(Dataset<Row> statsWithPlayer) {
        return statsWithPlayer
                .groupBy("playerId", "name", "year", "age")
                .agg(
                        concat_ws(", ", array_sort(collect_list("team"))).as("teams"),
                        round(avg("salary")).cast("long").as("salary"),
                        round(avg("pts"), 2).as("pts"),
                        round(avg("trb"), 2).as("trb"),
                        round(avg("ast"), 2).as("ast"))
                .withColumn("efficiency", col("pts").plus(col("trb")).plus(col("ast")))
                .filter(col("efficiency").gt(0))
                .withColumn("costPerEfficiency", round(col("salary").divide(col("efficiency")), 2))
                .select("playerId", "name", "year", "age", "teams", "salary", "pts", "trb", "ast",
                        "efficiency", "costPerEfficiency")
                .as(Encoders.bean(PlayerCostEfficiency.class));
    }

    static Column ageAtSeason(Column birthDate, Column seasonEndYear) {
        return seasonEndYear
                .minus(year(birthDate))
                .minus(when(month(birthDate).geq(2), 1).otherwise(0));
    }
}
