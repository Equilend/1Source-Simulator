package com.equilend.simulator.events_processor.event_handler;

import static com.equilend.simulator.service.ContractService.getContractById;
import static com.equilend.simulator.service.SplitService.getSplit;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.split_rules.SplitApproveRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.event.Event;
import com.equilend.simulator.model.split.ContractSplit;
import com.equilend.simulator.rules_processor.SplitRuleProcessor;
import com.equilend.simulator.service.ContractService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class SplitHandler implements EventHandler{
    private static final Logger logger = LogManager.getLogger(SplitHandler.class.getName());
    private final Event event;
    private final Configurator configurator;
    private final String botPartyId;
    private final Long startTime;

    public SplitHandler(Event e, Configurator configurator, Long startTime) {
        this.event = e;
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.startTime = startTime;
    }
    @Override
    public void run() {
        String uri = event.getResourceUri();
        //uri format:  /v1/ledger/contracts/898c6707-a34f-43d4-abad-8f76ffb6437d/split/9af2327f-2a6e-49fd-835e-d1faac9fce6d
        String[] arr = uri.split("/");
        String contractSplitId = arr[arr.length - 1];
        String contractId = arr[arr.length - 3];
        try {
            ContractSplit contractSplit = getSplit(contractId, contractSplitId);
            if (contractSplit == null) {
                throw new APIException("Invalid contract split");
            }
            Contract contract = getContractById(contractId);
            if (contract == null) {
                throw new APIException("Invalid contract id");
            }
            boolean isInitiator = ContractService.isInitiator(contract, botPartyId);
            switch (event.getEventType()) {
                case CONTRACT_SPLIT_PROPOSED:
                    if (!isInitiator) {
                        SplitApproveRule splitApproveRule = configurator.getSplitRules()
                            .getSplitApproveRule(contractSplit, contract, botPartyId);
                        if (splitApproveRule != null && splitApproveRule.shouldApprove()) {
                            SplitRuleProcessor.process(startTime, splitApproveRule, contract, contractSplit);
                            return;
                        }
                    }
                    break;
                default:
                    throw new RuntimeException("event type not supported");
            }
        } catch (APIException e) {
            logger.error("Unable to process split event", e);
        }
    }
}
