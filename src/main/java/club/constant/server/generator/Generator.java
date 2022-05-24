package club.constant.server.generator;

import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Generator {

    private final InstanceContainer instance;
    private final Consumer<?> onceCompleted;

    private final List<File> schematicFiles = new ArrayList<>();
    private final List<Schematic> schematics = new ArrayList<>();

    private int x = 0;
    private int z = 0;

    private int i = 0;
    private int i2 = 1;

    public Generator(InstanceContainer instance, Consumer<?> onceCompleted) {
        this.instance = instance;
        this.onceCompleted = onceCompleted;
    }

    public void start() {
        for (File file : new File("schematics").listFiles()) {
            try {
                schematics.add(Scaffolding.fromFile(file));
                schematicFiles.add(file);
                System.out.println(schematics.size());
            } catch (IOException | NBTException e) {
                e.printStackTrace();
            }
        }
        buildNext();
    }

    private void buildNext() {
        if ((schematics.size() - 1) < i) {
            onceCompleted.accept(null);
            return;
        }
        Pos pos = new Pos(x, 64, z);
        Schematic schematic = schematics.get(i);
        x += 500;
        z += 500;
        System.out.println("Building " + schematicFiles.get(i).getName() + " | Instance " + i2);
        if (i2 >= 5 || schematicFiles.get(i).getName().endsWith("lobby.schem")) {
            i++;
            i2 = 0;
        }
        schematic.build(instance, pos).whenComplete((ignore, ignore2) -> buildNext());
    }

}
