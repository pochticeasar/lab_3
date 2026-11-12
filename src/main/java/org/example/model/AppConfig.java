package org.example.model;

import java.util.Arrays;
import java.util.List;

public record AppConfig(
        String playersPath,
        String salariesPath,
        String statsPath,
        String outputPath,
        int topN,
        List<Integer> years
) {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DEFAULT_OUTPUT_DIR = "output";
    private static final int DEFAULT_TOP_N = 5;

    public static AppConfig fromArgs(String[] args) {
        String dataDir = args.length > 0 ? args[0] : DEFAULT_DATA_DIR;
        String outputDir = args.length > 1 ? args[1] : DEFAULT_OUTPUT_DIR;
        List<Integer> years = args.length > 2 ? parseYears(args[2]) : List.of();
        return new AppConfig(
                dataDir + "/players.csv",
                dataDir + "/salaries_1985to2018.csv",
                dataDir + "/Seasons_Stats.csv",
                outputDir + "/cost_per_efficiency",
                DEFAULT_TOP_N,
                years);
    }

    private static List<Integer> parseYears(String value) {
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(year -> !year.isEmpty())
                .map(Integer::valueOf)
                .toList();
    }
}
