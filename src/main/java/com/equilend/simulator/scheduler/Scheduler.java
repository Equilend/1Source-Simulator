package com.equilend.simulator.scheduler;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractGenerativeRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractRule;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.transacting_party.Party;
import com.equilend.simulator.trade.transacting_party.PartyRole;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private String botPartyId;
    private Map<String, Party> parties;
    private Map<String, Instrument> instruments;

    private static final Logger logger = LogManager.getLogger();

    public Scheduler(Configurator configurator){
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.parties = configurator.getParties();
        this.instruments = configurator.getInstruments();
    }

    public void run(){
        // Get generative contract rules/instructions from configurator
        List<ContractRule> rules = configurator.getContractRules().getSchedulerRules();
        if (rules == null || rules.size() == 0) return;
        
        ScheduledExecutorService exec = Executors.newScheduledThreadPool(8);

        // For each instruction, create a thread that handles this task.
        for (ContractRule rule : rules){
            ContractGenerativeRule instruction = (ContractGenerativeRule) rule;
            for (String counterpartyId : instruction.getCounterparties()){
                for (String security : instruction.getSecurities()){
                    PartyRole partyRole = instruction.getPartyRole();
                    Party party = parties.get(botPartyId);
                    Party counterparty = parties.get(counterpartyId);
                    Instrument instrument = instruments.get(security);
                    String quantity = instruction.getQuantity();
                    ScheduledEventHandler task = new ScheduledEventHandler(partyRole, party, counterparty, instrument, quantity);
                    
                    Long delayMillis = Math.round(1000*instruction.getDelaySecs());
                    Long periodMillis = Math.round(1000*instruction.getPeriodSecs());
                    Long durationMillis = Math.round(1000*instruction.getTotalDurationSecs()); 
                    logger.trace("Delay {}, Period {}, Duration {}", delayMillis, periodMillis, durationMillis);
                    ScheduledFuture<?> taskFuture = exec.scheduleAtFixedRate(task, delayMillis, periodMillis, TimeUnit.MILLISECONDS);
                    exec.schedule(new Runnable() {
                        public void run() {
                            taskFuture.cancel(false);
                        }
                    }, durationMillis, TimeUnit.MILLISECONDS);
                }
            }
        }
    }
}