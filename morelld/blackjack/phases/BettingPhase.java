package lld.blackjack.phases;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.player.HumanPlayer;

public class BettingPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[BettingPhase] Players are placing bets...");
    }

    @Override
    public void handle(GameEngine engine) {
        var players = engine.getPlayers();
        for (int i = 0; i < players.size(); i++) {
            HumanPlayer player = players.get(i);
            player.placeBet(50);
            System.out.println("Player " + (i + 1) + " bet: 50");
        }
        System.out.println("Debug: Bets and balances after betting:");
        for (int i = 0; i < players.size(); i++) {
            HumanPlayer player = players.get(i);
            System.out.println("Player " + (i + 1) + " bet: " + player.getBet().getAmount() + ", balance after bet: " + player.getBalance());
        }
    }

    @Override
    public GamePhase next(GameEngine engine) {
        System.out.println("[BettingPhase] Complete.");
        System.out.println();
        return new DealingPhase();
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.BETTING; }
} 