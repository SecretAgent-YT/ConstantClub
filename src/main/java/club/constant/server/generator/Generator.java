package club.constant.server.generator;

import club.constant.server.ConstantServer;
import club.constant.server.arena.Arena;
import club.constant.server.arena.type.ArenaType;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.stream.JsonReader;
import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.InstanceContainer;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.*;
import java.util.*;
import java.util.function.Consumer;

public class Generator {

    private final InstanceContainer instance;
    private final Consumer<?> onceCompleted;

    private final List<File> schematicFiles = new ArrayList<>();
    private final List<Schematic> schematics = new ArrayList<>();

    private int x = 0;

    private int i = 0;
    private int i2 = 1;

    public Generator(InstanceContainer instance, Consumer<?> onceCompleted) {
        this.instance = instance;
        this.onceCompleted = onceCompleted;
    }

    public void start() {
        schematicFiles.addAll(Arrays.asList(Objects.requireNonNull(new File("schematics").listFiles(pathname -> pathname.getName().endsWith(".schem")))));
        schematicFiles.sort(Comparator.comparing(File::getName, (file1, file2) -> {
            if (file1.endsWith("lobby.schem")) {
                return -1;
            }
            return 0;
        }));
        schematicFiles.forEach(file -> {
            try {
                schematics.add(Scaffolding.fromFile(file));
            } catch (IOException | NBTException e) {
                e.printStackTrace();
            }
        });
        try {
            buildNext();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void buildNext() throws FileNotFoundException {
        if ((schematics.size() - 1) < i) {
            onceCompleted.accept(null);
            return;
        }
        Pos pos = new Pos(x, 64, 0);
        Schematic schematic = schematics.get(i);
        System.out.println("Building " + schematicFiles.get(i).getName() + " | Instance " + i2);
        if (!schematicFiles.get(i).getName().endsWith("lobby.schem")) {
            System.out.println(this.x);
            JsonObject object = JsonParser.parseReader(new JsonReader(new FileReader(new File("schematics", schematicFiles.get(i).getName().replace(".schem", ".json"))))).getAsJsonObject();
            JsonObject spawn1Object = object.getAsJsonObject("spawn1");
            JsonObject spawn2Object = object.getAsJsonObject("spawn2");
            float x1 = spawn1Object.get("x").getAsFloat() + this.x;
            float y1 = spawn1Object.get("y").getAsFloat();
            float z1 = spawn1Object.get("z").getAsFloat();
            float yaw1 = spawn1Object.get("yaw").getAsFloat();
            float x2 = spawn2Object.get("x").getAsFloat() + this.x;
            float y2 = spawn2Object.get("y").getAsFloat();
            float z2 = spawn2Object.get("z").getAsFloat();
            float yaw2 = spawn2Object.get("yaw").getAsFloat();
            ArenaType type = ArenaType.valueOf(object.get("type").getAsString());
            String name = object.get("name").getAsString();
            Pos spawn1 = new Pos(x1, y1, z1, 0, yaw1);
            Pos spawn2 = new Pos(x2, y2, z2, 0, yaw2);
            Arena arena = new Arena(name + " " + i2, spawn1, spawn2, type);
        }
        if (i2 >= 5 || schematicFiles.get(i).getName().endsWith("lobby.schem")) {
            i++;
            i2 = 1;
        } else {
            i2++;
        }
        x += 500;
        schematic.build(instance, pos).whenComplete((ignore, ignore2) -> {
            try {
                buildNext();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        });
    }

}
