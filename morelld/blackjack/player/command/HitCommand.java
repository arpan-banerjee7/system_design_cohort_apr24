package lld.blackjack.player.command;

import lld.blackjack.player.Player;
import lld.blackjack.model.Shoe;
import lld.blackjack.model.Card;

public class HitCommand implements GameCommand {
    private final Player player;
    private final Shoe shoe;

    public HitCommand(Player player, Shoe shoe) {
        this.player = player;
        this.shoe = shoe;
    }

    @Override
    public void execute() {
        Card card = shoe.draw();
        player.getHand().addCard(card);
    }
} 