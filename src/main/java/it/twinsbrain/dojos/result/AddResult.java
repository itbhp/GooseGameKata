package it.twinsbrain.dojos.result;



public sealed interface AddResult extends Result permits PlayerAlreadyPresent, PlayerAdded{}

