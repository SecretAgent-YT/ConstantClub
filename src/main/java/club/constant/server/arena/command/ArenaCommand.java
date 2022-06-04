package club.constant.server.arena.commands;

import club.constant.server.ConstantServer;
import club.constant.server.arena.Arena;
import club.constant.server.playerstate.PlayerState;
import club.constant.server.queue.gui.QueueGUI;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class ArenaCommand extends Command {

    private static final ConstantServer server = ConstantServer.INSTANCE;

    public ArenaCommand() {
        super("arena");
        setDefaultExecutor((sender, context) -> {
            String string = "Arenas\n";
            for (Arena arena : server.getArenaManager().getArenas()) {
                string += arena.getName() + ": " + arena.getArenaState().toString() + "\n";
            }
            sender.sendMessage(string);
        });
    }

}

