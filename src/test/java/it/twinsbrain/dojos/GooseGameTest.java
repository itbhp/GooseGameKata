package it.twinsbrain.dojos;

import static it.twinsbrain.dojos.GooseGameTest.GameTester.givenTheseCommands;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import org.junit.jupiter.api.Test;

class GooseGameTest {

  @Test
  void should_allow_to_quit_game() throws Exception {
    givenTheseCommands("quit")
            .whenGameIsPlayed()
            .thenOutputShouldBe("See you!");
  }

  @Test
  void should_allow_to_add_a_player() throws Exception {
    givenTheseCommands("add player Pippo", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        See you!""");
  }

  @Test
  void should_allow_to_add_more_than_one_player() throws Exception {
    givenTheseCommands("add player Pippo", "add player Pluto", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        players: Pippo, Pluto
                        See you!""");
  }

  @Test
  void should_not_allow_to_add_more_than_once_the_same_player() throws Exception {
    givenTheseCommands("add player Pippo", "add player Pippo", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        Pippo: already existing player
                        See you!""");
  }

  @Test
  void should_allow_to_move_a_player_in_the_board() throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo 4, 2", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        Pippo rolls 4, 2. Pippo moves from Start to 6
                        See you!""");
  }

  @Test
  void should_allow_to_move_multiple_players_in_the_board() throws Exception {
    givenTheseCommands(
            "add player Pippo",
            "add player Pluto",
            "move Pippo 4, 2",
            "move Pluto 2, 2",
            "move Pippo 2, 1",
            "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        players: Pippo, Pluto
                        Pippo rolls 4, 2. Pippo moves from Start to 6
                        Pluto rolls 2, 2. Pluto moves from Start to 4
                        Pippo rolls 2, 1. Pippo moves from 6 to 9
                        See you!""");
  }

  @Test
  void should_bounce_back_when_a_player_steps_beyond_cell_63_to_the_exceeding_amount()
      throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo 58, 2", "move Pippo 2, 3", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        Pippo rolls 58, 2. Pippo moves from Start to 60
                        Pippo rolls 2, 3. Pippo moves from 60 to 63. Pippo bounces! Pippo returns to 61
                        See you!""");
  }

  @Test
  void should_finish_when_a_player_wins() throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo 58, 2", "move Pippo 1, 2")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                        players: Pippo
                        Pippo rolls 58, 2. Pippo moves from Start to 60
                        Pippo rolls 1, 2. Pippo moves from 60 to 63. Pippo Wins!!""");
  }

  static class GameTester {
    private final CharSequence[] commandList;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();

    GameTester(CharSequence[] commandList) {
      this.commandList = commandList;
    }

    static GameTester givenTheseCommands(CharSequence... commandList) {
      return new GameTester(commandList);
    }

    GameTester whenGameIsPlayed() throws Exception {
      var commands = String.join("\n", commandList);
      var input = new ByteArrayInputStream(commands.getBytes());
      new GooseGame(input, output).play();
      return this;
    }

    void thenOutputShouldBe(String expectedOutput) {
      assertThat(output.toString(), equalTo(expectedOutput));
    }
  }
}
