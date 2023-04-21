package it.twinsbrain.dojos;

import java.io.*;

public class GooseGame {
    private final BufferedReader input;
    private final PrintWriter output;

    public GooseGame(InputStream input, OutputStream output) {
        this.input = new BufferedReader(new InputStreamReader(input));
        this.output = new PrintWriter(output);
    }

    public void play() throws Exception {
        String line;
        while (!(line = input.readLine()).equals("quit")) {
            // TODO later
            var commandParts = line.split(" ");
            output.println("players: " + commandParts[2]);
        }
        output.print("See you!");
        output.flush();
    }
}
