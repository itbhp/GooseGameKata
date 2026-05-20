# Refactor Plan: Support Multiple UIs

## Why the current design cannot support a UI

`GooseGame` currently does three unrelated things:

1. **Owns game state** — `Map<String, Player> playersMap`
2. **Runs the game loop** — reads text lines from stdin, parses them, dispatches commands
3. **Formats and writes output** — prints strings to stdout

A UI adapter (Swing, web, JavaFX) bypasses #2 entirely — there is no text input to
parse, no `quit` keyword, no game loop reading lines. It would provide its own event
model (button clicks, form submits). It also needs a different mechanism for #3
(visual components, not stdout). Only #1 — the game state and rule execution — is
actually reusable.

The seam we need to expose is the game logic itself, free of any I/O.

---

## Design pattern: Hexagonal Architecture (Ports and Adapters)

The game domain becomes the **hexagon** — a pure use-case facade with no I/O.
Each UI is an **adapter** that drives the domain through a well-defined interface.

```
┌──────────────────────┐      ┌──────────────────────┐
│   ConsoleAdapter     │      │   SwingAdapter / etc. │
│                      │      │                       │
│  game loop           │      │  button click handler │
│  CommandParser       │      │  board panel renderer │
│  stdout formatting   │      │  event listeners      │
└──────────┬───────────┘      └────────────┬──────────┘
           │                               │
           └───────────────┬───────────────┘
                           │  calls
                           ▼
              ┌────────────────────────┐
              │         Game           │  ← the port (domain facade)
              │                        │
              │  addPlayer(name)       │  no I/O
              │    → AddResult         │  no string formatting
              │  movePlayer(name,d1,d2)│  no game loop
              │    → MoveResult        │  holds Map<String, Player>
              │  isOver() → boolean    │
              │  players() → Collection│
              └────────────┬───────────┘
                           │
              ┌────────────▼───────────┐
              │      Domain Model      │
              │                        │
              │  Board, Player         │
              │  Commands, Results     │
              └────────────────────────┘
```

`Game` is the boundary between domain and adapters. It returns rich result types
(`AddResult`, `MoveResult`) that each adapter can interpret in its own way — the
console formats them as strings; a UI can render them as board state changes.

---

## Secondary concern: pre-formatted strings in results

Results currently carry pre-formatted strings (e.g., `PlayerMoved.message()`).
These strings are console-centric. A graphical UI will likely ignore them in favour
of visual rendering derived from `player.position()`. Results already carry the
structured data (the new `Player` state) alongside the message, so adapters can
choose which they use.

This is sufficient for a first iteration. A follow-up phase is described at the
end.

---

## Target package structure

```
it.twinsbrain.dojos/
├── Game.java                        ← new: domain use-case facade
├── Board.java
├── Player.java
├── commands/
│   ├── Command.java
│   ├── AddPlayerCommand.java
│   └── MovePlayerCommand.java
├── result/
│   └── ... (unchanged)
└── adapters/
    └── console/
        ├── ConsoleAdapter.java      ← renamed from GooseGame
        └── CommandParser.java      ← moved here (console-only concern)
```

---

## TDD plan

### Phase 1 — Write `GameTest` (RED)

`Game` does not exist. Every test is a compilation failure — that is the RED state.

```java
// GameTest — drives the full API of the new class
@Test void addingANewPlayerReturnsPlayerAdded()
@Test void addingAnExistingPlayerReturnsPlayerAlreadyPresent()
@Test void movingAPlayerReturnsPlayerMoved()
@Test void movingAPlayerToTheWinCellReturnsGameFinished()
@Test void movingAPlayerBeyondTheFinishReturnsPlayerBouncedBack()
@Test void gameIsNotOverBeforeAnyoneWins()
@Test void gameIsOverAfterAPlayerWins()
@Test void playersReflectsCurrentPositionsAfterMoves()
```

These tests are pure unit tests — no streams, no strings, no I/O of any kind. They
verify the domain contract in isolation.

---

### Phase 2 — Implement `Game` (GREEN)

Extract the state and execution logic from `GooseGame` into the new `Game` class.

```java
public class Game {
  private final Board board;
  private final Map<String, Player> players = new HashMap<>();

  public Game(Board board) { this.board = board; }

  public AddResult addPlayer(String name) { ... }
  public MoveResult movePlayer(String name, int d1, int d2) { ... }
  public boolean isOver() { ... }
  public Collection<Player> players() { ... }
}
```

`addPlayer` contains the logic currently in `GooseGame.execute(AddPlayerCommand)`.
`movePlayer` contains the logic currently in `GooseGame.execute(MovePlayerCommand)`.
Neither method touches I/O.

The integration tests (`MineGooseGameTest`) still pass — `GooseGame` has not
changed yet.

---

### Phase 3 — Refactor `GooseGame` to delegate to `Game` (REFACTOR)

`GooseGame` stops managing `playersMap` and executing commands itself. It creates a
`Game` and delegates every command to it, then formats the returned result to stdout.

```java
// GooseGame after refactoring
public class GooseGame {
  private final Game game = new Game(new Board(63));
  private final CommandParser parser = new CommandParser();
  // ... input / output as before

  public void play() throws IOException {
    String line;
    while (!"quit".equals(line = input.readLine())) {
      try {
        // dispatch to game, format result
      } catch (...) { ... }
    }
    if (!game.isOver()) output.print("See you!");
  }
}
```

`GooseGame` is now a thin adapter: loop + parse + format. `Game` owns the state.

`MineGooseGameTest` remains green throughout — observable behaviour is unchanged.

---

### Phase 4 — Rename and relocate (REFACTOR)

| Before | After |
|---|---|
| `GooseGame` | `adapters.console.ConsoleAdapter` |
| `CommandParser` | `adapters.console.CommandParser` |

`Main` is updated to instantiate `ConsoleAdapter`. All tests pass. The package
structure now makes the architecture visible.

---

### Phase 5 — Prove the seam with a UI adapter test (RED → GREEN)

Write a test that exercises `Game` directly, with no console involvement. This is
both a proof of concept for a future UI adapter and a demonstration that the seam
is clean.

```java
class GameApiTest {
  private final Game game = new Game(new Board(63));

  @Test
  void uiCanAddAPlayerAndObserveTheResult() {
    var result = game.addPlayer("Pippo");
    assertThat(result, instanceOf(PlayerAdded.class));
    assertThat(game.players(), hasSize(1));
  }

  @Test
  void uiCanMoveAPlayerAndReadItsNewPosition() {
    game.addPlayer("Pippo");
    var result = game.movePlayer("Pippo", 4, 2);
    assertThat(result, instanceOf(PlayerMoved.class));
    assertThat(((PlayerMoved) result).player().position(), equalTo(12)); // lands on bridge → jumps to 12
  }

  @Test
  void uiKnowsWhenTheGameIsOver() {
    game.addPlayer("Pippo");
    game.movePlayer("Pippo", 58, 2); // → 60
    game.movePlayer("Pippo", 1, 2);  // → 63, wins
    assertThat(game.isOver(), is(true));
  }
}
```

A real UI adapter would call the same three `Game` methods from its event handlers
and use `player.position()` (available in every result) to update the visual board.

---

## Step summary

| Step | RED | GREEN | REFACTOR |
|---|---|---|---|
| 1 | `GameTest` — full API | — | — |
| 2 | — | Implement `Game`, extract state and logic from `GooseGame` | — |
| 3 | — | `GooseGame` delegates to `Game` | Simplify `GooseGame` |
| 4 | — | — | Rename to `ConsoleAdapter`, move `CommandParser` |
| 5 | `GameApiTest` — UI-style calls | Already passes (seam exists) | — |

`MineGooseGameTest` stays green at every step.

---

## Follow-up: separate message formatting from domain results

Currently `MovePlayerCommand` builds formatted strings inside the domain (e.g.,
`"Pippo rolls 1, 2. Pippo moves from Start to 3"`). These strings live in result
records as `.message()`. This is a console concern embedded in domain objects.

If a UI needs richer or different representations of the same events, the right
next step is:

1. Introduce a `MessageFormatter` interface in the console package
2. Move the string-building logic out of `MovePlayerCommand` into a
   `ConsoleMessageFormatter` implementation
3. Results carry only structured data; `ConsoleAdapter` uses `ConsoleMessageFormatter`
   to produce the strings it prints
4. A future `SwingMessageFormatter` could produce HTML, or the UI could skip
   formatting entirely and derive its display from `player.position()`

This is not needed for the first working UI and can be deferred until a second
adapter with genuinely different formatting requirements is being built.
