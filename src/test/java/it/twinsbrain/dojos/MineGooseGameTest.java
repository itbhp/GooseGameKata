package it.twinsbrain.dojos;

import static it.twinsbrain.dojos.MineGooseGameTest.GameTester.givenTheseCommands;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import org.junit.jupiter.api.Test;

class MineGooseGameTest {

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
                Pippo rolls 4, 2. Pippo moves from Start to The Bridge. Pippo jumps to 12
                See you!""");
  }

  @Test
  void should_jump_when_landing_on_the_goose_single_jump() throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo 1, 2", "move Pippo 1, 1", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                players: Pippo
                Pippo rolls 1, 2. Pippo moves from Start to 3
                Pippo rolls 1, 1. Pippo moves from 3 to 5, The Goose. Pippo moves again and goes to 7
                See you!""");
  }

  @Test
  void should_chain_multiple_goose_jumps() throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo 10, 0", "move Pippo 2, 2", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                players: Pippo
                Pippo rolls 10, 0. Pippo moves from Start to 10
                Pippo rolls 2, 2. Pippo moves from 10 to 14, The Goose. Pippo moves again and goes to 18, The Goose. Pippo moves again and goes to 22
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
                Pippo rolls 4, 2. Pippo moves from Start to The Bridge. Pippo jumps to 12
                Pluto rolls 2, 2. Pluto moves from Start to 4
                Pippo rolls 2, 1. Pippo moves from 12 to 15
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

  @Test
  void should_print_error_when_moving_non_existent_player() throws Exception {
    givenTheseCommands("move Pippo 1, 2", "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                Pippo: player not found
                See you!""");
  }

  @Test
  void should_prank_other_player_when_landing_on_occupied_space() throws Exception {
    givenTheseCommands(
        "add player Pippo",
        "add player Pluto",
        "move Pippo 10, 5",
        "move Pluto 10, 7",
        "move Pippo 1, 1",
        "quit")
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                players: Pippo
                players: Pippo, Pluto
                Pippo rolls 10, 5. Pippo moves from Start to 15
                Pluto rolls 10, 7. Pluto moves from Start to 17
                Pippo rolls 1, 1. Pippo moves from 15 to 17. On 17 there is Pluto, who returns to 15
                See you!""");
  }

  @Test
  void should_auto_roll_dice_when_dice_not_specified() throws Exception {
    givenTheseCommands("add player Pippo", "move Pippo", "quit")
        .withDice(1, 2)
        .whenGameIsPlayed()
        .thenOutputShouldBe(
            """
                players: Pippo
                Pippo rolls 1, 2. Pippo moves from Start to 3
                See you!""");
  }

  static class GameTester {
    private final CharSequence[] commandList;
    private final ByteArrayOutputStream output = new ByteArrayOutputStream();
    private DiceRoller diceRoller = new RandomDiceRoller();

    GameTester(CharSequence[] commandList) {
      this.commandList = commandList;
    }

    static GameTester givenTheseCommands(CharSequence... commandList) {
      return new GameTester(commandList);
    }

    @SuppressWarnings("SameParameterValue")
    GameTester withDice(int first, int second) {
      this.diceRoller = () -> new DiceRoll(first, second);
      return this;
    }

    GameTester whenGameIsPlayed() throws Exception {
      var commands = String.join("\n", commandList);
      var input = new ByteArrayInputStream(commands.getBytes());
      new GooseGame(input, output, diceRoller).play();
      return this;
    }

    void thenOutputShouldBe(String expectedOutput) {
      assertThat(output.toString(), equalTo(expectedOutput));
    }
  }
}
