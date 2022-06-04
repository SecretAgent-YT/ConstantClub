package club.constant.server;

import club.constant.server.arena.commands.ArenaCommand;
import club.constant.server.arena.manager.ArenaManager;
import club.constant.server.commands.GameModeCommand;
import club.constant.server.generator.Generator;
import club.constant.server.match.manager.MatchManager;
import club.constant.server.match.commands.LeaveCommand;
import club.constant.server.playerstate.PlayerState;
import club.constant.server.playerstate.manager.PlayerStateManager;
import club.constant.server.queue.manager.QueueManager;
import club.constant.server.queue.commands.QueueCommand;
import io.github.bloepiloepi.pvp.PvpExtension;
import io.github.bloepiloepi.pvp.explosion.PvpExplosionSupplier;
import net.crystalgames.scaffolding.Scaffolding;
import net.crystalgames.scaffolding.schematic.Schematic;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.adventure.audience.Audiences;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.player.PlayerLoginEvent;
import net.minestom.server.extras.MojangAuth;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.InstanceManager;
import net.minestom.server.instance.block.Block;
import net.minestom.server.monitoring.BenchmarkManager;
import net.minestom.server.utils.time.TimeUnit;
import org.jglrxavpok.hephaistos.nbt.NBTException;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.Collection;

public class ConstantServer {

    private final int port;
    private final MinecraftServer server;

    private final QueueManager queueManager;
    private final PlayerStateManager playerStateManager;
    private final MatchManager matchManager;
    private final ArenaManager arenaManager;

    private boolean playersCanJoin = false;

    public ConstantServer(int port) {
        this.port = port;
        this.server = MinecraftServer.init();
        this.queueManager = new QueueManager();
        this.playerStateManager = new PlayerStateManager();
        this.matchManager = new MatchManager();
        this.arenaManager = new ArenaManager();
    }

    public void start() {
        System.out.println("Starting minestom server...");
        System.setProperty("minestom.chunk-view-distance", String.valueOf(4));
        System.setProperty("minestom.entity-view-distance", String.valueOf(4));
        InstanceManager manager = MinecraftServer.getInstanceManager();
        InstanceContainer instance = manager.createInstanceContainer();
        instance.setGenerator(unit -> unit.modifier().fillHeight(0, 40, Block.AIR));
        GlobalEventHandler handler = MinecraftServer.getGlobalEventHandler();
        handler.addListener(PlayerLoginEvent.class, event -> {
            Player player = event.getPlayer();
            if (!playersCanJoin) player.kick("You can't join yet!");
            player.setGameMode(GameMode.CREATIVE);
            event.setSpawningInstance(instance);
            player.setRespawnPoint(new Pos(0.5, 66, 0.5));
            getPlayerStateManager().setState(player, PlayerState.LOBBY);
        });
        MinecraftServer.getCommandManager().register(new GameModeCommand());
        MinecraftServer.getCommandManager().register(new QueueCommand());
        MinecraftServer.getCommandManager().register(new LeaveCommand());
        MinecraftServer.getCommandManager().register(new ArenaCommand());
        instance.setExplosionSupplier(PvpExplosionSupplier.INSTANCE);
        PvpExtension.init();
        System.out.println("Initialized PVP Extension");
        BenchmarkManager benchmarkManager = MinecraftServer.getBenchmarkManager();
        benchmarkManager.enable(Duration.ofMillis(Long.MAX_VALUE));
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            Collection<Player> players = MinecraftServer.getConnectionManager().getOnlinePlayers();
            if (players.isEmpty())
                return;

            long ramUsage = benchmarkManager.getUsedMemory();
            ramUsage /= 1e6; // bytes to MB

            final Component header = Component.text("Constants Club")
                    .append(Component.newline()).append(Component.text("Players: " + players.size()))
                    .append(Component.newline()).append(Component.newline())
                    .append(Component.text("RAM USAGE: " + ramUsage + " MB").append(Component.newline()));
            final Component footer = Component.newline().append(Component.text("constants.club")
                    .append(Component.newline()).append(Component.newline()));
            Audiences.players().sendPlayerListHeaderAndFooter(header, footer);
        }).repeat(10, TimeUnit.SERVER_TICK).schedule();
        System.out.println("Prepared Benchmark Manager");
        MojangAuth.init();
        System.out.println("Initialized MojangAuth");
        Generator generator = new Generator(instance, ignore -> playersCanJoin = true);
        generator.start();
        server.start("0.0.0.0", 25565);
    }

    public QueueManager getQueueManager() {
        return queueManager;
    }

    public PlayerStateManager getPlayerStateManager() {
        return playerStateManager;
    }

    public MatchManager getMatchManager() {
        return matchManager;
    }

    public ArenaManager getArenaManager() {
        return arenaManager;
    }

    public static final ConstantServer INSTANCE = new ConstantServer(25565);

    public static void main(String[] args) {
        INSTANCE.start();
    }

}