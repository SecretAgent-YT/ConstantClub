package club.constant.server.playerstate.manager;

import club.constant.server.playerstate.PlayerState;
import io.github.bloepiloepi.pvp.events.FinalAttackEvent;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import net.minestom.server.event.EventListener;

import java.util.HashMap;

public class PlayerStateManager {

    private final HashMap<Player, PlayerState> hashMap = new HashMap<>();

    public PlayerStateManager() {
        MinecraftServer.getGlobalEventHandler().addListener(
                EventListener.builder(FinalAttackEvent.class)
                        .filter(event -> {
                            return event.getTarget() instanceof Player player && getState(player) != PlayerState.IN_GAME;
                        })
                        .handler(event -> event.setCancelled(true))
                        .build()
        );
    }

    public void setState(Player player, PlayerState state) {
        hashMap.put(player, state);
    }

    public PlayerState getState(Player player) {
        if (!hashMap.containsKey(player)) hashMap.put(player, PlayerState.LOBBY);
        return hashMap.get(player);
    }

}
