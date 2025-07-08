package lld.blackjack.phases;

import lld.blackjack.enums.Action;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.player.command.DoubleDownCommand;
import lld.blackjack.player.command.GameCommand;
import lld.blackjack.player.command.HitCommand;
import lld.blackjack.player.command.StandCommand;
import lld.blackjack.player.Dealer;
import lld.blackjack.player.Player;
import lld.blackjack.model.Shoe;



public class DealerTurnPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[DealerTurnPhase] Dealer's turn...");
    }

    @Override
    public void handle(GameEngine engine) {
        Dealer dealer = engine.getDealer();
        while (true) {
            Action action = dealer.decideAction(null);
            GameCommand command = getCommandForAction(action, dealer, engine.getShoe());
            command.execute();
            System.out.println("Dealer action: " + action + ", hand: " + dealer.getHand().getCards());
            if (action == Action.STAND) break;
        }
        if (dealer.getHand().isBusted()) {
            System.out.println("Dealer busted: " + dealer.getHand().getCards());
        }
        System.out.println("[DealerTurnPhase] Complete.");
        System.out.println();
    }

    private GameCommand getCommandForAction(Action action, Player player, Shoe shoe) {
        switch (action) {
            case HIT:
                return new HitCommand(player, shoe);
            case STAND:
                return new StandCommand(player, shoe);
            case DOUBLE_DOWN:
                return new DoubleDownCommand(player, shoe);
            default:
                return new StandCommand(player, shoe);
        }
    }

    @Override
    public GamePhase next(GameEngine engine) {
        return new SettlementPhase();
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.DEALER_TURN; }
} 