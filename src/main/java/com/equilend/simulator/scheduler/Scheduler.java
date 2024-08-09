package com.equilend.simulator.scheduler;

import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractGenerativeRule;
import com.equilend.simulator.configurator.rules.contract_rules.ContractRule;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.instrument.Instrument;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

public class Scheduler implements Runnable {

    private Configurator configurator;
    private String botPartyId;
    private Map<String, Party> parties;
    private Map<String, Instrument> instruments;

    public Scheduler(Configurator configurator) {
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.parties = configurator.getParties();
        this.instruments = configurator.getInstruments();
    }

    private static class GeneratedEventThread implements ThreadFactory {

        private static int count = 0;

        public Thread newThread(Runnable r) {
            return new Thread(r, "Generated-Event-Thread-" + String.valueOf(count++));
        }
    }

    public void run() {
        // Get generative contract rules/instructions from configurator
        List<ContractRule> rules = configurator.getContractRules().getSchedulerRules();
        if (rules == null || rules.size() == 0) {
            return;
        }

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(8, new GeneratedEventThread());

        // For each instruction, create a thread that handles this task.
        for (ContractRule rule : rules) {
            ContractGenerativeRule instruction = (ContractGenerativeRule) rule;
            for (String counterpartyId : instruction.getCounterparties()) {
                for (String security : instruction.getSecurities()) {
                    PartyRole partyRole = instruction.getPartyRole();
                    Party party = parties.get(botPartyId);
                    Party counterparty = parties.get(counterpartyId);
                    int bang = security.indexOf("!");
                    Instrument instrument;
                    String idType = "";
                    if (bang == -1) {
                        instrument = instruments.get(security);
                    } else {
                        idType = security.substring(0, bang).trim();
                        String idValue = security.substring(bang + 1).trim();
                        instrument = new Instrument().figi(idValue).description("Security LLC");
                    }
                    String quantity = instruction.getQuantity();
                    ScheduledEventHandler task = new ScheduledEventHandler(partyRole, party, counterparty, instrument,
                        quantity, idType);

                    Long delayMillis = Math.round(1000 * instruction.getDelaySecs());
                    Long periodMillis = Math.round(1000 * instruction.getPeriodSecs());
                    Long durationMillis = Math.round(1000 * instruction.getTotalDurationSecs());
                    ScheduledFuture<?> taskFuture = exec.scheduleAtFixedRate(task, delayMillis, periodMillis,
                        TimeUnit.MILLISECONDS);
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