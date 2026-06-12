package it.twinsbrain.dojos;

import it.twinsbrain.dojos.commands.AddPlayerCommand;
import it.twinsbrain.dojos.commands.MovePlayerCommand;
import it.twinsbrain.dojos.result.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class Game {

  private final Board board;
  private final Map<String, Player> players = new HashMap<>();

  public Game(Board board) {
    this.board = board;
  }

  public AddResult execute(AddPlayerCommand command) {
    var result = command.createIfNotExists(players::containsKey);
    if (result instanceof PlayerAdded(Player player)) {
      players.put(command.playerName(), player);
    }
    return result;
  }

  public MoveResult execute(MovePlayerCommand movePlayerCommand) {
    var name = movePlayerCommand.playerName();
    var player = players.get(name);
    var result = movePlayerCommand.move(player);
    var prank = prankIfOccupied(result, name);
    if (prank != null) {
      players.put(prank.occupantName(), new Player(prank.occupantName(), prank.occupantNewPosition()));
    }
    switch (result) {
      case GameFinished gameFinished -> players.put(name, gameFinished.winner());
      case PlayerMoved playerMoved -> players.put(name, playerMoved.player());
      case PlayerBouncedBack playerBouncedBack -> players.put(name, playerBouncedBack.player());
    }
    return attachPrank(result, prank);
  }

  private Prank prankIfOccupied(MoveResult result, String moverName) {
    var destination = switch (result) {
      case PlayerMoved pm -> pm.player().position();
      case PlayerBouncedBack pbb -> pbb.player().position();
      case GameFinished ignored -> -1;
    };
    if (destination == -1) return null;
    var fromPosition = switch (result) {
      case PlayerMoved pm -> pm.context().fromPosition();
      case PlayerBouncedBack pbb -> pbb.context().fromPosition();
      case GameFinished ignored -> -1;
    };
    return players.entrySet().stream()
        .filter(e -> !e.getKey().equals(moverName))
        .filter(e -> e.getValue().position() == destination)
        .findFirst()
        .map(entry -> new Prank(entry.getKey(), entry.getValue().position(), fromPosition))
        .orElse(null);
  }

  private MoveResult attachPrank(MoveResult result, Prank prank) {
    if (prank == null) return result;
    return switch (result) {
      case PlayerMoved pm -> new PlayerMoved(pm.player(), pm.context(), prank);
      case PlayerBouncedBack pbb -> new PlayerBouncedBack(pbb.player(), pbb.context(), prank);
      case GameFinished gf -> gf;
    };
  }

  public boolean isOver() {
    return players.values().stream().anyMatch(p -> board.isWin(p.position()));
  }

  public boolean containsPlayer(String name) {
    return players.containsKey(name);
  }

  public Collection<Player> players() {
    return players.values();
  }
}
