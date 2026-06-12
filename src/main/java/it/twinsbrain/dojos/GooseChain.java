package it.twinsbrain.dojos;

import java.util.List;

public record GooseChain(
    String playerName, int firstDice, int secondDice, int fromPosition, List<Integer> visitedPositions)
    implements MoveContext {}
