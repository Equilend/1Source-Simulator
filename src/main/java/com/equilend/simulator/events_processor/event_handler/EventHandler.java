package com.equilend.simulator.events_processor.event_handler;

import com.equilend.simulator.token.OneSourceToken;

public interface EventHandler extends Runnable {

    public OneSourceToken getToken();

}