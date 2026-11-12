package org.example.service;

import org.apache.spark.sql.Column;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.apache.spark.sql.functions.*;

public final class TeamAbbreviations {

    private static final String CHARLOTTE_HORNETS = "Charlotte Hornets";
    private static final int CHARLOTTE_HORNETS_FIRST_ERA_LAST_YEAR = 2002;

    private static final Map<String, String> ABBREVIATIONS = Map.ofEntries(
            Map.entry("Atlanta Hawks", "ATL"),
            Map.entry("Boston Celtics", "BOS"),
            Map.entry("Brooklyn Nets", "BRK"),
            Map.entry("Charlotte Bobcats", "CHA"),
            Map.entry("Chicago Bulls", "CHI"),
            Map.entry("Cleveland Cavaliers", "CLE"),
            Map.entry("Dallas Mavericks", "DAL"),
            Map.entry("Denver Nuggets", "DEN"),
            Map.entry("Detroit Pistons", "DET"),
            Map.entry("Golden State Warriors", "GSW"),
            Map.entry("Houston Rockets", "HOU"),
            Map.entry("Indiana Pacers", "IND"),
            Map.entry("Kansas City Kings", "KCK"),
            Map.entry("Los Angeles Clippers", "LAC"),
            Map.entry("Los Angeles Lakers", "LAL"),
            Map.entry("Memphis Grizzlies", "MEM"),
            Map.entry("Miami Heat", "MIA"),
            Map.entry("Milwaukee Bucks", "MIL"),
            Map.entry("Minnesota Timberwolves", "MIN"),
            Map.entry("New Jersey Nets", "NJN"),
            Map.entry("New Orleans Hornets", "NOH"),
            Map.entry("New Orleans Pelicans", "NOP"),
            Map.entry("New Orleans/Oklahoma City Hornets", "NOK"),
            Map.entry("New York Knicks", "NYK"),
            Map.entry("Oklahoma City Thunder", "OKC"),
            Map.entry("Orlando Magic", "ORL"),
            Map.entry("Philadelphia 76ers", "PHI"),
            Map.entry("Phoenix Suns", "PHO"),
            Map.entry("Portland Trail Blazers", "POR"),
            Map.entry("Sacramento Kings", "SAC"),
            Map.entry("San Antonio Spurs", "SAS"),
            Map.entry("Seattle SuperSonics", "SEA"),
            Map.entry("Toronto Raptors", "TOR"),
            Map.entry("Utah Jazz", "UTA"),
            Map.entry("Vancouver Grizzlies", "VAN"),
            Map.entry("Washington Bullets", "WSB"),
            Map.entry("Washington Wizards", "WAS")
    );

    private TeamAbbreviations() {
    }

    public static Column abbreviation(Column teamName, Column year) {
        return when(teamName.equalTo(CHARLOTTE_HORNETS),
                when(year.leq(CHARLOTTE_HORNETS_FIRST_ERA_LAST_YEAR), lit("CHH")).otherwise(lit("CHO")))
                .otherwise(element_at(abbreviationMap(), teamName));
    }

    private static Column abbreviationMap() {
        List<Column> entries = new ArrayList<>();
        ABBREVIATIONS.forEach((name, abbreviation) -> {
            entries.add(lit(name));
            entries.add(lit(abbreviation));
        });
        return map(entries.toArray(Column[]::new));
    }
}
