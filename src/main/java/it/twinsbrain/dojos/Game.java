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
    switch (result) {
      case GameFinished gameFinished -> players.put(name, gameFinished.winner());
      case PlayerMoved playerMoved -> players.put(name, playerMoved.player());
      case PlayerBouncedBack playerBouncedBack -> players.put(name, playerBouncedBack.player());
    }
    return result;
  }

  public boolean isOver() {
    return players.values().stream().anyMatch(p -> board.isWin(p.position()));
  }

  public Collection<Player> players() {
    return players.values();
  }
}
