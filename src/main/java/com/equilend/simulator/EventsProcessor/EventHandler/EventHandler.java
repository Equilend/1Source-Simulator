package com.equilend.simulator.EventsProcessor.EventHandler;

import com.equilend.simulator.Token.BearerToken;

public interface EventHandler extends Runnable {

    public BearerToken getToken();

}