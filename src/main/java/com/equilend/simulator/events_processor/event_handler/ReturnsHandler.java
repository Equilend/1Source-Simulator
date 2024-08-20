package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.ContractService.getContractById;
import static com.equilend.simulator.service.ReturnService.getReturnById;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.return_rules.ReturnAcknowledgeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnSettlementStatusUpdateRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.returns.Return;
import com.equilend.simulator.rules_processor.ReturnRuleProcessor;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReturnsHandler implements EventHandler {

    private static final Logger logger = LogManager.getLogger(ReturnsHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public ReturnsHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }

    public void run() {
        String uri = event.getResourceUri();
        String[] arr = uri.split("/");
        String returnId = arr[arr.length - 1];
        try {
            Return oneSourceReturn = getReturnById(returnId);
            if (oneSourceReturn == null) {
                return;
            }
            Contract contract = getContractById(oneSourceReturn.getContractId());
            boolean isInitiator = ContractService.isInitiator(contract, botPartyId);

            switch (event.getEventType()) {
                case RETURN_PENDING:

                    ReturnAcknowledgeRule acknowledgeRule = configurator.getReturnRules()
                        .getReturnAcknowledgeRule(oneSourceReturn, contract, botPartyId);
                    if (!isInitiator && acknowledgeRule != null && !acknowledgeRule.isIgnored()) {
                        ReturnRuleProcessor.process(startTime, acknowledgeRule, contract, oneSourceReturn);
                        return;
                    }

                    ReturnCancelRule cancelRule = configurator.getReturnRules()
                        .getReturnCancelRule(oneSourceReturn, contract, botPartyId);
                    if (isInitiator && cancelRule != null && !cancelRule.isIgnored()) {
                        ReturnRuleProcessor.process(startTime, cancelRule, contract, oneSourceReturn);
                        return;
                    }

                    ReturnSettlementStatusUpdateRule returnSettlementStatusUpdateRule = configurator.getReturnRules()
                        .getReturnSettlementStatusUpdateRule(oneSourceReturn, contract, botPartyId);
                    if (returnSettlementStatusUpdateRule != null && !returnSettlementStatusUpdateRule.isIgnored()) {
                        ReturnRuleProcessor.process(startTime, returnSettlementStatusUpdateRule, contract,
                            oneSourceReturn);
                        return;
                    }

                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process rerate event", e);
        }

    }

}