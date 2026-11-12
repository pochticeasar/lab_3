package org.example;

import org.apache.spark.SparkConf;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;
import org.example.model.AppConfig;
import org.example.model.PlayerCostEfficiency;
import org.example.model.TopPlayer;
import org.example.service.CostEfficiencyCalculator;
import org.example.service.DataLoader;
import org.example.service.ParquetRepository;
import org.example.service.TopPlayersReport;

public class Main {

    public static void main(String[] args) {
        AppConfig config = AppConfig.fromArgs(args);

        SparkSession.Builder builder = SparkSession.builder()
                .appName("NBA Cost per Efficiency")
                .config("spark.sql.shuffle.partitions", "8");
        if (!new SparkConf().contains("spark.master")) {
            builder.master("local[*]");
        }
        SparkSession spark = builder.getOrCreate();
        spark.sparkContext().setLogLevel("WARN");

        try {
            DataLoader loader = new DataLoader(spark, config);

            Dataset<PlayerCostEfficiency> result = new CostEfficiencyCalculator()
                    .calculate(loader.players(), loader.salaries(), loader.seasonStats());

            ParquetRepository repository = new ParquetRepository(spark, config.outputPath());
            repository.save(result);

            Dataset<PlayerCostEfficiency> fromParquet = repository.load();
            System.out.println("Схема прочитанного parquet (year восстановлен из имён каталогов year=...):");
            fromParquet.printSchema();

            TopPlayersReport report = new TopPlayersReport(config.topN());
            Dataset<TopPlayer> top = report.build(fromParquet);
            report.print(top);
        } finally {
            spark.stop();
        }
    }
}
