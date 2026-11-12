package org.example.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.expressions.WindowSpec;
import org.example.model.PlayerCostEfficiency;
import org.example.model.TopPlayer;

import static org.apache.spark.sql.functions.*;

public class TopPlayersReport {

    private final int topN;

    public TopPlayersReport(int topN) {
        this.topN = topN;
    }

    public Dataset<TopPlayer> build(Dataset<PlayerCostEfficiency> data) {
        WindowSpec byYear = Window.partitionBy("year")
                .orderBy(col("costPerEfficiency").asc(), col("name").asc());

        return data
                .withColumn("rank", row_number().over(byYear))
                .filter(col("rank").leq(topN))
                .orderBy("year", "rank")
                .select("year", "rank", "name", "salary", "efficiency", "costPerEfficiency")
                .as(Encoders.bean(TopPlayer.class));
    }

    public void print(Dataset<TopPlayer> top) {
        long years = top.select("year").distinct().count();
        System.out.printf("Топ-%d самых выгодных игроков (минимальный Cost per Efficiency) по годам:%n", topN);
        top.show((int) years * topN, false);
    }
}
