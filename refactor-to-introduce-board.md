# Refactor Plan: Introduce `Board`

## Goal

The current code passes `boardSize` as a raw `int` through every layer:
`CommandParser` → `MovePlayerCommand` → `Player` methods. Magic numbers
(`6` for The Bridge, `12` for its destination, `{5,9,14,18,23,27}` for Goose
cells) are duplicated across `Player` and `MovePlayerCommand`.

The target design:
- `Board` owns all game-rule knowledge (size, named cells, special positions, bounce logic)
- `Player` becomes a pure value object: only `name`, `position`, and `move(steps)`
- `MovePlayerCommand` takes a `Board`, not an `int boardSize`
- `CommandParser` stops receiving `boardSize` entirely

The existing integration tests in `MineGooseGameTest` act as a safety net
throughout. They must stay green after every step.

---

## Phase 1 — Introduce `Board` with unit tests

`Board` does not exist yet, so every test written here is a compilation
failure — that counts as RED in TDD.

Create `BoardTest` and add one test at a time, making each pass before adding
the next.

```java
// 1. Board knows its size
new Board(63).size() == 63

// 2. Position 0 is named "Start"
new Board(63).nameOf(0).equals("Start")

// 3. Position 6 is named "The Bridge"
new Board(63).nameOf(6).equals("The Bridge")

// 4. Any other position is named by its number
new Board(63).nameOf(7).equals("7")

// 5. Bridge detection
new Board(63).isBridge(6) == true
new Board(63).isBridge(7) == false

// 6. Bridge has a fixed destination
new Board(63).bridgeDestination() == 12

// 7. Goose detection
new Board(63).isGoose(5) == true
new Board(63).isGoose(6) == false

// 8. Win condition
new Board(63).isWin(63) == true
new Board(63).isWin(62) == false

// 9. Beyond finish
new Board(63).isBeyondFinish(64) == true
new Board(63).isBeyondFinish(63) == false

// 10. Bounce calculation
new Board(63).bouncePositionFor(65) == 61   // 63 - (65 - 63)
```

**REFACTOR after Phase 1:** review `Board` for remaining magic numbers;
consolidate the goose set, bridge position, and bridge destination into named
constants inside `Board`.

---

## Phase 2 — Write unit tests for `Player`'s intended future shape

Write a `PlayerTest` that defines what `Player` should look like after the
refactoring: a pure position container with no `boardSize` dependencies.

```java
// Player tracks name and position
new Player("Pippo", 3).name().equals("Pippo")
new Player("Pippo", 3).position() == 3

// move returns a new Player, original is unchanged
new Player("Pippo", 3).move(4).position() == 7
new Player("Pippo", 3).position() == 3
```

These tests already pass. Writing them explicitly makes the target contract
visible and prevents regression when the rest of `Player` is simplified.

---

## Phase 3 — Simplify `Player`

Delete the four methods that take `boardSize`:

```java
hasWonGiven(int boardSize)
bounceBack(int boardSize)
isBeyondTheFinish(int boardSize)
cellGiven(int boardSize)
```

Deleting them breaks `MovePlayerCommand` — compilation failure. That is
intentional: the compiler drives Phase 4.

---

## Phase 4 — Refactor `MovePlayerCommand` to use `Board`

Change the record signature:

```java
// Before
public record MovePlayerCommand(String playerName, int firstDice, int secondDice, int boardSize)

// After
public record MovePlayerCommand(String playerName, int firstDice, int secondDice, Board board)
```

Rewrite each rule method replacing the deleted `Player` calls with `Board`
equivalents:

| Deleted call | Replacement |
|---|---|
| `player.cellGiven(boardSize)` | `board.nameOf(player.position())` |
| `movedPlayer.isBeyondTheFinish(boardSize)` | `board.isBeyondFinish(movedPlayer.position())` |
| `movedPlayer.hasWonGiven(boardSize)` | `board.isWin(movedPlayer.position())` |
| `movedPlayer.bounceBack(boardSize)` | `new Player(name, board.bouncePositionFor(movedPlayer.position()))` |
| `movedPlayer.position() == 6` in `bridgeRule` | `board.isBridge(movedPlayer.position())` |
| `movedPlayer.move(6)` in `bridgeRule` | `movedPlayer.move(board.bridgeDestination() - movedPlayer.position())` |

**After GREEN:** write focused unit tests for `MovePlayerCommand` directly —
something that was impossible before because the only entry point was the full
game loop:

```java
Board board = new Board(63);
Player at4 = new Player("Pippo", 4);
MoveResult result = new MovePlayerCommand("Pippo", 1, 1, board).move(at4);
// assert bridge jump: result is PlayerMoved, player lands at 12
```

Add one test per rule: bridge, single goose jump, chained goose jumps, bounce,
win, normal move.

**REFACTOR:** the `MovementRule` interface currently passes seven parameters
because `boardSize` and `playerName` had to be threaded through explicitly.
With `board` already on the record and `playerName` also on the record, those
parameters can be removed from the interface — the rule methods close over
`this` instead.

---

## Phase 5 — Update `GooseGame`

Replace the `BOARD_SIZE` integer constant with a `Board` instance and thread
it to `MovePlayerCommand`:

```java
// Before
private static final int BOARD_SIZE = 63;
commandParser.parse(line, BOARD_SIZE);
playersMap.values().stream().noneMatch(p -> p.hasWonGiven(BOARD_SIZE));

// After
private final Board board = new Board(63);
commandParser.parse(line);
playersMap.values().stream().noneMatch(p -> board.isWin(p.position()));
```

Integration tests stay green.

---

## Phase 6 — Clean up `CommandParser`

Remove the `boardSize` parameter from `parse()`. `CommandParser` parses text
and has no business knowing about board geometry.

```java
// Before
public Command parse(String line, int boardSize)

// After
public Command parse(String line)
```

---

## Step summary

| Step | RED | GREEN | REFACTOR |
|---|---|---|---|
| 1 | `BoardTest` — `size()` | Create `Board` | — |
| 2–10 | Each `Board` capability | Add method to `Board` | Consolidate magic numbers |
| 11 | `PlayerTest` — simple shape | Tests already pass | — |
| 12 | Delete `Player` boardSize methods | (breaks `MovePlayerCommand`) | — |
| 13 | — | Fix `MovePlayerCommand` with `Board` | Shrink `MovementRule` signature |
| 14 | `MovePlayerCommandTest` per rule | Tests pass | — |
| 15 | — | Update `GooseGame` | — |
| 16 | — | Clean `CommandParser` | — |

At every step `MineGooseGameTest` must remain green. If it goes red, stop and
investigate before continuing.
