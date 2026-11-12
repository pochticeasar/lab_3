package org.example.model;

public record PlayerCostEfficiency(
        String playerId,
        String name,
        int year,
        int age,
        String teams,
        long salary,
        double pts,
        double trb,
        double ast,
        double efficiency,
        double costPerEfficiency
) {
}
