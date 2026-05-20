package it.twinsbrain.dojos;

import it.twinsbrain.dojos.adapters.console.ConsoleAdapter;

@SuppressWarnings("unused")
public class Main {
  public static void main(String[] args) throws Exception {
    new ConsoleAdapter(System.in, System.out).play();
  }
}
