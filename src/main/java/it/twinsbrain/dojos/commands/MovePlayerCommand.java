package it.twinsbrain.dojos.commands;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.GooseChain;
import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.SimpleMove;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
import java.util.ArrayList;
import java.util.List;

public record MovePlayerCommand(String playerName, int firstDice, int secondDice, Board board)
    implements Command {

  @FunctionalInterface
  private interface MovementRule {
    MoveResult apply(Player from, Player to, int steps);
  }

  public MoveResult move(Player player) {
    var steps = firstDice + secondDice;
    var to = player.move(steps);
    List<MovementRule> rules =
        List.of(this::bridgeRule, this::gooseRule, this::bounceRule, this::winRule, this::normalRule);
    for (var rule : rules) {
      var result = rule.apply(player, to, steps);
      if (result != null) return result;
    }
    return normalRule(player, to, steps);
  }

  private MoveResult bridgeRule(Player from, Player to, int steps) {
    if (!board.isBridge(to.position())) return null;
    var jumped = new Player(playerName, board.bridgeDestination());
    return new PlayerMoved(jumped, new SimpleMove(playerName, firstDice, secondDice, from.position(), to.position()));
  }

  private MoveResult gooseRule(Player from, Player to, int steps) {
    if (!board.isGoose(to.position())) return null;
    var visited = new ArrayList<Integer>();
    visited.add(to.position());
    var current = to;
    while (board.isGoose(current.position())) {
      var next = current.move(steps);
      visited.add(next.position());
      if (board.isBeyondFinish(next.position())) {
        var bounced = new Player(playerName, board.bouncePositionFor(next.position()));
        return new PlayerBouncedBack(bounced, new GooseChain(playerName, firstDice, secondDice, from.position(), List.copyOf(visited)));
      }
      if (board.isWin(next.position())) {
        return new GameFinished(next, new GooseChain(playerName, firstDice, secondDice, from.position(), List.copyOf(visited)));
      }
      current = next;
    }
    return new PlayerMoved(current, new GooseChain(playerName, firstDice, secondDice, from.position(), List.copyOf(visited)));
  }

  private MoveResult bounceRule(Player from, Player to, int steps) {
    if (!board.isBeyondFinish(to.position())) return null;
    var bounced = new Player(playerName, board.bouncePositionFor(to.position()));
    return new PlayerBouncedBack(bounced, new SimpleMove(playerName, firstDice, secondDice, from.position(), to.position()));
  }

  private MoveResult winRule(Player from, Player to, int steps) {
    if (!board.isWin(to.position())) return null;
    return new GameFinished(to, new SimpleMove(playerName, firstDice, secondDice, from.position(), to.position()));
  }

  private MoveResult normalRule(Player from, Player to, int steps) {
    return new PlayerMoved(to, new SimpleMove(playerName, firstDice, secondDice, from.position(), to.position()));
  }
}
