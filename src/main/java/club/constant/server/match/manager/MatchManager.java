package club.constant.server.match.manager;

import club.constant.server.match.Match;
import net.minestom.server.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class MatchManager {

    private final List<Match> matches = new ArrayList<>();

    public void addMatch(Match match) {
        matches.add(match);
    }

    public Match getMatch(Player player) {
        return matches.stream().filter(match -> match.getPlayers().contains(player)).findFirst().orElse(null);
    }

    public List<Match> getMatches() {
        return matches;
    }

}
