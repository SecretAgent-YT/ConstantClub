package club.constant.server.match.commands;

import club.constant.server.ConstantServer;
import club.constant.server.match.Match;
import club.constant.server.playerstate.PlayerState;
import club.constant.server.queue.MatchQueue;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.Player;

public class LeaveCommand extends Command {

    private static final ConstantServer server = ConstantServer.INSTANCE;

    public LeaveCommand() {
        super("leave");
        setCondition(Conditions::playerOnly);
        setDefaultExecutor((sender, context) -> {
            Player player = (Player) sender;
            if (server.getPlayerStateManager().getState(player) == PlayerState.IN_GAME) {
                Match match = server.getMatchManager().getMatch(player);
                match.removePlayer(player);
            } else if (server.getPlayerStateManager().getState(player) == PlayerState.QUEUE) {
                MatchQueue queue = server.getQueueManager().getQueue(player);
                queue.removePlayer(player);
            }
        });
    }

}
