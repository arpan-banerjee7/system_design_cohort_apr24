package lld.blackjack.player.command;

import lld.blackjack.model.Shoe;
import lld.blackjack.model.Card;
import lld.blackjack.player.Player;
import lld.blackjack.player.decorator.Bet;
import lld.blackjack.player.decorator.SimpleBet;

public class DoubleDownCommand implements GameCommand {
    private final Player player;
    private final Shoe shoe;

    public DoubleDownCommand(Player player, Shoe shoe) {
        this.player = player;
        this.shoe = shoe;
    }

    @Override
    public void execute() {
        Bet currentBet = player.getBet();
        double newAmount = currentBet.getAmount() * 2;
        player.setBet(new SimpleBet(newAmount));
        Card card = shoe.draw();
        player.getHand().addCard(card);
    }
} 