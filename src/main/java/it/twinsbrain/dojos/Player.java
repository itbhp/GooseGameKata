package it.twinsbrain.dojos;

public record Player(String name, int position) {

  private static final int BOARD_SIZE = 63;

  public Player move(int steps) {
    return new Player(name, position + steps);
  }

  public boolean hasWon() {
    return position == BOARD_SIZE;
  }

  public Player bounceBack() {
    return new Player(name, BOARD_SIZE - exceedingSteps());
  }

  public boolean isBeyondTheFinish() {
    return exceedingSteps() > 0;
  }

  public String cell() {
    return position == 0 ? "Start" : String.valueOf(effectivePosition());
  }

  private int effectivePosition() {
    return Math.min(position, BOARD_SIZE);
  }

  private int exceedingSteps() {
    return position - BOARD_SIZE;
  }
}
