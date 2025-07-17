package it.twinsbrain.dojos.commands;

import static java.lang.String.format;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;

public record MovePlayerCommand(String playerName, int firstDice, int secondDice, int boardSize)
    implements Command {
  public MoveResult move(Player player) {
    var movedPlayer = player.move(steps());
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

  private int steps() {
    return firstDice + secondDice;
  }

  private String moveMessage(String startCell, String finishCell) {
    return format(
        "%s rolls %d, %d. %s moves from %s to %s",
        playerName, firstDice, secondDice, playerName, startCell, finishCell);
  }
}
