package it.twinsbrain.dojos;

public record Player(String name, int position) {

  public Player move(int steps) {
    return new Player(name, position + steps);
  }
}
