package org.example.model;

public record TopPlayer(
        int year,
        int rank,
        String name,
        int age,
        String teams,
        long salary,
        double efficiency,
        double costPerEfficiency
) {
}
