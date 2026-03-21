package it.twinsbrain.dojos.commands;

import static java.lang.String.format;

import it.twinsbrain.dojos.Player;

import java.util.Set;

import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;

public record MovePlayerCommand(String playerName, int firstDice, int secondDice, int boardSize)
    implements Command {
  // The Goose: certain cells cause the player to move again by the same dice sum
  private static final Set<Integer> GOOSE = Set.of(5, 9, 14, 18, 23, 27);

  public MoveResult move(Player player) {
    var steps = firstDice + secondDice;
    var movedPlayer = player.move(steps);
    // The Bridge: landing on cell 6 jumps to 12
    if (movedPlayer.position() == 6) {
      var jumped = movedPlayer.move(6);
      return new PlayerMoved(jumped, moveMessage(player.cellGiven(boardSize), "The Bridge") +
          ". " + playerName + " jumps to 12");
    }

    if (GOOSE.contains(movedPlayer.position())) {
      var msg = new StringBuilder();
      msg.append(moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize))).append(", The Goose.");
      var current = movedPlayer;
      while (GOOSE.contains(current.position())) {
        var next = current.move(steps);
        // bounced during goose chain
        if (next.isBeyondTheFinish(boardSize)) {
          var bounced = next.bounceBack(boardSize);
          var bouncedMessage = format(". %s bounces! %s returns to %d", playerName, playerName, bounced.position());
          msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize)).append(bouncedMessage);
          return new PlayerBouncedBack(bounced, msg.toString());
        }
        // win during goose chain
        if (next.hasWonGiven(boardSize)) {
          msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize)).append(". ").append(playerName).append(" Wins!!");
          return new GameFinished(next, msg.toString());
        }
        // normal goose continuation
        msg.append(" ").append(playerName).append(" moves again and goes to ").append(next.cellGiven(boardSize));
        if (GOOSE.contains(next.position())) msg.append(", The Goose.");
        current = next;
      }
      return new PlayerMoved(current, msg.toString());
    }
    if (movedPlayer.isBeyondTheFinish(boardSize)) {
      var bounced = movedPlayer.bounceBack(boardSize);
      var bouncedMessage =
          format(
              ". %s bounces! %s returns to %d", player.name(), player.name(), bounced.position());
      return new PlayerBouncedBack(
          bounced, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize)) + bouncedMessage);
    }
    if (movedPlayer.hasWonGiven(boardSize)) {
      return new GameFinished(
          movedPlayer,
          moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize)) + ". " + player.name() + " Wins!!");
    }
    return new PlayerMoved(movedPlayer, moveMessage(player.cellGiven(boardSize), movedPlayer.cellGiven(boardSize)));
  }

  private String moveMessage(String startCell, String finishCell) {
    return format(
        "%s rolls %d, %d. %s moves from %s to %s",
        playerName, firstDice, secondDice, playerName, startCell, finishCell);
  }
}
