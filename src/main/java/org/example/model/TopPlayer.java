package org.example.model;

public record TopPlayer(
        int year,
        int rank,
        String name,
        long salary,
        double efficiency,
        double costPerEfficiency
) {
}
