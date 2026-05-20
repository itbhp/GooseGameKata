package it.twinsbrain.dojos;

public sealed interface MoveContext permits SimpleMove, GooseChain {
  String playerName();
  int firstDice();
  int secondDice();
  int fromPosition();
}
