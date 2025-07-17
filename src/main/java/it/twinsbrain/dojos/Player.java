package it.twinsbrain.dojos;

public record Player(String name, int position) {

    public Player move(int steps) {
    return new Player(name, position + steps);
  }

  public boolean hasWonGiven(int boardSize) {
    return position == boardSize;
  }

  public Player bounceBack(int boardSize) {
    return new Player(name, boardSize - exceedingSteps(boardSize));
  }

  public boolean isBeyondTheFinish(int boardSize) {
    return exceedingSteps(boardSize) > 0;
  }

  public String cellGiven(int boardSize) {
    return position == 0 ? "Start" : String.valueOf(effectivePosition(boardSize));
  }

  private int effectivePosition(int boardSize) {
    return Math.min(position, boardSize);
  }

  private int exceedingSteps(int boardSize) {
    return position - boardSize;
  }
}
