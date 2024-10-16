package com.equilend.simulator.generator;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.loan_rules.LoanGenerativeRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanRule;

public class Generator implements Runnable {

    private Config config;
    private String botPartyId;

    public Generator() {
        this.config = Config.getInstance();
        this.botPartyId = config.getBotPartyId();
    }

    private static class GeneratedEventThread implements ThreadFactory {

        private static int count = 0;

        public Thread newThread(Runnable r) {
            return new Thread(r, "Generated-Event-Thread-" + String.valueOf(count++));
        }
    }

    public void run() {
        // Get generative loan rules/instructions from configurator
        List<LoanRule> rules = config.getLoanRules().getLoanProposeRules();
        if (rules == null || rules.size() == 0) {
            return;
        }

        ScheduledExecutorService exec = Executors.newScheduledThreadPool(8, new GeneratedEventThread());

        // For each instruction, create a thread that handles this task.
        for (LoanRule rule : rules) {
            LoanGenerativeRule loanGenerativeRule = (LoanGenerativeRule) rule;
            for (String counterpartyId : loanGenerativeRule.getCounterparties()) {
                for (String security : loanGenerativeRule.getSecurities()) {
                    String partyRole = loanGenerativeRule.getPartyRole();
                    Integer quantity = loanGenerativeRule.getQuantity();
                    Double rate = loanGenerativeRule.getRate();
                    Double price = loanGenerativeRule.getPrice();
                    String termType = loanGenerativeRule.getTermType();
                    LoanGenerator task = new LoanGenerator(partyRole, botPartyId, counterpartyId,
                        security,
                        quantity, rate, price, termType);

                    Long delayMillis = Math.round(1000 * loanGenerativeRule.getDelaySecs());
                    Long periodMillis = Math.round(1000 * loanGenerativeRule.getPeriodSecs());
                    Long durationMillis = Math.round(1000 * loanGenerativeRule.getTotalDurationSecs());
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