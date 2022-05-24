package club.constant.server.queue;

import club.constant.server.ConstantServer;
import club.constant.server.arena.Arena;
import club.constant.server.arena.type.ArenaType;
import club.constant.server.kit.Kit;
import club.constant.server.match.Match;
import club.constant.server.playerstate.PlayerState;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventFilter;
import net.minestom.server.event.EventListener;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerDisconnectEvent;
import net.minestom.server.event.trait.PlayerEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class MatchQueue implements Comparable<MatchQueue> {

    private static final ConstantServer server = ConstantServer.INSTANCE;

    private final UUID uuid = UUID.randomUUID();
    private final Queue<Player> queue = new LinkedList<>();
    private final int maxSize;

    private boolean ended = false;

    public MatchQueue(int maxSize) {
        this.maxSize = maxSize;
        server.getQueueManager().addQueue(this);
        MinecraftServer.getGlobalEventHandler().addChild(events());
    }

    public Queue<Player> getQueue() {
        return queue;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public UUID getUUID() {
        return uuid;
    }

    public EventNode<PlayerEvent> events() {
        @NotNull EventNode<PlayerEvent> events = EventNode.type(uuid.toString(), EventFilter.PLAYER);
        events.addListener(
                EventListener.builder(PlayerDisconnectEvent.class)
                        .filter(event -> queue.contains(event.getPlayer()))
                        .expireWhen(event -> ended == false)
                        .handler(event -> removePlayer(event.getPlayer()))
                        .build()
        );
        return events;
    }

    public void addPlayer(Player player) {
        queue.add(player);
        server.getPlayerStateManager().setState(player, PlayerState.QUEUE);
        if (queue.size() >= maxSize) {
            start();
        }
    }

    public void removePlayer(Player player) {
        queue.remove(player);
        server.getPlayerStateManager().setState(player, PlayerState.LOBBY);
        if (queue.size() <= 0) {
            server.getQueueManager().getQueues().remove(this);
        }
    }

    public void start() {
        server.getQueueManager().getQueues().remove(this);
        List<Player> blueTeam = new ArrayList<>();
        List<Player> redTeam = new ArrayList<>();
        boolean onRed = false;
        for (int i = 0; i < queue.size() + 1; i++) {
            System.out.println(onRed);
            Player player = queue.poll();
            if (!onRed) {
                blueTeam.add(player);
            } else {
                redTeam.add(player);
            }
            onRed = !onRed;
        }
        Match match = new Match(new Kit(), new Arena("Arena", new Pos(0, 42, 0), new Pos(0, 42, 10, 180, 0), ArenaType.PVP), blueTeam, redTeam);
        match.start();
        ended = true;
    }

    public ItemStack getItemStack() {
        return ItemStack.builder(Material.PAPER)
                .displayName(getQueue().peek().getName())
                .lore(Component.text(getQueue().size() + "/" + getMaxSize()))
                .set(Tag.UUID("queue"), getUUID())
                .build();
    }

    @Override
    public int compareTo(@NotNull MatchQueue queue) {
        return Integer.compare(queue.getMaxSize(), getMaxSize());
    }

}
