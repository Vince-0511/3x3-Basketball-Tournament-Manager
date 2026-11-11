
package com.tournament.exception;

public class TeamLimitReachedException extends RuntimeException {

    public TeamLimitReachedException(String message) {
        super(message);
    }
}
