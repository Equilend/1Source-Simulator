package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.service.LoanService.acceptLoan;
import static com.equilend.simulator.service.LoanService.cancelLoan;
import static com.equilend.simulator.service.LoanService.declineLoan;
import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.rules.loan_rules.LoanApproveRejectRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanCancelRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanPendingCancelRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanPendingUpdateRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanRule;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.settlement.SettlementStatus;
import com.equilend.simulator.service.LoanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoanRuleProcessor {

    private static final Logger logger = LogManager.getLogger(LoanRuleProcessor.class.getName());

    public static void process(Long startTime, LoanRule rule, Loan loan, String botPartyId)  throws APIException {

        if (rule instanceof LoanApproveRejectRule) {
            logger.debug("Processing Loan Rule: Approve or Reject Contract Proposals (LoanApproveRejectRule). Loan: " + loan.getLoanId());
            processByLoanApproveRejectRule(startTime, (LoanApproveRejectRule) rule, loan, botPartyId);
        }
    }

    public static void process(Long startTime, LoanRule rule, Loan loan)
        throws APIException {

        if (rule instanceof LoanCancelRule) {
            logger.debug("Processing Loan Rule: Cancel Contract Proposals (LoanCancelRule). Loan: " + loan.getLoanId());
            processByLoanCancelRule(startTime, (LoanCancelRule) rule, loan);
        }

        if (rule instanceof LoanPendingCancelRule) {
            logger.debug("Processing Loan Rule: Cancel Contract Pending (LoanPendingCancelRule). Loan: " + loan.getLoanId());
            processByLoanPendingCancelRule(startTime, (LoanPendingCancelRule) rule, loan);
        }

        if (rule instanceof LoanPendingUpdateRule) {
            logger.debug("Processing Loan Rule: Update Contract Pending settl. status (LoanPendingUpdateRule). Loan: " + loan.getLoanId());
            processByLoanPendingUpdateRule(startTime, (LoanPendingUpdateRule) rule, loan);
        }
    }

    private static void processByLoanApproveRejectRule(Long startTime, LoanApproveRejectRule rule,
        Loan loan, String botPartyId) throws APIException {
        if (rule.shouldApprove()) {
            PartyRole partyRole = LoanService.getTransactingPartyById(loan, botPartyId)
                .get()
                .getPartyRole();
            waitForDelay(startTime, rule.getDelay());
            acceptLoan(loan.getLoanId(), partyRole);
        }

        if (rule.shouldReject()) {
            waitForDelay(startTime, rule.getDelay());
            declineLoan(loan.getLoanId());
        }
    }

    private static void processByLoanCancelRule(Long startTime, LoanCancelRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        cancelLoan(loan.getLoanId());
    }


    private static void processByLoanPendingCancelRule(Long startTime, LoanPendingCancelRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        LoanService.cancelPendingLoan(loan.getLoanId());
    }

    private static void processByLoanPendingUpdateRule(Long startTime, LoanPendingUpdateRule rule, Loan loan)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        LoanService.updateLoanSettlementStatus(loan.getLoanId(), SettlementStatus.SETTLED);
    }
}
