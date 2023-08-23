package com.equilend.simulator.EventHandler;

import com.equilend.simulator.Token.BearerToken;

public interface EventHandler extends Runnable{
    public BearerToken getToken();
}
