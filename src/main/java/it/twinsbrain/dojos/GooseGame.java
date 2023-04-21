package it.twinsbrain.dojos;

import java.io.InputStream;
import java.io.OutputStream;

public class GooseGame {
    private final InputStream input;
    private final OutputStream output;

    public GooseGame(InputStream input, OutputStream output) {
        this.input = input;
        this.output = output;
    }

    public void play() throws Exception {
        output.write("See you!".getBytes());
    }
}
