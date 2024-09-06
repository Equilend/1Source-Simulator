package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.model.event.EventType.RERATE_PENDING;
import static com.equilend.simulator.model.event.EventType.RERATE_PROPOSED;
import static com.equilend.simulator.rules_processor.RerateRuleProcessor.process;
import static com.equilend.simulator.service.LoanService.getLoanById;
import static com.equilend.simulator.service.LoanService.getTransactingPartyById;
import static com.equilend.simulator.service.RerateService.getRerateById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.ReratePendingCancelRule;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.rerate.Rerate;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(RerateHandler.class.getName());
    private final Event event;
    private final Config config;
    private final String botPartyId;
    private final Long startTime;

    public RerateHandler(Event e, Config config, Long startTime) {
        this.event = e;
        this.config = config;
        this.botPartyId = config.getBotPartyId();
        this.startTime = startTime;
    }


    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        try {
            if (event.getEventType().equals(RERATE_PROPOSED)) {
                String rerateId = arr[arr.length - 1];
                Rerate rerate = getRerateById(rerateId);
                if (rerate == null) {
                    return;
                }

                String loanId = rerate.getLoanId();
                Loan loan = getLoanById(loanId);

                //For now, default to lender party being initiator and borrower party being recipient
                Optional<TransactingParty> transactingParty = getTransactingPartyById(loan, botPartyId);
                if (transactingParty.isPresent() && transactingParty.get().getPartyRole() == PartyRole.LENDER) {
                    RerateCancelRule rule = config.getRerateRules().getCancelRule(rerate, loan, botPartyId);
                    if (rule == null || !rule.shouldCancel()) {
                        return; //default to ignore/ not cancelling
                    }
                    process(startTime, rule, loan, rerate);
                } else {
                    RerateApproveRule rule = config.getRerateRules().getApproveRule(rerate, loan, botPartyId);
                    if (rule == null) {
                        //no applicable rule, default to approve w/o delay
                        process(startTime, new RerateApproveRule("A", 0.0), loan, rerate);
                    } else {
                        process(startTime, rule, loan, rerate);
                    }
                }
            } else if (event.getEventType().equals(RERATE_PENDING)) {
                String rerateId = arr[arr.length - 1];
                Rerate rerate = getRerateById(rerateId);
                if (rerate == null) {
                    return;
                }

                String loanId = rerate.getLoanId();
                Loan loan = getLoanById(loanId);

                ReratePendingCancelRule rule = config.getRerateRules()
                    .getPendingCancelRule(rerate, loan, botPartyId);
                if (rule == null || !rule.shouldCancel()) {
                    return; //default to ignore/ not cancelling
                }
                process(startTime, rule, loan, rerate);
            }
        } catch (APIException e) {
            logger.error("Unable to process rerate event", e);
        }
    }
}