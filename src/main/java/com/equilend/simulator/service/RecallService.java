package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.recall_rules.RecallProposeRule;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.model.recall.RecallProposal;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallService {

    private static final Logger logger = LogManager.getLogger(RecallService.class.getName());

    public static Recall getRecallById(String recallId) throws APIException {
        return APIConnector.getRecallById(EventHandler.getToken(), recallId);
    }

    public static int proposeRecall(Contract contract, Integer quantity) throws APIException {
        RecallProposal recallProposal = buildRecallProposal(contract, quantity);
        return APIConnector.proposeRecall(OneSourceToken.getToken(), contract.getContractId(), recallProposal);
    }

    private static RecallProposal buildRecallProposal(Contract contract, Integer quantity) {
        RecallProposal recallProposal = new RecallProposal();
        recallProposal.setQuantity(quantity);
        recallProposal.setRecallDate(LocalDate.now());
        return recallProposal;
    }

    public static int cancelRecall(String contractId, String recallId) throws APIException {
        return APIConnector.cancelRecall(OneSourceToken.getToken(), contractId, recallId);
    }
}
