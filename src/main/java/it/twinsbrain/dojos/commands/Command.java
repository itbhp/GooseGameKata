package it.twinsbrain.dojos.commands;

public sealed interface Command permits AddPlayerCommand, MovePlayerCommand {}
