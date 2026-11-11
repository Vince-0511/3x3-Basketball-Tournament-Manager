package com.tournament.exception;

public class TeamCannotBeDeletedException extends RuntimeException {

    public TeamCannotBeDeletedException(String message) {
        super(message);
    }
}