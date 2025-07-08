package lld.blackjack.game;

import lld.blackjack.config.RuleConfig;
import lld.blackjack.factory.ShoeFactory;
import lld.blackjack.model.Card;
import lld.blackjack.model.Hand;
import lld.blackjack.model.Shoe;
import lld.blackjack.player.Dealer;
import lld.blackjack.player.HumanPlayer;
import lld.blackjack.player.Player;
import lld.blackjack.phases.*;

import java.util.ArrayList;
import java.util.List;


public class GameEngine {
    private final List<HumanPlayer> players = new ArrayList<>();
    private final Dealer dealer;
    private Shoe shoe;
    private final List<GameEventListener> listeners = new ArrayList<>();

    // Default constructor (uses factory)
    public GameEngine(RuleConfig config) {
        this(config, ShoeFactory.createShoe());
    }

    // Constructor for custom shoe
    public GameEngine(RuleConfig config, Shoe shoe) {
        this.shoe = shoe;
        this.dealer = new Dealer(config);
    }

    public Dealer getDealer() {
        return dealer;
    }

    public Shoe getShoe() {
        return shoe;
    }

    public void setShoe(Shoe shoe) {
        this.shoe = shoe;
    }

    public void addPlayer(HumanPlayer player) {
        players.add(player);
    }

    public List<HumanPlayer> getPlayers() {
        return players;
    }

    public void addGameEventListener(GameEventListener listener) {
        listeners.add(listener);
    }

    public void notifyCardDealt(Player player, Card card) {
        for (GameEventListener listener : listeners) {
            listener.onCardDealt(player, card);
        }
    }

    public void notifyHandResolved(Player player, Hand hand) {
        for (GameEventListener listener : listeners) {
            listener.onHandResolved(player, hand);
        }
    }

    // State pattern: run all phases, with dealer blackjack check after dealing
    public void runGamePhases() {
        GamePhase phase = new BettingPhase();
        while (phase != null) {
            phase.enter(this);
            phase.handle(this);
            phase = phase.next(this);
        }
    }
} 