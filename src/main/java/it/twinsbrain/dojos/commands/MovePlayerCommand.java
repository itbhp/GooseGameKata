package it.twinsbrain.dojos.commands;

import static java.lang.String.format;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;
import java.util.List;
import java.util.Set;

public record MovePlayerCommand(String playerName, int firstDice, int secondDice, int boardSize)
    implements Command {

  private static final Set<Integer> GOOSE = Set.of(5, 9, 14, 18, 23, 27);

  @FunctionalInterface
  private interface MovementRule {
    MoveResult apply(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName);
  }

  public MoveResult move(Player player) {
    var steps = firstDice + secondDice;
    var movedPlayer = player.move(steps);
    List<MovementRule> rules = List.of(this::bridgeRule, this::gooseRule, this::bounceRule, this::winRule, this::normalRule);
    for (var rule : rules) {
      var result = rule.apply(player, movedPlayer, steps, firstDice, secondDice, boardSize, playerName);
      if (result != null) return result;
    }
    // fallback (should not happen)
    return new PlayerMoved(movedPlayer, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize), playerName, firstDice, secondDice));
  }

  private MoveResult bridgeRule(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName) {
    if (movedPlayer.position() == 6) {
      var jumped = movedPlayer.move(6);
      return new PlayerMoved(jumped, moveMessage(player.cellGiven(boardSize), "The Bridge", playerName, firstDice, secondDice) + ". " + playerName + " jumps to 12");
    }
    return null;
  }

  private MoveResult gooseRule(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName) {
    if (!GOOSE.contains(movedPlayer.position())) return null;
    var msg = new StringBuilder();
    msg.append(moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize), playerName, firstDice, secondDice)).append(", The Goose.");
    var current = movedPlayer;
    while (GOOSE.contains(current.position())) {
      var next = current.move(steps);
      if (next.isBeyondTheFinish(boardSize)) {
        var bounced = next.bounceBack(boardSize);
        var bouncedMessage = format(". %s bounces! %s returns to %d", playerName, playerName, bounced.position());
        msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize)).append(bouncedMessage);
        return new PlayerBouncedBack(bounced, msg.toString());
      }
      if (next.hasWonGiven(boardSize)) {
        msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize)).append(". ").append(playerName).append(" Wins!!");
        return new GameFinished(next, msg.toString());
      }
      msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize));
      if (GOOSE.contains(next.position())) msg.append(", The Goose.");
      current = next;
    }
    return new PlayerMoved(current, msg.toString());
  }

  private MoveResult bounceRule(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName) {
    if (!movedPlayer.isBeyondTheFinish(boardSize)) return null;
    var bounced = movedPlayer.bounceBack(boardSize);
    var bouncedMessage = format(". %s bounces! %s returns to %d", playerName, playerName, bounced.position());
    return new PlayerBouncedBack(bounced, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize), playerName, firstDice, secondDice) + bouncedMessage);
  }

  private MoveResult winRule(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName) {
    if (!movedPlayer.hasWonGiven(boardSize)) return null;
    return new GameFinished(movedPlayer, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize), playerName, firstDice, secondDice) + ". " + playerName + " Wins!!");
  }

  private MoveResult normalRule(Player player, Player movedPlayer, int steps, int firstDice, int secondDice, int boardSize, String playerName) {
    return new PlayerMoved(movedPlayer, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize), playerName, firstDice, secondDice));
  }

  private String moveMessage(String startCell, String finishCell, String playerName, int firstDice, int secondDice) {
    return format("%s rolls %d, %d. %s moves from %s to %s", playerName, firstDice, secondDice, playerName, startCell, finishCell);
  }
}
