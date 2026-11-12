package org.example.model;

public record AppConfig(
        String playersPath,
        String salariesPath,
        String statsPath,
        String outputPath,
        int topN
) {
    private static final String DEFAULT_DATA_DIR = "data";
    private static final String DEFAULT_OUTPUT_DIR = "output";
    private static final int DEFAULT_TOP_N = 5;

    public static AppConfig fromArgs(String[] args) {
        String dataDir = args.length > 0 ? args[0] : DEFAULT_DATA_DIR;
        String outputDir = args.length > 1 ? args[1] : DEFAULT_OUTPUT_DIR;
        return new AppConfig(
                dataDir + "/players.csv",
                dataDir + "/salaries_1985to2018.csv",
                dataDir + "/Seasons_Stats.csv",
                outputDir + "/cost_per_efficiency",
                DEFAULT_TOP_N);
    }
}
