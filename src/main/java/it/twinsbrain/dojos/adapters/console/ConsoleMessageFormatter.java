package it.twinsbrain.dojos.adapters.console;

import it.twinsbrain.dojos.*;
import it.twinsbrain.dojos.result.*;

import java.util.Collection;

import static java.util.stream.Collectors.joining;

public class ConsoleMessageFormatter {

  private final Board board;

  public ConsoleMessageFormatter(Board board) {
    this.board = board;
  }

  public String format(AddResult result, Collection<Player> allPlayers) {
    return switch (result) {
      case PlayerAdded ignored -> "players: " + allPlayers.stream().map(Player::name).collect(joining(", "));
      case PlayerAlreadyPresent p -> p.playerName() + ": already existing player";
    };
  }

  public String format(MoveResult result) {
    var base = switch (result) {
      case PlayerMoved pm -> formatPlayerMoved(pm);
      case PlayerBouncedBack pbb -> bouncePrefix(pbb.context())
          + ". " + pbb.player().name() + " bounces! " + pbb.player().name() + " returns to " + pbb.player().position();
      case GameFinished gf -> bouncePrefix(gf.context())
          + ". " + gf.winner().name() + " Wins!!";
    };
    var prank = switch (result) {
      case PlayerMoved pm -> pm.prank();
      case PlayerBouncedBack pbb -> pbb.prank();
      case GameFinished ignored -> null;
    };
    if (prank != null) {
      return base + ". On " + board.nameOf(prank.occupantPreviousPosition())
          + " there is " + prank.occupantName()
          + ", who returns to " + board.nameOf(prank.occupantNewPosition());
    }
    return base;
  }

  private String bouncePrefix(MoveContext pbb) {
    return switch (pbb) {
      case SimpleMove sm -> {
        var displayTo = board.isBeyondFinish(sm.landedPosition()) ? board.size() : sm.landedPosition();
        yield header(sm) + board.nameOf(sm.fromPosition()) + " to " + displayTo;
      }
      case GooseChain gc -> formatGooseChain(gc);
    };
  }

  private String formatPlayerMoved(PlayerMoved pm) {
    return switch (pm.context()) {
      case SimpleMove sm when board.isBridge(sm.landedPosition()) ->
          header(sm) + board.nameOf(sm.fromPosition()) + " to The Bridge"
              + ". " + sm.playerName() + " jumps to " + board.bridgeDestination();
      case SimpleMove sm -> {
        var displayTo = board.isBeyondFinish(sm.landedPosition()) ? board.size() : sm.landedPosition();
        yield header(sm) + board.nameOf(sm.fromPosition()) + " to " + displayTo;
      }
      case GooseChain gc -> formatGooseChain(gc);
    };
  }

  private String formatGooseChain(GooseChain gc) {
    var sb = new StringBuilder(header(gc)).append(board.nameOf(gc.fromPosition()));
    var positions = gc.visitedPositions();
    for (int i = 0; i < positions.size(); i++) {
      var pos = positions.get(i);
      var isLast = (i == positions.size() - 1);
      var displayPos = (!isLast || !board.isBeyondFinish(pos)) ? pos : board.size();
      if (i == 0) {
        sb.append(" to ").append(displayPos);
      } else {
        sb.append(". ").append(gc.playerName()).append(" moves again and goes to ").append(displayPos);
      }
      if (!isLast && board.isGoose(pos)) {
        sb.append(", The Goose");
      }
    }
    return sb.toString();
  }

  private String header(MoveContext ctx) {
    return ctx.playerName() + " rolls " + ctx.firstDice() + ", " + ctx.secondDice()
        + ". " + ctx.playerName() + " moves from ";
  }
}
