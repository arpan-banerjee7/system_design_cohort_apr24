package lld.blackjack.phases;

import lld.blackjack.enums.Action;
import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.model.Shoe;
import lld.blackjack.player.command.DoubleDownCommand;
import lld.blackjack.player.command.GameCommand;
import lld.blackjack.player.command.HitCommand;
import lld.blackjack.player.command.StandCommand;
import lld.blackjack.model.Card;
import lld.blackjack.player.HumanPlayer;
import lld.blackjack.player.Player;

public class PlayerTurnPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[PlayerTurnPhase] Players take turns...");
    }

    @Override
    public void handle(GameEngine engine) {
        Card dealerUpCard = engine.getDealer().getHand().getCards().get(0);
        var players = engine.getPlayers();
        for (int idx = 0; idx < players.size(); idx++) {
            HumanPlayer player = players.get(idx);
            while (!player.getHand().isBusted()) {

                // Strategy pattern used here
                Action action = player.decideAction(dealerUpCard);

                //Command pattern used here
                GameCommand command = getCommandForAction(action, player, engine.getShoe());
                command.execute();
                System.out.println("Player " + (idx + 1) + " action: " + action + ", hand: " + player.getHand().getCards());
                if (action == Action.STAND || action == Action.DOUBLE_DOWN || action == Action.SURRENDER) {
                    int total = player.getHand().getBestTotal();
                    System.out.println("Player " + (idx + 1) + " stands with total: " + total);
                    System.out.println("----");
                    break;
                }
            }
            if (player.getHand().isBusted()) {
                System.out.println("Player " + (idx + 1) + " busted: " + player.getHand().getCards());
            }
        }
        System.out.println("[PlayerTurnPhase] Complete.");
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
            // Add SPLIT, SURRENDER as needed
            default:
                return new StandCommand(player, shoe);
        }
    }

    @Override
    public GamePhase next(GameEngine engine) {
        return new DealerTurnPhase();
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.PLAYER_TURN; }
} 