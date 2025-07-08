package lld.blackjack.phases;

import lld.blackjack.enums.PhaseType;
import lld.blackjack.game.GameEngine;

public interface GamePhase {
    void enter(GameEngine engine);
    void handle(GameEngine engine);
    GamePhase next(GameEngine engine);
    default PhaseType getPhaseType() {
        return PhaseType.UNKNOWN;
    }
} 