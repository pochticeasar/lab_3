package org.example.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.example.model.AppConfig;

import static org.apache.spark.sql.functions.*;

public class DataLoader {

    private final SparkSession spark;
    private final AppConfig config;

    public DataLoader(SparkSession spark, AppConfig config) {
        this.spark = spark;
        this.config = config;
    }

    public Dataset<Row> players() {
        return readCsv(config.playersPath())
                .select(col("_id").as("playerId"), trim(col("name")).as("name"));
    }

    public Dataset<Row> salaries() {
        return readCsv(config.salariesPath())
                .select(
                        col("player_id").as("playerId"),
                        col("season_end").cast("int").as("year"),
                        col("salary").cast("long").as("salary"));
    }

    public Dataset<Row> seasonStats() {
        return readCsv(config.statsPath())
                .filter(not(col("Player").endsWith("*")))
                .select(
                        col("Year").cast("int").as("year"),
                        trim(col("Player")).as("name"),
                        col("Tm").as("team"),
                        col("PTS").cast("double").as("pts"),
                        col("TRB").cast("double").as("trb"),
                        col("AST").cast("double").as("ast"))
                .filter(col("year").isNotNull().and(col("name").isNotNull()));
    }

    private Dataset<Row> readCsv(String path) {
        return spark.read()
                .option("header", "true")
                .option("multiLine", "true")
                .option("escape", "\"")
                .csv(path);
    }
}
