package org.example.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.expressions.Window;
import org.example.model.PlayerCostEfficiency;

import static org.apache.spark.sql.functions.*;

public class CostEfficiencyCalculator {

    private static final String TOTAL_TEAM = "TOT";

    public Dataset<PlayerCostEfficiency> calculate(Dataset<Row> players,
                                                   Dataset<Row> salaries,
                                                   Dataset<Row> stats) {
        return seasonSalary(salaries, players)
                .join(efficiency(stats), new String[]{"name", "year"})
                .filter(col("efficiency").gt(0))
                .withColumn("costPerEfficiency", round(col("salary").divide(col("efficiency")), 2))
                .select("playerId", "name", "year", "salary", "pts", "trb", "ast",
                        "efficiency", "costPerEfficiency")
                .as(Encoders.bean(PlayerCostEfficiency.class));
    }

    Dataset<Row> efficiency(Dataset<Row> stats) {
        var isTeamRow = col("team").notEqual(TOTAL_TEAM);
        return stats
                .groupBy("name", "year")
                .agg(
                        sum(when(isTeamRow, 1).otherwise(0)).as("teamRows"),
                        max(when(col("team").equalTo(TOTAL_TEAM), true).otherwise(false)).as("hasTotal"),
                        round(avg(when(isTeamRow, coalesce(col("pts"), lit(0)))), 2).as("pts"),
                        round(avg(when(isTeamRow, coalesce(col("trb"), lit(0)))), 2).as("trb"),
                        round(avg(when(isTeamRow, coalesce(col("ast"), lit(0)))), 2).as("ast"))
                .filter(col("teamRows").equalTo(1).or(col("hasTotal")))
                .withColumn("efficiency", col("pts").plus(col("trb")).plus(col("ast")))
                .drop("teamRows", "hasTotal");
    }

    Dataset<Row> seasonSalary(Dataset<Row> salaries, Dataset<Row> players) {
        return salaries
                .groupBy("playerId", "year")
                .agg(round(avg("salary")).cast("long").as("salary"))
                .join(players, "playerId")
                .withColumn("idsWithName", count("playerId").over(Window.partitionBy("name", "year")))
                .filter(col("idsWithName").equalTo(1))
                .drop("idsWithName");
    }
}
