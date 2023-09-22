package com.equilend.simulator.scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.rules.ContractGenerativeRule;
import com.equilend.simulator.rules.ContractRule;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.transacting_party.Party;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private String botPartyId;
    private Map<String, Party> parties;
    private Map<String, Instrument> instruments;

    public Scheduler(Configurator configurator){
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.parties = configurator.getParties();
        this.instruments = configurator.getInstruments();
    }

    public void run(){
        //get generative contract rules/instructions from configurator
        List<ContractRule> rules = configurator.getContractRules().getSchedulerRules();
        
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

        //for each instruction, create a thread that handles this task.
        for (ContractRule rule : rules){
            ContractGenerativeRule instruction = (ContractGenerativeRule) rule;
            for (String counterpartyId : instruction.getCounterparties()){
                for (String security : instruction.getSecurities()){
                    Party party = parties.get(botPartyId);
                    Party counterparty = parties.get(counterpartyId);
                    Instrument instrument = instruments.get(security);
                    String quantity = instruction.getQuantity();
                    ScheduledEventHandler task = new ScheduledEventHandler(party, counterparty, instrument, quantity);
                    
                    Long delayMillis = Math.round(1000*instruction.getDelaySecs());
                    Long periodMillis = Math.round(1000*instruction.getPeriodSecs());
                    Long durationMillis = Math.round(1000*instruction.getTotalDurationSecs()); 
                    ScheduledFuture<?> taskFuture = exec.scheduleAtFixedRate(task, delayMillis, periodMillis, TimeUnit.MILLISECONDS);
                    exec.schedule(new Runnable() {
                        public void run() {
                            taskFuture.cancel(true);
                        }
                    }, durationMillis, TimeUnit.MILLISECONDS);
                }
            }

        }
    }
}