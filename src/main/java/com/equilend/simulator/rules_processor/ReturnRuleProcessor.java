package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.return_rules.ReturnAcknowledgeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromContractRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromRecallRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnSettlementStatusUpdateRule;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.returns.AcknowledgementType;
import com.equilend.simulator.model.returns.Return;
import com.equilend.simulator.model.returns.ReturnAcknowledgement;
import com.equilend.simulator.model.returns.ReturnProposal;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.model.settlement.SettlementStatus;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Set;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReturnRuleProcessor {

    private static final Logger logger = LogManager.getLogger(ReturnRuleProcessor.class.getName());

    public static void process(Long startTime, ReturnRule rule, Contract contract, Return oneSourceReturn)
        throws APIException {

        if (rule instanceof ReturnAcknowledgeRule) {
            processReturnByAcknowledgeRule(startTime, (ReturnAcknowledgeRule) rule, contract,
                oneSourceReturn.getReturnId());
        }
        if (rule instanceof ReturnCancelRule) {
            processReturnByCancelRule(startTime, (ReturnCancelRule) rule, contract.getContractId(),
                oneSourceReturn.getReturnId());
        }

        if (rule instanceof ReturnProposeFromContractRule) {
            processByProposeRule(startTime, (ReturnProposeFromContractRule) rule, contract);
        }

        if (rule instanceof ReturnProposeFromRecallRule) {
            processByProposeRule(startTime, (ReturnProposeFromRecallRule) rule, contract);
        }

        if (rule instanceof ReturnSettlementStatusUpdateRule) {
            processReturnBySettlementStatusUpdateRule(startTime, (ReturnSettlementStatusUpdateRule) rule,
                contract.getContractId(), oneSourceReturn.getReturnId());
        }

    }

    private static void processReturnByAcknowledgeRule(Long startTime, ReturnAcknowledgeRule rule, Contract contract,
        String returnId) throws APIException {
        if (rule.shouldAcknowledgePositively()) {
            Double delay = rule.getDelay();
            postReturnAcknowledgement(contract, returnId, startTime, delay, AcknowledgementType.POSITIVE);
        }
        if (rule.shouldAcknowledgeNegatively()) {
            Double delay = rule.getDelay();
            postReturnAcknowledgement(contract, returnId, startTime, delay, AcknowledgementType.NEGATIVE);
        }
    }

    private static void postReturnAcknowledgement(Contract contract, String returnId, Long startTime, Double delay,
        AcknowledgementType type) throws APIException {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        ReturnAcknowledgement returnAcknowledgement = buildReturnAcknowledgement(type, contract);
        APIConnector.postReturnAck(OneSourceToken.getToken(), contract.getContractId(), returnId,
            returnAcknowledgement);
    }

    private static ReturnAcknowledgement buildReturnAcknowledgement(AcknowledgementType type, Contract contract) {
        ReturnAcknowledgement returnAcknowledgement = new ReturnAcknowledgement();
        PartySettlementInstruction partySettlementInstruction = new PartySettlementInstruction().partyRole(
            PartyRole.LENDER);
        if (contract.getSettlement() != null && contract.getSettlement().size() > 0) {
            partySettlementInstruction.internalAcctCd(contract.getSettlement().get(0).getInternalAcctCd())
                .instruction(contract.getSettlement().get(0).getInstruction());
        }
        returnAcknowledgement.acknowledgementType(type)
            .settlement(partySettlementInstruction);
        return returnAcknowledgement;
    }

    private static void processReturnByCancelRule(Long startTime, ReturnCancelRule rule, String contractId,
        String returnId) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.cancelReturn(OneSourceToken.getToken(), contractId, returnId);
    }

    private static void processByProposeRule(Long startTime, ReturnProposeFromContractRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ReturnProposal returnProposal = buildReturnProposal(contract, rule);
        APIConnector.proposeReturn(OneSourceToken.getToken(), contract.getContractId(), returnProposal);
    }

    private static ReturnProposal buildReturnProposal(Contract contract, ReturnProposeFromContractRule rule) throws RuleException {
        Set<String> ruleReturnQuantity = rule.getReturnQuantity();
        Integer quantity;
        try {
            quantity = Integer.parseInt(ruleReturnQuantity.stream().findFirst().get());
        } catch (NumberFormatException | NoSuchElementException e) {
            logger.error("Return Propose Rule (from CONTRACT_OPENED event) must contain 'return_quantity' as number for new Return Propose");
            throw new RuleException(
                "Return Propose Rule (from CONTRACT_OPENED event) must contain 'return_quantity' as number for new Return Propose");
        }
        ReturnProposal returnProposal = new ReturnProposal();
        returnProposal.quantity(quantity)
            .returnDate(LocalDate.now())
            .returnSettlementDate(LocalDate.now())
            .collateralValue(contract.getTrade().getCollateral().getCollateralValue())
            .settlementType(contract.getTrade().getSettlementType());
        return returnProposal;
    }

    private static void processByProposeRule(Long startTime, ReturnProposeFromRecallRule rule, Contract contract)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ReturnProposal returnProposal = buildReturnProposal(contract, rule);
        APIConnector.proposeReturn(OneSourceToken.getToken(), contract.getContractId(), returnProposal);
    }

    private static ReturnProposal buildReturnProposal(Contract contract, ReturnProposeFromRecallRule rule) {
        Set<String> ruleReturnQuantity = rule.getRecallQuantity();
        Integer quantity;
        try {
            quantity = Integer.parseInt(ruleReturnQuantity.stream().findFirst().get());
        } catch (NumberFormatException | NoSuchElementException e) {
            logger.error("Return Propose Rule (from RECALL_OPENED event) must contain 'recall_quantity' as number for new Return Propose");
            throw new RuleException(
                "Return Propose Rule (from RECALL_OPENED event) must contain 'recall_quantity' as number for new Return Propose");
        }
        ReturnProposal returnProposal = new ReturnProposal();
        returnProposal.quantity(quantity)
            .returnDate(LocalDate.now())
            .returnSettlementDate(LocalDate.now())
            .collateralValue(contract.getTrade().getCollateral().getCollateralValue())
            .settlementType(contract.getTrade().getSettlementType());
        return returnProposal;
    }

    private static void processReturnBySettlementStatusUpdateRule(Long startTime, ReturnSettlementStatusUpdateRule rule,
        String contractId, String returnId) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.instructReturnSettlementStatus(OneSourceToken.getToken(), contractId, returnId,
            SettlementStatus.SETTLED);
    }
}
