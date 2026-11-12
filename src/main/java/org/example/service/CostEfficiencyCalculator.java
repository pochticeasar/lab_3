package org.example.service;

import org.apache.spark.sql.Column;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.example.model.PlayerCostEfficiency;

import static org.apache.spark.sql.functions.*;

public class CostEfficiencyCalculator {

    private static final String TOTAL_TEAM = "TOT";
    private static final String[] JOIN_KEYS = {"year", "name", "age", "team"};

    public Dataset<PlayerCostEfficiency> calculate(Dataset<Row> players,
                                                   Dataset<Row> salaries,
                                                   Dataset<Row> stats) {
        Dataset<Row> perTeam = salaryByTeam(salaries, players)
                .join(statsByTeam(stats), JOIN_KEYS);

        return perTeam
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

    Dataset<Row> salaryByTeam(Dataset<Row> salaries, Dataset<Row> players) {
        return salaries
                .groupBy("playerId", "year", "teamName")
                .agg(sum("salary").as("salary"))
                .join(players, "playerId")
                .withColumn("age", ageAtSeason(col("birthDate"), col("year")))
                .withColumn("team", TeamAbbreviations.abbreviation(col("teamName"), col("year")))
                .select("playerId", "year", "name", "age", "team", "salary");
    }

    Dataset<Row> statsByTeam(Dataset<Row> stats) {
        return stats
                .filter(col("team").notEqual(TOTAL_TEAM))
                .select(
                        col("year"), col("name"), col("age"), col("team"),
                        coalesce(col("pts"), lit(0)).as("pts"),
                        coalesce(col("trb"), lit(0)).as("trb"),
                        coalesce(col("ast"), lit(0)).as("ast"));
    }

    static Column ageAtSeason(Column birthDate, Column seasonEndYear) {
        return seasonEndYear
                .minus(year(birthDate))
                .minus(when(month(birthDate).geq(2), 1).otherwise(0));
    }
}
