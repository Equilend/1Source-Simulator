package com.equilend.simulator;

public class TradeHandler implements EventHandler{
    private Event event;
    private Configurator rules;
    
    public Token getToken(){
        return new Token();
    }
    public TradeHandler(Event e, Configurator rules){
        this.event = e;
        this.rules = rules;
    }

    public void run(){
        System.out.println(Thread.currentThread().getName() + ": get token");
        System.out.println(Thread.currentThread().getName() + ": propose contract from trade");
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String agreementId = arr[arr.length-1];
    }    
}
