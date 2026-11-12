package org.example.service;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.SparkSession;
import org.example.model.PlayerCostEfficiency;

import java.util.List;

import static org.apache.spark.sql.functions.col;

public class ParquetRepository {

    private static final String PARTITION_COLUMN = "year";

    private final SparkSession spark;
    private final String path;

    public ParquetRepository(SparkSession spark, String path) {
        this.spark = spark;
        this.path = path;
    }

    public void save(Dataset<PlayerCostEfficiency> data) {
        data.repartition(col(PARTITION_COLUMN))
                .write()
                .mode(SaveMode.Overwrite)
                .partitionBy(PARTITION_COLUMN)
                .parquet(path);
    }

    public Dataset<PlayerCostEfficiency> load(List<Integer> years) {
        Dataset<Row> data = spark.read().parquet(path);
        if (!years.isEmpty()) {
            data = data.filter(col(PARTITION_COLUMN).isin(years.toArray()));
        }
        return data.as(Encoders.bean(PlayerCostEfficiency.class));
    }
}
