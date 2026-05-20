# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Commands

**Build:**
```bash
./mvnw clean package
```

**Run all tests:**
```bash
./mvnw test
```

**Run a single test class:**
```bash
./mvnw -Dtest=MineGooseGameTest test
```

**Run a single test method:**
```bash
./mvnw -Dtest=MineGooseGameTest#shouldAddPlayerWhenNameIsNew test
```

**Format code (Spotless with Google Java Format — enforced):**
```bash
./mvnw spotless:apply
```

**Check formatting without applying:**
```bash
./mvnw spotless:check
```

> Java 24 with `--enable-preview` is required. Spotless/Google Java Format is configured in `pom.xml` and should be applied before committing.

## Architecture

The kata implements the Goose Game board game (63 squares) with a command-driven game loop.

### Core flow

`Main` → `GooseGame` reads lines from stdin, delegates to `CommandParser` to produce a `Command`, executes the command against the player state map, and writes the resulting message(s) to stdout. Typing `quit` exits the loop.

### Key design patterns

**Sealed interfaces for exhaustive typing** — both `Command` and `Result` hierarchies use `sealed interface` with Java records as leaves. Pattern matching (`switch` expressions) replaces `instanceof` chains everywhere.

**Chain of Responsibility for movement rules** — `MovePlayerCommand` holds an ordered list of `MovementRule` lambdas (Bridge → Goose → Bounce → Win → Normal). Each rule returns a `MoveResult` or `null`; the first non-null result wins. To add a new rule, insert a lambda in the right position.

**Immutable value objects** — `Player(String name, int position)` is a record. Movement produces new `Player` instances; `GooseGame` swaps them in the map.

### Package layout

```
it.twinsbrain.dojos/
├── GooseGame.java          # game loop, player state map, board size (63)
├── Player.java             # record; position logic (0=Start, 6=The Bridge)
├── commands/
│   ├── Command.java        # sealed interface
│   ├── CommandParser.java  # parses "add player X" / "move X d1, d2"
│   ├── AddPlayerCommand.java
│   └── MovePlayerCommand.java  # movement rules, goose positions {5,9,14,18,23,27}
└── result/                 # sealed Result hierarchy (AddResult, MoveResult subtypes)
```

### Test structure

`MineGooseGameTest` uses a fluent DSL: `givenTheseCommands(...).whenGameIsPlayed().thenOutputShouldBe(...)`. Tests inject commands via `ByteArrayInputStream` and capture output via `ByteArrayOutputStream`, so the full game loop runs in isolation.
