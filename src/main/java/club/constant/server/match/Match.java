package club.constant.server.match;

import club.constant.server.ConstantServer;
import club.constant.server.arena.Arena;
import club.constant.server.arena.state.ArenaState;
import club.constant.server.kit.Kit;
import club.constant.server.match.state.MatchState;
import club.constant.server.match.team.Team;
import club.constant.server.playerstate.PlayerState;
import io.github.bloepiloepi.pvp.events.FinalAttackEvent;
import io.github.bloepiloepi.pvp.events.FinalDamageEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.timer.TaskSchedule;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class Match {

    private static final ConstantServer server = ConstantServer.INSTANCE;

    private final UUID uuid = UUID.randomUUID();

    private final Kit kit;
    private final Arena arena;
    private final List<Player> blueTeam;
    private final List<Player> redTeam;
    private final List<Player> players = new ArrayList<>();

    private int blueSize;
    private int redSize;

    private MatchState state = MatchState.CREATED;

    private Team winner;

    public Match(Kit kit, Arena arena, List<Player> blueTeam, List<Player> redTeam) {
        this.kit = kit;
        this.arena = arena;
        this.blueTeam = blueTeam;
        this.redTeam = redTeam;
        players.addAll(redTeam);
        players.addAll(blueTeam);
        this.redSize = redTeam.size();
        this.blueSize = blueTeam.size();
    }

    public void start() {
        setState(MatchState.STARTING);
    }

    private EventNode<Event> events() {
        EventNode<Event> events = EventNode.all(uuid.toString());
        events.addListener(EventListener.builder(FinalDamageEvent.class)
                .filter(event -> {
                    if (event.getEntity() instanceof Player player) {
                        return players.contains(player) && event.doesKillEntity();
                    }
                    return false;
                })
                .expireWhen(event -> getState() == MatchState.ENDING)
                .handler(event -> {
                    event.setCancelled(true);
                    Player player = (Player) event.getEntity();
                    player.getInventory().clear();
                    player.setGameMode(GameMode.SPECTATOR);
                    if (blueTeam.contains(player)) {
                        blueSize--;
                    } else if (redTeam.contains(player)) {
                        redSize--;
                    }
                    if (redSize <= 0) {
                        winner = Team.BLUE;
                        startEndTask();
                    } else if (blueSize <= 0) {
                        winner = Team.RED;
                        startEndTask();
                    }
                })
                .build()
        );
        events.addListener(EventListener.builder(FinalAttackEvent.class)
                .filter(event -> {
                    if (event.getEntity() instanceof Player player && event.getTarget() instanceof Player target) {
                        return players.contains(player) || players.contains(target);
                    }
                    return false;
                })
                .expireWhen(event -> getState() == MatchState.ENDING)
                .handler(event -> {
                    Player player = (Player) event.getEntity();
                    Player target = (Player) event.getTarget();
                    if (blueTeam.contains(player) && blueTeam.contains(target)) {
                        event.setCancelled(true);
                    } else if (redTeam.contains(player) && redTeam.contains(target)) {
                        event.setCancelled(true);
                    } else if (getState() != MatchState.ACTIVE) {
                        event.setCancelled(true);
                    }
                })
                .build()
        );
        events.addListener(
                EventListener.builder(PlayerDisconnectEvent.class)
                        .filter(event -> players.contains(event.getPlayer()))
                        .expireWhen(event -> getState() == MatchState.ENDING)
                        .handler(event -> removePlayer(event.getPlayer()))
                        .build()
        );
        return events;
    }

    public MatchState getState() {
        return state;
    }

    public void setState(MatchState state) {
        if (this.state == state) return;
        switch (state) {
            case STARTING -> {
                MinecraftServer.getGlobalEventHandler().addChild(events());
                server.getMatchManager().addMatch(this);
                players.forEach(p -> {
                    p.sendMessage("Starting!" + " " + arena.getName());
                    p.setGameMode(GameMode.ADVENTURE);
                    p.closeInventory();
                    p.setHealth(20);
                    p.setFood(20);
                    kit.giveKit(p);
                    server.getPlayerStateManager().setState(p, PlayerState.IN_GAME);
                });
                arena.setArenaState(ArenaState.CLOSED);
                blueTeam.forEach(p -> p.teleport(arena.getSpawn1()));
                redTeam.forEach(p -> p.teleport(arena.getSpawn2()));
                MinecraftServer.getSchedulerManager().scheduleTask(new Runnable() {

                    private int seconds = 5;
                    private boolean running = true;

                    @Override
                    public void run() {
                        if (!running) return;
                        if (seconds <= 0) {
                            setState(MatchState.ACTIVE);
                            players.forEach(p -> p.sendMessage("Game started!"));
                            running = false;
                            return;
                        }
                        players.forEach(p -> p.sendMessage("Game starts in " + seconds + "!"));
                        seconds--;
                    }

                }, TaskSchedule.millis(0), TaskSchedule.millis(1000));
            }
            case ACTIVE -> {
                players.forEach(p -> p.sendMessage("Active!"));
                blueTeam.forEach(p -> p.teleport(arena.getSpawn1()));
                redTeam.forEach(p -> p.teleport(arena.getSpawn2()));
            }
            case ENDING -> {
                players.forEach(p -> {
                    p.sendMessage(winner.toString() + " won!");
                    p.teleport(new Pos(0, 42, 0));
                    p.setGameMode(GameMode.ADVENTURE);
                    p.setHealth(20);
                    p.setFood(20);
                    p.getInventory().clear();
                    server.getPlayerStateManager().setState(p, PlayerState.LOBBY);
                });
                arena.setArenaState(ArenaState.OPEN);
                setState(MatchState.ENDED);
                server.getMatchManager().getMatches().remove(this);
            }
        }
        this.state = state;
    }

    public Collection<Player> getPlayers() {
        return players;
    }

    public void removePlayer(Player player) {
        server.getPlayerStateManager().setState(player, PlayerState.LOBBY);
        players.remove(player);
        if (blueTeam.contains(player)) {
            blueSize--;
        } else if (redTeam.contains(player)) {
            redSize--;
        }
        if (redSize <= 0 || blueSize <= 0) {
            startEndTask();
        }
        player.teleport(new Pos(0, 42, 0));
    }

    private void startEndTask() {
        MinecraftServer.getSchedulerManager().scheduleTask(new Runnable() {

            private int seconds = 3;
            private boolean running = true;

            @Override
            public void run() {
                if (!running) return;
                if (seconds <= 0) {
                    if (redSize <= 0) {
                        winner = Team.BLUE;
                    } else if (blueSize <= 0) {
                        winner = Team.RED;
                    }
                    setState(MatchState.ENDING);
                    players.forEach(p -> p.sendMessage("Game ended!"));
                    running = false;
                    return;
                }
                players.forEach(p -> p.sendMessage("Game ends in " + seconds + "!"));
                seconds--;
            }

        }, TaskSchedule.millis(0), TaskSchedule.millis(1000));
    }

}
