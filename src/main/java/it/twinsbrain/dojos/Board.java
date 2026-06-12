package it.twinsbrain.dojos;

import java.util.Set;

public record Board(int size) {

  private static final int BRIDGE_POSITION = 6;
  private static final int BRIDGE_DESTINATION = 12;
  private static final Set<Integer> GOOSE_POSITIONS = Set.of(5, 9, 14, 18, 23, 27);

  public String nameOf(int position) {
    if (position == 0) return "Start";
    if (position == BRIDGE_POSITION) return "The Bridge";
    return String.valueOf(position);
  }

  public boolean isBridge(int position) {
    return position == BRIDGE_POSITION;
  }

  public int bridgeDestination() {
    return BRIDGE_DESTINATION;
  }

  public boolean isGoose(int position) {
    return GOOSE_POSITIONS.contains(position);
  }

  public boolean isWin(int position) {
    return position == size;
  }

  public boolean isBeyondFinish(int position) {
    return position > size;
  }

  public int bouncePositionFor(int position) {
    return size - (position - size);
  }
}
