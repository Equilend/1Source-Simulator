package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.rules.RuleException;
import com.equilend.simulator.configurator.rules.return_rules.ReturnAcknowledgeRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnCancelRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromLoanRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnProposeFromRecallRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnRule;
import com.equilend.simulator.configurator.rules.return_rules.ReturnSettlementStatusUpdateRule;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.AcknowledgementType;
import com.equilend.simulator.model.returns.ModelReturn;
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

    public static void process(Long startTime, ReturnRule rule, Loan loan, ModelReturn oneSourceReturn)
        throws APIException {

        if (rule instanceof ReturnAcknowledgeRule) {
            processReturnByAcknowledgeRule(startTime, (ReturnAcknowledgeRule) rule, loan,
                oneSourceReturn.getReturnId());
        }
        if (rule instanceof ReturnCancelRule) {
            processReturnByCancelRule(startTime, (ReturnCancelRule) rule, loan.getLoanId(),
                oneSourceReturn.getReturnId());
        }

        if (rule instanceof ReturnProposeFromLoanRule) {
            processByProposeRule(startTime, (ReturnProposeFromLoanRule) rule, loan);
        }

        if (rule instanceof ReturnProposeFromRecallRule) {
            processByProposeRule(startTime, (ReturnProposeFromRecallRule) rule, loan);
        }

        if (rule instanceof ReturnSettlementStatusUpdateRule) {
            processReturnBySettlementStatusUpdateRule(startTime, (ReturnSettlementStatusUpdateRule) rule,
                loan.getLoanId(), oneSourceReturn.getReturnId());
        }

    }

    private static void processReturnByAcknowledgeRule(Long startTime, ReturnAcknowledgeRule rule, Loan loan,
        String returnId) throws APIException {
        if (rule.shouldAcknowledgePositively()) {
            Double delay = rule.getDelay();
            postReturnAcknowledgement(loan, returnId, startTime, delay, AcknowledgementType.POSITIVE);
        }
        if (rule.shouldAcknowledgeNegatively()) {
            Double delay = rule.getDelay();
            postReturnAcknowledgement(loan, returnId, startTime, delay, AcknowledgementType.NEGATIVE);
        }
    }

    private static void postReturnAcknowledgement(Loan loan, String returnId, Long startTime, Double delay,
        AcknowledgementType type) throws APIException {
        long delayMillis = Math.round(1000 * delay);
        while (System.currentTimeMillis() - startTime < delayMillis) {
            Thread.yield();
        }
        ReturnAcknowledgement returnAcknowledgement = buildReturnAcknowledgement(type, loan);
        APIConnector.postReturnAck(OneSourceToken.getToken(), loan.getLoanId(), returnId,
            returnAcknowledgement);
    }

    private static ReturnAcknowledgement buildReturnAcknowledgement(AcknowledgementType type, Loan loan) {
        ReturnAcknowledgement returnAcknowledgement = new ReturnAcknowledgement();
        PartySettlementInstruction partySettlementInstruction = new PartySettlementInstruction().partyRole(
            PartyRole.LENDER);
        if (loan.getSettlement() != null && loan.getSettlement().size() > 0) {
            partySettlementInstruction.internalAcctCd(loan.getSettlement().get(0).getInternalAcctCd())
                .instruction(loan.getSettlement().get(0).getInstruction());
        }
        returnAcknowledgement.acknowledgementType(type)
            .settlement(partySettlementInstruction);
        return returnAcknowledgement;
    }

    private static void processReturnByCancelRule(Long startTime, ReturnCancelRule rule, String loanId,
        String returnId) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.cancelReturn(OneSourceToken.getToken(), loanId, returnId);
    }

    private static void processByProposeRule(Long startTime, ReturnProposeFromLoanRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ReturnProposal returnProposal = buildReturnProposal(loan, rule);
        APIConnector.proposeReturn(OneSourceToken.getToken(), loan.getLoanId(), returnProposal);
    }

    private static ReturnProposal buildReturnProposal(Loan loan, ReturnProposeFromLoanRule rule) throws RuleException {
        Set<String> ruleReturnQuantity = rule.getReturnQuantity();
        Integer quantity;
        try {
            quantity = Integer.parseInt(ruleReturnQuantity.stream().findFirst().get());
        } catch (NumberFormatException | NoSuchElementException e) {
            logger.error("Return Propose Rule (from LOAN_OPENED event) must contain 'return_quantity' as number for new Return Propose");
            throw new RuleException(
                "Return Propose Rule (from LOAN_OPENED event) must contain 'return_quantity' as number for new Return Propose");
        }
        ReturnProposal returnProposal = new ReturnProposal();
        returnProposal.quantity(quantity)
            .returnDate(LocalDate.now())
            .returnSettlementDate(LocalDate.now())
            .collateralValue(loan.getTrade().getCollateral().getCollateralValue())
            .settlementType(loan.getTrade().getSettlementType());
        return returnProposal;
    }

    private static void processByProposeRule(Long startTime, ReturnProposeFromRecallRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        ReturnProposal returnProposal = buildReturnProposal(loan, rule);
        APIConnector.proposeReturn(OneSourceToken.getToken(), loan.getLoanId(), returnProposal);
    }

    private static ReturnProposal buildReturnProposal(Loan loan, ReturnProposeFromRecallRule rule) {
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
            .collateralValue(loan.getTrade().getCollateral().getCollateralValue())
            .settlementType(loan.getTrade().getSettlementType());
        return returnProposal;
    }

    private static void processReturnBySettlementStatusUpdateRule(Long startTime, ReturnSettlementStatusUpdateRule rule,
        String loanId, String returnId) throws APIException {
        waitForDelay(startTime, rule.getDelay());
        APIConnector.instructReturnSettlementStatus(OneSourceToken.getToken(), loanId, returnId,
            SettlementStatus.SETTLED);
    }
}
