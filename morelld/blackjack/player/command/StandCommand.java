package lld.blackjack.player.command;

import lld.blackjack.player.Player;
import lld.blackjack.model.Shoe;

public class StandCommand implements GameCommand {
    private final Player player;
    private final Shoe shoe;

    public StandCommand(Player player, Shoe shoe) {
        this.player = player;
        this.shoe = shoe;
    }

    @Override
    public void execute() {
        // No action needed for stand
    }
} 