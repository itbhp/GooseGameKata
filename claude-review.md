# Code Review — GooseGameKata

> Reviewed as a senior software engineer, XP practitioner, and TDD advocate, evaluating this as a home exercise submission for a technical interview.

---

## Overall

This is a competent submission. Modern Java features (sealed interfaces, records, switch expressions with pattern matching) are used appropriately and show real familiarity with the language. The sealed `Result` hierarchy is a good design choice. But several things would raise questions in an interview, particularly for an XP/TDD role.

---

## TDD — the biggest concern

**All tests exercise the full I/O stack.** Every test goes: raw string → `CommandParser` → `GooseGame` → `Command` → `Player` → `Result` → formatted output. When a test fails, you cannot tell which layer broke. This is an integration test suite, not a TDD-driven one.

An XP practitioner would expect to see:

- Unit tests for `CommandParser` in isolation — none exist at all
- Unit tests for `Player` (position arithmetic, bounce logic, `cellGiven`)
- Unit tests for individual movement rules in `MovePlayerCommand`
- Unit tests for `AddPlayerCommand.createIfNotExists`
- Integration tests like these as the final confidence check

The test DSL (`GameTester`) is a nice touch, but it hides the absence of smaller tests. Ask the candidate: *"How would you debug a broken bounce calculation using only these tests?"*

---

## `boardSize` leaks through every layer

`boardSize` travels as a parameter through `CommandParser.parse()` → `MovePlayerCommand` constructor → `Player.hasWonGiven(boardSize)` / `Player.bounceBack(boardSize)` / `Player.cellGiven(boardSize)` / `Player.isBeyondTheFinish(boardSize)`.

`CommandParser` has no business knowing about board size — it parses text. Passing it there just so `MovePlayerCommand` can store it is a design smell. And a `Player` taking the board boundary in every single method is awkward; `Player` is a value object and its API shouldn't require callers to always supply external context.

A `Board` abstraction (even a tiny one) would centralize this constant and remove the noise.

---

## Magic number duplication: The Bridge

Position 6 as "The Bridge" is hardcoded in two completely separate places:

```java
// Player.java
if (eff == 6) return "The Bridge";

// MovePlayerCommand.java
if (movedPlayer.position() == 6) { ... jumps to 12 ... }
```

`Player` should not know what "The Bridge" is — that is game-rule knowledge. And the jump destination (12) is also a magic number. These belong together in one place, near the bridge rule.

---

## The Chain of Responsibility is hollow

The `MovementRule` functional interface has seven parameters:

```java
MoveResult apply(Player player, Player movedPlayer, int steps,
                 int firstDice, int secondDice, int boardSize, String playerName);
```

The rules are private methods on the record itself, referenced as `this::bridgeRule` etc. This means:

- Adding a new rule still requires editing `MovePlayerCommand` — the pattern gives no real extension point
- All rules receive all context regardless of what they need (`normalRule` ignores five of seven parameters)
- The fallback after the loop is dead code — `normalRule` always returns non-null, making the comment `// should not happen` a quiet lie

A real Chain of Responsibility would let you compose independent `Rule` objects externally. What's here is just a `List` of method references dressed up as a pattern.

---

## `play() throws Exception`

```java
public void play() throws Exception {
```

The actual checked exception is `IOException` from `BufferedReader.readLine()`. Declaring `throws Exception` is imprecise and forces every caller (including tests) to catch `Exception`. This propagates into every test method signature too.

---

## `noPlayersWon()` is an indirect way to track game termination

```java
if (noPlayersWon()) {
    output.print("See you!");
}
```

The intent is "print `See you!` only if the game ended via `quit`, not via a win." The implementation checks player positions to infer this, instead of tracking how the loop ended. A boolean flag set before the `break` would make the intent explicit and is harder to accidentally break.

---

## `PlayerAdded` carries a function — subtle fragility

```java
public record PlayerAdded(Player player, Function<Collection<Player>, String> messageFn)
```

The result carries a formatting lambda that depends on state (`playersMap`) it doesn't own. In `GooseGame.execute()`, the map must be updated *before* `messageFn` is applied, or the message is wrong. This implicit ordering constraint isn't obvious and isn't tested for. The simplest fix: pass the player list to `PlayerAdded` at creation time, not a function.

---

## Minor issues

- `commandParser` is a `static` field on `GooseGame`, which makes it impossible to inject a test double. Since `CommandParser` is stateless this is harmless in practice, but it is an inconsistency.
- `case PlayerBouncedBack ignored -> false; case PlayerMoved ignored -> false;` in `gameFinishedAfter` can be collapsed into `case PlayerBouncedBack ignored, PlayerMoved ignored -> false;` — a small signal of Java 21 fluency.
- The test `move Pippo 10, 0` uses a dice value of 0, which is impossible in the actual game. No validation exists in `CommandParser` to prevent this, so tests can use physically impossible inputs without any error.

---

## What to praise

- The sealed `Result` hierarchy is clean and the exhaustive `switch` expressions that consume it are well-structured.
- `Player` as an immutable record with movement returning new instances is correct.
- The test DSL with `givenTheseCommands(...).whenGameIsPlayed().thenOutputShouldBe(...)` reads well.
- The goose rule's chaining loop handles multiple consecutive goose cells correctly.

---

## Takeaway for the interview conversation

The main conversation to have is: *"Walk me through how you developed this test by test."* If the answer reveals an integration-test-first approach, the follow-up is: *"What would you do differently to make individual failures easier to diagnose?"* That conversation reveals far more about a candidate's TDD understanding than the code itself.
