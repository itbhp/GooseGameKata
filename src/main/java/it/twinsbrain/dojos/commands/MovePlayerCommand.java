package it.twinsbrain.dojos.commands;

import static java.lang.String.format;

import it.twinsbrain.dojos.Board;
import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
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
    List<MovementRule> rules = List.of(this::bridgeRule, this::gooseRule, this::bounceRule, this::winRule, this::normalRule);
    for (var rule : rules) {
      var result = rule.apply(player, to, steps);
      if (result != null) return result;
    }
    return normalRule(player, to, steps);
  }

  private MoveResult bridgeRule(Player from, Player to, int steps) {
    if (!board.isBridge(to.position())) return null;
    var jumped = new Player(playerName, board.bridgeDestination());
    return new PlayerMoved(
        jumped,
        moveMessage(board.nameOf(from.position()), "The Bridge")
            + ". "
            + playerName
            + " jumps to "
            + board.bridgeDestination());
  }

  private MoveResult gooseRule(Player from, Player to, int steps) {
    if (!board.isGoose(to.position())) return null;
    var msg = new StringBuilder();
    msg.append(moveMessage(board.nameOf(from.position()), board.nameOf(to.position()))).append(", The Goose.");
    var current = to;
    while (board.isGoose(current.position())) {
      var next = current.move(steps);
      if (board.isBeyondFinish(next.position())) {
        var bounced = new Player(playerName, board.bouncePositionFor(next.position()));
        msg.append(" ")
            .append(playerName)
            .append(" moves again and goes to ")
            .append(board.nameOf(next.position()))
            .append(format(". %s bounces! %s returns to %d", playerName, playerName, bounced.position()));
        return new PlayerBouncedBack(bounced, msg.toString());
      }
      if (board.isWin(next.position())) {
        msg.append(" ").append(playerName).append(" moves again and goes to ").append(board.nameOf(next.position()))
            .append(". ").append(playerName).append(" Wins!!");
        return new GameFinished(next, msg.toString());
      }
      msg.append(" ").append(playerName).append(" moves again and goes to ").append(board.nameOf(next.position()));
      if (board.isGoose(next.position())) msg.append(", The Goose.");
      current = next;
    }
    return new PlayerMoved(current, msg.toString());
  }

  private MoveResult bounceRule(Player from, Player to, int steps) {
    if (!board.isBeyondFinish(to.position())) return null;
    var bounced = new Player(playerName, board.bouncePositionFor(to.position()));
    return new PlayerBouncedBack(
        bounced,
        moveMessage(board.nameOf(from.position()), board.nameOf(board.size()))
            + format(". %s bounces! %s returns to %d", playerName, playerName, bounced.position()));
  }

  private MoveResult winRule(Player from, Player to, int steps) {
    if (!board.isWin(to.position())) return null;
    return new GameFinished(
        to,
        moveMessage(board.nameOf(from.position()), board.nameOf(to.position()))
            + ". "
            + playerName
            + " Wins!!");
  }

  private MoveResult normalRule(Player from, Player to, int steps) {
    return new PlayerMoved(to, moveMessage(board.nameOf(from.position()), board.nameOf(to.position())));
  }

  private String moveMessage(String fromCell, String toCell) {
    return format("%s rolls %d, %d. %s moves from %s to %s", playerName, firstDice, secondDice, playerName, fromCell, toCell);
  }
}
