package club.constant.server.arena;

import net.minestom.server.coordinate.Pos;

public class Arena {

    private final String name;
    private final Pos spawn1;
    private final Pos spawn2;

    public Arena(String name, Pos spawn1, Pos spawn2) {
        this.name = name;
        this.spawn1 = spawn1;
        this.spawn2 = spawn2;
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

}
