# Blackjack Game LLD (Low-Level Design)

# Overview
This project implements a modular, extensible, and testable Blackjack game in Java. The design demonstrates best practices in object-oriented programming and applies multiple design patterns to ensure clean architecture, maintainability, and flexibility.

---

# Requirements

**R1:** The game uses a shoe containing one or more standard 52-card decks.

**R2:** Each deck has four suits (hearts, diamonds, clubs, spades), each with 13 ranks: Ace, 2-10, Jack, Queen, King.

**R3:** Each card has a point value: Number cards are worth their face value, face cards (Jack, Queen, King) are worth 10, and Ace is worth 1 or 11 (whichever is more favorable).

**R4:** There are two user types: Dealer and Player.

**R5:** Players place bets at the start of each round.

**R6:** The dealer deals two cards to each player and themselves at the start of each round.

**R7:** Both player cards are face up; the dealer shows one card face up and one face down.

**R8:** Players may "hit" (draw a card) as long as their hand total is less than 21.

**R9:** The dealer must hit if their hand total is less than 17.

**R10:** If a hand exceeds 21, that player or dealer "busts" and loses the round.

**R11:** Players may "stand" to end their turn and keep their current hand.

**R12:** If a player's hand is higher than the dealer's (without busting), the player wins and receives a 1:1 payout.

**R13:** A player with a "Blackjack" (Ace + 10-point card as initial hand) receives a 3:2 payout.

**R14:** If the player and dealer tie, the player's bet is returned (push), or the player may replay the round.

**R15:** If a player leaves before the round ends, the dealer wins by default.

**Additional:** The design supports extensibility for side bets, insurance, and multiple players.

---
# Solution

## Bird's Eye View of Core Classes

Below is a high-level summary of the main classes and their key methods/fields. Use this as a quick reference to recall each component's responsibility and API.

---

### Model & Value Objects

| Class    | Responsibility                                            | Key Methods / Fields                                                           |
| -------- | --------------------------------------------------------- | ------------------------------------------------------------------------------ |
| **Card** | Represents a playing card.                                | `Card(Suit, Rank)`, `getSuit()`, `getRank()`, `toString()`                     |
| **Suit** | Enum of the four suits.                                   | `HEARTS`, `DIAMONDS`, `CLUBS`, `SPADES`                                        |
| **Rank** | Enum of 13 ranks with values.                             | `TWO…TEN`, `JACK`, `QUEEN`, `KING`, `ACE` <br> `getValue()`                    |
| **Hand** | Holds cards; calculates totals and checks blackjack/bust. | `addCard(Card)`, `getBestTotal()`, `isBlackjack()`, `isBusted()`, `getCards()` |

---

### Deck & Shoe

| Class           | Responsibility                                        | Key Methods / Fields                                                                                       |
| --------------- | ----------------------------------------------------- | ---------------------------------------------------------------------------------------------------------- |
| **Deck**        | Single 52-card deck; supports shuffle & draw.         | `shuffle()`, `draw()`, `getCards()`, `clear()`, `addCards(...)`                                            |
| **Shoe**        | One or more decks combined and shuffled together.     | `Shoe(int numDecks)`, `shuffle()`, `draw()`, `drawAll()`, `setCards(...)` <br> Implements `Iterable<Card>` |
| **ShoeFactory** | Static factory for default shoe setup (e.g. 6 decks). | `createShoe()`                                                                                             |

---

### Betting

| Class / Interface | Responsibility                          | Key Methods / Fields                                                             |
| ----------------- | --------------------------------------- | -------------------------------------------------------------------------------- |
| **Bet**           | Abstracts a wager and its payout logic. | `getAmount()`, `payout(Hand player, Hand dealer)`                                |
| **SimpleBet**     | Basic 1:1 or blackjack 3:2 payout.      | `SimpleBet(double amount)`                                                       |
| **BetDecorator**  | Base for wrapping/enhancing bets.       | `BetDecorator(Bet inner)`, delegates `getAmount()`                               |
| **InsuranceBet**  | Adds insurance side-bet logic.          | `InsuranceBet(Bet inner, double stake)`, overrides `getAmount()` & `payout(...)` |

---

### Players & Decisions

| Class / Interface     | Responsibility                                            | Key Methods / Fields                                                            |
| --------------------- | --------------------------------------------------------- | ------------------------------------------------------------------------------- |
| **Player** (abstract) | Base for human or dealer; holds `Hand`, `bet`, `balance`. | `placeBet(...)`, `decideAction(Card upCard)`, getters/setters for state         |
| **HumanPlayer**       | Concrete player with injected `DecisionStrategy`.         | `HumanPlayer(double initBal, DecisionStrategy strat)`                           |
| **Dealer**            | Dealer logic with forced hit/stand rules (H17/S17).       | `Dealer(RuleConfig cfg)`, overrides `decideAction(...)`, `playDealerHand(Shoe)` |
| **DecisionStrategy**  | Strategy interface for hit/stand/double logic.            | `decide(Hand hand, Card dealerUpCard)`                                          |
| **BasicStrategy**     | Implements standard basic strategy rules.                 | `decide(...)`                                                                   |

---

### Actions as Commands

| Interface / Class                                             | Responsibility                                     | Key Methods / Fields                                               |
| ------------------------------------------------------------- | -------------------------------------------------- | ------------------------------------------------------------------ |
| **GameCommand**                                               | Encapsulates an action to execute on a player.     | `execute()`                                                        |
| **HitCommand**, **StandCommand**, **DoubleDownCommand**, etc. | Concrete commands that modify `Player` and `Shoe`. | Constructors take `(Player, Shoe)`; `execute()` applies the action |

---

### Game Flow Phases (State Pattern)

| Interface / Class                  | Responsibility                                    | Key Methods / Fields                                                            |
| ---------------------------------- | ------------------------------------------------- | ------------------------------------------------------------------------------- |
| **GamePhase**                      | Abstract phase of a round.                        | `enter(GameEngine)`, `handle(GameEngine)`, `next(GameEngine)`, `getPhaseType()` |
| **BettingPhase**                   | Collects and logs bets.                           | See `handle(...)` implementation                                                |
| **DealingPhase**                   | Deals two cards to each player and dealer.        | Draws from `Shoe`, calls `notifyCardDealt(...)`                                 |
| **PlayerTurnPhase**                | Iterates players; uses Strategy → Command.        | Loops `decideAction` → `getCommandForAction` → `execute()`                      |
| **DealerTurnPhase**                | Dealer draws to 17+ according to rules.           | Uses `Dealer.decideAction(...)`, loops commands                                 |
| **SettlementPhase**                | Compares hands, applies `Bet.payout(...)`.        | Updates balances, notifies listeners                                            |
| **DealerBlackjackSettlementPhase** | Special early settlement if dealer has blackjack. | Overrides `handle(...)` for blackjack payouts                                   |

---

### Supporting Infrastructure

| Class                             | Responsibility                                                      | Key Methods / Fields                                  |
| --------------------------------- | ------------------------------------------------------------------- | ----------------------------------------------------- |
| **GameEngine**                    | Context for phases; holds `players`, `dealer`, `shoe`, `listeners`. | `runGamePhases()`, `addPlayer(...)`, `notify...(...)` |
| **RuleConfig**                    | Singleton-like config for H17/S17, decks, payouts.                  | Getters for rule settings                             |
| **PlayerTurnIterator** (optional) | Custom iterator to traverse active players.                         | Implements `Iterator<Player>`                         |
| **GameEventListener**             | Observer interface for UI/log updates.                              | `onCardDealt(...)`, `onHandResolved(...)`             |

---

## Design Patterns Used

| Pattern       | Where / How                                                                                                     | Why it helps                                                                                                                              |
| ------------- | --------------------------------------------------------------------------------------------------------------- | ----------------------------------------------------------------------------------------------------------------------------------------- |
| **Factory**   | `ShoeFactory.createShoe(numDecks)`                                                                              | Encapsulates the complex setup of N decks, shuffling, etc., so that clients just ask "give me a Shoe"                                     |
| **Strategy**  | `DecisionStrategy` interface (and `BasicStrategy`, etc.)                                                        | Allows you to swap in different player/dealer decision‐making logic at runtime—AI, human UI, card‐counting bot, etc.                      |
| **Decorator** | Wrapping a core `Bet` (or even a `Hand`) with `InsuranceBet`, etc.                                              | Lets you layer in side-bets, insurance, or bonus rules dynamically without changing the underlying Bet logic                              |
| **Command**   | `GameCommand` objects for `HitCommand`, `StandCommand`, `DoubleDownCommand`, etc., invoked by the engine        | Encapsulates each action as an object—enables logging, undo/redo, macro moves, and decouples invoker from executor                        |
| **State**     | `GamePhase` hierarchy (`BettingPhase`, `DealingPhase`, `PlayerTurnPhase`, `DealerTurnPhase`, `SettlementPhase`) | Each phase of a hand is its own class with `enter()`, `handle()`, `next()`—avoids giant switch statements                                 |
| **Iterator**  | `Shoe implements Iterable<Card>` and custom `PlayerTurnIterator`                                                | Standardises traversing cards or cycling through active players; you can swap in a different iterator to tweak order or skip busted hands |
| **Observer**  | UI or logging observers subscribed to events from `GameEngine` or `Player`                                      | Decouples state changes (card dealt, hand resolved) from display/logging logic—great for GUI or audit trails                              |
| **Singleton** | A single shared `RuleConfig`                                                                                    | Guarantees a single source of truth for rule settings (e.g. H17 vs. S17) and prevents conflicting engines                                 |

### Pattern Details
- **Factory:** Used for creating and shuffling the shoe of cards.
- **Strategy:** Allows different player and dealer decision logic to be plugged in.
- **Decorator:** Enables flexible bet enhancements (insurance, side bets, etc.).
- **Command:** Encapsulates player and dealer actions as objects for extensibility.
- **State:** Each phase of the game is a class, making the game flow modular and extensible.
- **Iterator:** Standardizes iteration over cards and players.
- **Observer:** Allows for event-driven extensions (e.g., logging, UI updates).
- **Singleton:** Ensures only one rule configuration is used throughout the game.

---

## Rationale Behind Usage of Design Patterns

### State Design Pattern in This Codebase

The **State** design pattern allows an object to alter its behavior when its internal state changes. The object will appear to change its class. This pattern is used to encapsulate varying behavior for the same object based on its internal state, and to delegate state-specific behavior to state objects. It helps avoid large conditional statements and makes state transitions explicit and maintainable.

#### How It's Used in This Blackjack Code

- **What is the "State"?**
  - The state is the current **phase** of the Blackjack game (e.g., Betting, Dealing, Player Turn, Dealer Turn, Settlement).
  - Each phase is represented by a class implementing the `GamePhase` interface (e.g., `BettingPhase`, `DealingPhase`, etc.).

- **What Object's Behavior Changes?**
  - The **GameEngine** object's behavior changes depending on the current phase (state) of the game.
  - The engine delegates the handling of game logic to the current phase object.

- **How is the Pattern Implemented?**
  - **State Interface:** `GamePhase` interface defines methods like `enter()`, `handle()`, and `next()`.
  - **Concrete States:** Each phase (e.g., `BettingPhase`, `DealingPhase`, etc.) implements `GamePhase` and encapsulates the logic for that phase.
  - **Context:** `GameEngine` acts as the context. It holds a reference to the current `GamePhase` and delegates phase-specific logic to it.

  Example:
  ```java
  public void runGamePhases() {
      GamePhase phase = new BettingPhase();
      while (phase != null) {
          phase.enter(this);
          phase.handle(this);
          // State transition logic
          if (phase.getPhaseType() == PhaseType.DEALING && dealer.getHand().isBlackjack()) {
              phase = new DealerBlackjackSettlementPhase();
          } else {
              phase = phase.next(this);
          }
      }
  }
  ```
  - Here, the `GameEngine` does not need to know the details of each phase. It simply calls `enter()`, `handle()`, and then transitions to the next phase by calling `next()` on the current phase.
  - Each phase knows what the next phase should be, and what logic to execute.

- **How Does This Allow Behavior Change?**
  - When the game is in the **BettingPhase**, the engine's behavior is to collect bets.
  - When the game transitions to the **DealingPhase**, the engine's behavior is to deal cards.
  - In the **PlayerTurnPhase**, the engine's behavior is to allow players to take actions.
  - Each phase encapsulates its own rules and logic, so the engine's behavior changes as the phase (state) changes.

  **This is the essence of the State pattern:**
  > The same object (`GameEngine`) changes its behavior (what happens when you call `handle()`, etc.) depending on its internal state (the current `GamePhase`).

#### Benefits in This Code
- **Extensibility:** Add new phases (states) without modifying the engine's core logic.
- **Maintainability:** Each phase's logic is isolated, making it easy to update or debug.
- **Clarity:** No giant `switch` or `if` statements in the engine; phase transitions are explicit and easy to follow.

#### Summary Table

| State Pattern Role | Your Code Example |
|--------------------|------------------|
| Context            | `GameEngine`     |
| State Interface    | `GamePhase`      |
| Concrete States    | `BettingPhase`, `DealingPhase`, `PlayerTurnPhase`, etc. |
| State Transition   | `phase.next(this)` or custom logic in `runGamePhases()` |

**In Short:**
- **State:** The current phase of the game (`GamePhase`).
- **Object whose behavior changes:** `GameEngine`.
- **How:** By delegating to the current phase object, which encapsulates phase-specific logic and transitions.

#### State Pattern vs. Chain of Responsibility (CoR)

While the State and Chain of Responsibility (CoR) patterns can look similar—both may involve passing control from one object to another—they serve different purposes:

- **State Pattern:** Used when an object (like the GameEngine) needs to change its behavior based on its internal state (the current phase). Only one state is active at a time, and the context delegates all behavior to that state.
- **Chain of Responsibility Pattern:** Used to pass a request along a chain of handlers, where each handler can choose to process the request or pass it along. Multiple handlers may process the same request, or it may stop at the first that can handle it.

**Why not use CoR for game phases?**
- In Blackjack, only one phase is active at a time. The game is never in multiple phases simultaneously, and each phase knows what comes next.
- CoR is more appropriate for scenarios like validation chains or event processing, not for modeling mutually exclusive, sequential phases.

**Summary Table:**

| Pattern                   | Purpose                                      | Example Use Case in Games                |
|---------------------------|----------------------------------------------|------------------------------------------|
| State                     | Change object behavior as state changes      | Game phases (betting, dealing, etc.)     |
| Chain of Responsibility   | Pass request through chain of handlers       | Action validation, event processing      |

In this codebase, the State pattern is the correct choice for modeling game phases. CoR could be used elsewhere, such as for validating player actions or processing game events.

---

### Command Design Pattern in This Codebase

The **Command** design pattern encapsulates a request as an object, thereby allowing for parameterization of clients with queues, requests, and operations, and supporting undoable operations. It decouples the object that invokes the operation from the one that knows how to perform it.

#### Textbook Command Pattern Structure
- **Command Interface:** Declares a method for executing a command (`void execute()`).
- **ConcreteCommand:** Implements the Command interface and defines the binding between an action and a receiver.
- **Invoker:** Asks the command to carry out the request.
- **Receiver:** Knows how to perform the operations associated with carrying out a request.

**Example (textbook):**
```java
interface Command { void execute(); }
class LightOnCommand implements Command {
    private Light light;
    public LightOnCommand(Light light) { this.light = light; }
    public void execute() { light.on(); }
}
// Invoker
remote.setCommand(new LightOnCommand(light));
remote.pressButton();
```

#### How It's Used in This Blackjack Code
- **Command Interface:** `GameCommand` with `void execute()`.
- **Concrete Commands:** `HitCommand`, `StandCommand`, `DoubleDownCommand`, etc., each encapsulating the logic for a specific player action.
- **Invoker:** The game engine or phase, which creates and executes the appropriate command based on the player's decision.
- **Receiver:** The `Player` and related game objects (e.g., `Hand`, `Shoe`) that are manipulated by the command.

**Example from this codebase:**
```java
GameCommand cmd = new HitCommand(player, shoe);
cmd.execute();
```

#### Why Use the Command Pattern Here?
- **Single Responsibility Principle:** Player only decides what to do; command classes execute the action.
- **Open/Closed Principle:** New actions are added by creating new command classes, not by modifying existing code or adding to a switch statement.
- **Extensibility:** Supports features like logging, undo/redo, macros, and flexible action handling.
- **Decoupling:** The engine/phase does not need to know the details of how each action is performed.

#### Why Not Use a Switch or Template Method?
- **Switch statements** in `Player` or the engine would violate SRP and OCP, making the code harder to extend and maintain.
- **Template Method** is for defining the skeleton of an algorithm in a base class and letting subclasses override steps. Here, each action is a distinct operation, not a step in a fixed algorithm.

#### Summary Table
| Command Pattern Role | Your Code Example |
|---------------------|------------------|
| Command Interface   | `GameCommand`    |
| Concrete Commands   | `HitCommand`, `StandCommand`, `DoubleDownCommand`, etc. |
| Invoker             | Engine/Phase     |
| Receiver            | `Player`, `Hand`, `Shoe` |

**In Short:**
- The Command pattern keeps action logic modular, testable, and extensible.
- Adding new player actions is as simple as creating a new command class.

---
### Decorator Design Pattern in This Codebase

#### Why Decorator for Insurance Bets (and Not Command)

Insurance in Blackjack is not a one-off action, but an augmentation of the player's bet contract. The **Decorator** pattern is ideal for this because:

- **Semantics:**
  - Decorator answers: "I have a core bet, and now I want to augment its behavior—specifically, how much is at risk and how the payout is calculated."
  - Command answers: "I want to perform a one-off operation right now—'Hit', 'Stand', or 'Double Down'—and then I'm done."
  - Insurance is part of the bet's definition and lives until settlement, not a one-off action.

- **Lifecycle:**
  - A Command fires, does its thing, and disappears. If you used an InsuranceCommand, you'd have to attach state back onto the player or bet, or queue up a second "settle insurance" command—complicating your flow.
  - A Decorator stays wrapped around the Bet from the moment you place it until settlement, automatically contributing its extra logic when you call payout or getAmount.

- **Stackability & Open/Closed:**
  - With Decorators, you can stack multiple bet types (insurance, side bets, etc.) without changing core classes. Each new side-bet is just another decorator layer.
  - With Commands, you'd need distinct commands for each side-bet placement and additional commands for each settlement phase, scattering payout logic across multiple spots.

**TL;DR:**
- Use **Decorator** for "this bet now has extra rules/payout logic attached."
- Use **Command** for "the player is about to do a discrete action right now."

Insurance isn't an action like "Hit" or "Stand"—it's a modification of your betting contract, so it belongs in the Decorator pattern.

---

## Why Game Phases Are Modeled as States (and Not Outcomes)

A common question in designing a Blackjack game engine is: Why do we model the states of the game as *phases* (like Betting, Dealing, Player Turn, Dealer Turn, Settlement) rather than as *outcomes* (like Player Wins, Player Loses, Tie, etc.)?

**Outcome-based State Example**
- **Initial state:** The player places bets and the dealer deals the cards among themselves and the player.
- **Final states:**
    - The player wins with 3:2 of the bet.
    - The player wins with an equal bet.
    - The match is tied.
    - The player loses.

**Why We Chose Phases as States**
- **Game Flow Modeling:** The game naturally progresses through a series of phases, each with its own rules and allowed actions. Modeling these as states (using the State pattern) makes the flow explicit and modular.
- **Extensibility:** New phases (e.g., Insurance, Side Bets, Splitting Hands) can be added without disrupting the core logic. If we modeled only outcomes, adding new rules or intermediate steps would be much harder.
- **Clarity & Maintainability:** Each phase encapsulates its own logic for what actions are allowed, what transitions are possible, and what data is relevant. This avoids giant switch statements and makes the code easier to reason about and test.
- **Outcome as a Result, Not a State:** Outcomes (win, lose, tie) are the *result* of progressing through the phases, not states the game can be in for any length of time. The game spends most of its time in phases, and only briefly in an outcome before resetting for the next round.

In summary, modeling phases as states aligns with both the real-world flow of Blackjack and best practices in extensible, maintainable software design.

---

## How to Run

1. **Compile:**
   - Ensure you are in the `src/lld/BlackJackGameLLD` directory.
   - Compile all Java files:
     ```
     javac com/example/blackjack/*.java
     ```
2. **Run:**
   - Run the main class:
     ```
     java com.example.blackjack.Main
     ```

---

## Extensibility & Testing
- The design supports adding new bet types, player strategies, and game phases with minimal changes.
- You can inject custom shoes for deterministic testing.
- The code is modular and suitable for unit testing of all major components.

---

## Author
- Designed and implemented by Arpan Banerjee (and ChatGPT assistant)

---

## License
This project is for educational purposes. Feel free to use and modify as needed. 