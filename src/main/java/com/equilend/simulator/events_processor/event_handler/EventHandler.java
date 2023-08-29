package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.token.BearerToken;

public interface EventHandler extends Runnable {

    public BearerToken getToken();

}