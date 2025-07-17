package it.twinsbrain.dojos.commands;

import static java.lang.String.format;

import it.twinsbrain.dojos.Player;
import it.twinsbrain.dojos.result.GameFinished;
import it.twinsbrain.dojos.result.MoveResult;
import it.twinsbrain.dojos.result.PlayerBouncedBack;
import it.twinsbrain.dojos.result.PlayerMoved;

public record MovePlayerCommand(String playerName, int firstDice, int secondDice)
    implements Command {
  public MoveResult move(Player player) {
    var movedPlayer = player.move(steps());
    if (movedPlayer.isBeyondTheFinish()) {
      var bounced = movedPlayer.bounceBack();
      var bouncedMessage =
          format(
              ". %s bounces! %s returns to %d", player.name(), player.name(), bounced.position());
      return new PlayerBouncedBack(
          bounced, moveMessage(player.cell(), movedPlayer.cell()) + bouncedMessage);
    }
    if (movedPlayer.hasWon()) {
      return new GameFinished(
          movedPlayer,
          moveMessage(player.cell(), movedPlayer.cell()) + ". " + player.name() + " Wins!!");
    }
    return new PlayerMoved(movedPlayer, moveMessage(player.cell(), movedPlayer.cell()));
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
