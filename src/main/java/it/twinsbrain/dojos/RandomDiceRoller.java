package it.twinsbrain.dojos;

import java.util.Random;

public class RandomDiceRoller implements DiceRoller {
  private final Random random = new Random();

  @Override
  public DiceRoll roll() {
    return new DiceRoll(random.nextInt(6) + 1, random.nextInt(6) + 1);
  }
}
