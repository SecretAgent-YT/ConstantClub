package club.constant.server.queue.commands;

import club.constant.server.ConstantServer;
import club.constant.server.playerstate.PlayerState;
import club.constant.server.queue.gui.QueueGUI;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class QueueCommand extends Command {

    private static final ConstantServer server = ConstantServer.INSTANCE;

    public QueueCommand() {
        super("queue", "q");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            if (server.getPlayerStateManager().getState(player) == PlayerState.IN_GAME) {
                player.sendMessage("You may only access this in a lobby!");
                return;
            }
            QueueGUI.open(player);
        });
    }

}
