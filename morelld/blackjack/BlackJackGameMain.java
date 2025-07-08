/**
 * Blackjack Game LLD Driver code
 */
package lld.blackjack;

import lld.blackjack.config.RuleConfig;
import lld.blackjack.enums.Rank;
import lld.blackjack.enums.Suit;
import lld.blackjack.game.GameEngine;
import lld.blackjack.model.Shoe;
import lld.blackjack.model.Card;
import lld.blackjack.player.strategy.BasicStrategy;
import lld.blackjack.player.HumanPlayer;
import java.util.*;


public class BlackJackGameMain {
    public static void main(String[] args) {
        RuleConfig config = RuleConfig.getInstance(6, false, true, 1.5);

        // --- ROUND 1: Dealer gets blackjack ---
        System.out.println("\n--- ROUND 1: Dealer gets blackjack ---");
        List<Card> customCards1 = new ArrayList<>();
        // Deal order: p1, p2, p3, p4, dealer, p1, p2, p3, p4, dealer
        customCards1.add(new Card(Suit.HEARTS, Rank.FIVE));    // p1
        customCards1.add(new Card(Suit.CLUBS, Rank.SIX));      // p2
        customCards1.add(new Card(Suit.DIAMONDS, Rank.SEVEN)); // p3
        customCards1.add(new Card(Suit.SPADES, Rank.EIGHT));   // p4
        customCards1.add(new Card(Suit.HEARTS, Rank.ACE));     // dealer (upcard)
        customCards1.add(new Card(Suit.CLUBS, Rank.NINE));     // p1
        customCards1.add(new Card(Suit.DIAMONDS, Rank.FOUR));  // p2
        customCards1.add(new Card(Suit.SPADES, Rank.THREE));   // p3
        customCards1.add(new Card(Suit.HEARTS, Rank.TWO));     // p4
        customCards1.add(new Card(Suit.SPADES, Rank.KING));    // dealer (hole, blackjack)
        Shoe customShoe1 = new Shoe(2); // 2-deck shoe
        customShoe1.setCards(customCards1);
        GameEngine engine = new GameEngine(config, customShoe1);
        HumanPlayer player1 = new HumanPlayer(1000, new BasicStrategy());
        HumanPlayer player2 = new HumanPlayer(1000, new BasicStrategy());
        HumanPlayer player3 = new HumanPlayer(1000, new BasicStrategy());
        HumanPlayer player4 = new HumanPlayer(1000, new BasicStrategy());
        engine.addPlayer(player1);
        engine.addPlayer(player2);
        engine.addPlayer(player3);
        engine.addPlayer(player4);
        System.out.println("Balances before ROUND 1:");
        System.out.println("Player1: " + player1.getBalance());
        System.out.println("Player2: " + player2.getBalance());
        System.out.println("Player3: " + player3.getBalance());
        System.out.println("Player4: " + player4.getBalance());
        engine.runGamePhases();
        System.out.println("Balances after ROUND 1:");
        System.out.println("Player1: " + player1.getBalance());
        System.out.println("Player2: " + player2.getBalance());
        System.out.println("Player3: " + player3.getBalance());
        System.out.println("Player4: " + player4.getBalance());

        // --- ROUND 2: Players take actions, one busts, reveal hands ---
        System.out.println("\n\n--- ROUND 2: Player actions, bust, and winner ---");
        List<Card> customCards2 = new ArrayList<>();
        // Initial deal (2 rounds, 5 players: p1, p2, p3, p4, dealer)
        customCards2.add(new Card(Suit.HEARTS, Rank.SIX));     // p1
        customCards2.add(new Card(Suit.CLUBS, Rank.SEVEN));    // p2
        customCards2.add(new Card(Suit.DIAMONDS, Rank.EIGHT)); // p3
        customCards2.add(new Card(Suit.SPADES, Rank.NINE));    // p4
        customCards2.add(new Card(Suit.HEARTS, Rank.FIVE));    // dealer (upcard)
        customCards2.add(new Card(Suit.CLUBS, Rank.FOUR));     // p1
        customCards2.add(new Card(Suit.DIAMONDS, Rank.THREE)); // p2
        customCards2.add(new Card(Suit.SPADES, Rank.TWO));     // p3
        customCards2.add(new Card(Suit.HEARTS, Rank.SEVEN));   // p4
        customCards2.add(new Card(Suit.SPADES, Rank.SIX));     // dealer (hole)
        // Player 1 hits: 10 (DIAMONDS) to push with dealer
        customCards2.add(new Card(Suit.DIAMONDS, Rank.TEN));   // p1 hit (push with dealer)
        // Player 2 hits: 2 (CLUBS), 3 (HEARTS), 4 (SPADES)
        customCards2.add(new Card(Suit.CLUBS, Rank.TWO));      // p2 hit 1
        customCards2.add(new Card(Suit.HEARTS, Rank.THREE));   // p2 hit 2
        customCards2.add(new Card(Suit.SPADES, Rank.FOUR));    // p2 hit 3
        // Player 3 hits: 5 (DIAMONDS), ACE (HEARTS), 5 (CLUBS)
        customCards2.add(new Card(Suit.DIAMONDS, Rank.FIVE));  // p3 hit 1
        customCards2.add(new Card(Suit.HEARTS, Rank.ACE));     // p3 hit 2
        customCards2.add(new Card(Suit.CLUBS, Rank.FIVE));     // p3 hit 3
        // Player 4 hits: 6 (SPADES) to bust
        customCards2.add(new Card(Suit.SPADES, Rank.SIX));     // p4 hit (busts at 22)
        // Dealer hits: 9 (DIAMONDS)
        customCards2.add(new Card(Suit.DIAMONDS, Rank.NINE));  // dealer hit
        // Add a few extra cards for safety
        customCards2.add(new Card(Suit.CLUBS, Rank.THREE));
        customCards2.add(new Card(Suit.HEARTS, Rank.FOUR));
        customCards2.add(new Card(Suit.SPADES, Rank.FIVE));
        Shoe customShoe2 = new Shoe(2);
        customShoe2.setCards(customCards2);
        engine.setShoe(customShoe2);
        engine.runGamePhases();
        System.out.println("Balances after ROUND 2:");
        System.out.println("Player1: " + player1.getBalance());
        System.out.println("Player2: " + player2.getBalance());
        System.out.println("Player3: " + player3.getBalance());
        System.out.println("Player4: " + player4.getBalance());
    }
} 