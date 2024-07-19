package com.equilend.simulator.api;

public class FedAPIException extends Exception {

    public FedAPIException(String message) {
        super(message);
    }

    public FedAPIException(String message, Throwable cause) {
        super(message, cause);
    }

}