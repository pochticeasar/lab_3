package org.example.model;

public record AppConfig(
        String playersPath,
        String salariesPath,
        String statsPath,
        String outputPath,
        int topN
) {
    public static AppConfig defaults() {
        return new AppConfig(
                "data/players.csv",
                "data/salaries_1985to2018.csv",
                "data/Seasons_Stats.csv",
                "output/cost_per_efficiency",
                5);
    }
}
