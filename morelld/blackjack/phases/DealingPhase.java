package lld.blackjack.phases;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;
import lld.blackjack.model.Card;
import lld.blackjack.player.HumanPlayer;

public class DealingPhase implements GamePhase {
    @Override
    public void enter(GameEngine engine) {
        System.out.println("[DealingPhase] Dealing cards...");
    }

    @Override
    public void handle(GameEngine engine) {
        var players = engine.getPlayers();
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < players.size(); j++) {
                HumanPlayer player = players.get(j);
                Card card = engine.getShoe().draw();
                player.getHand().addCard(card);
                System.out.println("Dealt to Player " + (j + 1) + ": " + card);
            }
            Card dealerCard = engine.getShoe().draw();
            engine.getDealer().getHand().addCard(dealerCard);
            System.out.println("Dealt to dealer: " + dealerCard);
        }
        // Print hands
        for (int j = 0; j < players.size(); j++) {
            HumanPlayer player = players.get(j);
            System.out.println("Player " + (j + 1) + " hand: " + player.getHand().getCards());
        }
        System.out.println("Dealer hand: " + engine.getDealer().getHand().getCards());
    }

    @Override
    public GamePhase next(GameEngine engine) {
        if (engine.getDealer().getHand().isBlackjack()) {
            return new DealerBlackjackSettlementPhase();
        }
        System.out.println("[DealingPhase] Complete.\n");
        return new PlayerTurnPhase();
    }

    @Override
    public PhaseType getPhaseType() { return PhaseType.DEALING; }
} 