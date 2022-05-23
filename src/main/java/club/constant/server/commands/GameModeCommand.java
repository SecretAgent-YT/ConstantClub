package club.constant.server.commands;

import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.condition.Conditions;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;

public class GameModeCommand extends Command {

    public GameModeCommand() {
        super("gamemode", "gm");
        setCondition(Conditions::playerOnly);
        var gamemode = ArgumentType.Enum("gamemode", GameMode.class);

        addSyntax(((sender, context) -> {
            Player player = (Player) sender;
            player.setGameMode(context.get("gamemode"));
        }), gamemode);
    }

}
