package club.constant.server.arena.manager;

import club.constant.server.arena.Arena;
import club.constant.server.arena.state.ArenaState;
import club.constant.server.arena.type.ArenaType;

import java.util.ArrayList;
import java.util.List;

public class ArenaManager {

    private final List<Arena> arenas = new ArrayList<>();

    public List<Arena> getArenas() {
        return arenas;
    }

    public void addArena(Arena arena) {
        arenas.add(arena);
    }

    public Arena getAvailableArena(ArenaType type) {
        return arenas.stream().filter(arena -> arena.getArenaState() == ArenaState.OPEN && arena.getType() == type).findFirst().orElse(null);
    }

    public boolean arenaAvailable(ArenaType type) {
        return getAvailableArena(type) != null;
    }

}
