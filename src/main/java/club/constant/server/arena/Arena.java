package club.constant.server.arena;

import club.constant.server.arena.state.ArenaState;
import club.constant.server.arena.type.ArenaType;
import net.minestom.server.coordinate.Pos;

public class Arena {

    private final String name;
    private final Pos spawn1;
    private final Pos spawn2;
    private final ArenaType type;

    private ArenaState arenaState = ArenaState.OPEN;

    public Arena(String name, Pos spawn1, Pos spawn2, ArenaType type) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public Pos getSpawn1() {
        return spawn1;
    }

    public Pos getSpawn2() {
        return spawn2;
    }

    public ArenaType getType() {
        return type;
    }

    public void setArenaState(ArenaState state) {
        this.arenaState = state;
    }

    public ArenaState getArenaState() {
        return arenaState;
    }

}
