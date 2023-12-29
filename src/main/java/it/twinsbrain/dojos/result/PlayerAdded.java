package it.twinsbrain.dojos.result;

import it.twinsbrain.dojos.Player;
import java.util.Collection;
import java.util.function.Function;

public record PlayerAdded(Player player, Function<Collection<Player>, String> messageFn)
    implements AddResult {}
