package com.equilend.simulator.record_analyzer;

import static com.equilend.simulator.service.LoanService.acceptLoan;
import static com.equilend.simulator.service.LoanService.cancelLoan;
import static com.equilend.simulator.service.LoanService.declineLoan;
import static com.equilend.simulator.service.RerateService.postRerateProposal;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.loan_rules.LoanApproveRejectRule;
import com.equilend.simulator.configurator.rules.loan_rules.LoanCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.service.LoanService;
import com.equilend.simulator.service.RerateService;
import com.os.client.model.Loan;
import com.os.client.model.PartyRole;
import com.os.client.model.Rerate;

public class RecordAnalyzer {

    private static final Logger logger = LogManager.getLogger(RecordAnalyzer.class.getName());
    private Config config;
    private String botPartyId;
    private boolean rerateAnalysisMode;
    private boolean loanAnalysisMode;
    private String loanStartDate;

    public RecordAnalyzer() {
        this.config = Config.getInstance();
        this.botPartyId = config.getBotPartyId();
        if (config.getRerateRules() != null) {
            this.rerateAnalysisMode = config.getRerateRules().getAnalysisMode();
        }
        if (config.getLoanRules() != null) {
            this.loanAnalysisMode = config.getLoanRules().getAnalysisMode();
            this.loanStartDate = config.getLoanRules().getAnalysisStartDate();
            if (this.loanStartDate == null || this.loanStartDate.trim().length() == 0) {
            	loanStartDate = APIConnector.getCurrentTime().minusDays(1).format(DateTimeFormatter.ISO_LOCAL_DATE);
            }
            logger.debug("loanStartDate: " + loanStartDate);
        }
    }

    private Loan getLoanById(String loanId) {
        Loan loan = null;
        try {
            loan = APIConnector.getLoanById(OneSourceToken.getToken(), loanId);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve loan {}", loanId);
        }
        return loan;
    }

    private List<Loan> getLoans(String status) {
        List<Loan> loans = null;
        try {
            loans = APIConnector.getAllLoans(OneSourceToken.getToken(), status, loanStartDate);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve open loans");
        }
        return loans;
    }

    private List<Rerate> getOpenReratesOnLoan(String loanId) {
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllReratesOnLoan(OneSourceToken.getToken(), loanId);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve open rerates on loan {}", loanId);
        }

        return rerates;
    }

    private List<Rerate> getAllRerates() {
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllRerates(OneSourceToken.getToken());
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve approved rerates");
        }
        return rerates;
    }

    public void run() {
        if (rerateAnalysisMode) {
            // get all open rerates
            List<Rerate> rerates = getAllRerates();
            if (rerates != null) {
                for (Rerate rerate : rerates) {
                    try {
                        Loan loan = getLoanById(rerate.getLoanId());
                        if (loan == null) {
                            continue;
                        }

                        if (LoanService.getTransactingPartyById(loan, botPartyId).get().getPartyRole()
                            == PartyRole.BORROWER) {
                            // if bot is lender => initiator => cancel/ignore rules
                            RerateCancelRule rule = config.getRerateRules()
                                .getCancelRule(rerate, loan, botPartyId);
                            if (rule == null || !rule.shouldCancel()) {
                                continue;
                            }

                            RerateService.cancelRerateProposal(loan, rerate);
                        } else {
                            // if bot is borrower => recipient => approve/reject rules
                            RerateApproveRule rule = config.getRerateRules()
                                .getApproveRule(rerate, loan, botPartyId);
                            if (rule == null) {
                                continue;
                            }
                            if (rule.shouldApprove()) {
                                RerateService.approveRerateProposal(loan, rerate);
                            } else {
                                RerateService.declineRerateProposal(loan, rerate);
                            }
                        }
                    } catch (APIException e) {
                        logger.error("Unable to process analysis", e);
                    }
                }
            }
            // Get open loans to consider proposing rerates
            List<Loan> loans = getLoans("OPEN");
            if (loans != null) {
                for (Loan loan : loans) {
                    List<Rerate> reratesOnLoan = getOpenReratesOnLoan(loan.getLoanId());
                    if (reratesOnLoan.size() > 0) {
                        continue;
                    }

                    RerateProposeRule rule = config.getRerateRules()
                        .getProposeRule(loan, config.getBotPartyId());
                    if (rule == null || !rule.shouldPropose()) {
                        continue;
                    }
                    try {
                        postRerateProposal(loan, 0.0);
                    } catch (APIException e) {
                        logger.error("Unable to post rerate proposal", e);
                    }
                }
            }
        }
        if (loanAnalysisMode) {
            //get all proposed loans to consider accepting/declining/cancelling
            List<Loan> loans = getLoans("PROPOSED");
            if (loans != null) {
                for (Loan loan : loans) {
                    //determine whether will consider as initiator or as recipient
                    try {
                        if (LoanService.isInitiator(loan, botPartyId)) {
                            LoanCancelRule loanCancelRule = config.getLoanRules()
                                .getLoanCancelRule(loan, botPartyId);
                            if (loanCancelRule != null && loanCancelRule.shouldCancel()) {
                                cancelLoan(loan.getLoanId());
                            }
                        } else {
                            LoanApproveRejectRule rule = config.getLoanRules()
                                .getLoanApproveRejectRule(loan, botPartyId);
                            if (rule == null) {
                                continue;
                            }
                            if (rule.shouldApprove()) {
                                PartyRole partyRole = LoanService.getTransactingPartyById(loan, botPartyId).get()
                                    .getPartyRole();
                                acceptLoan(loan.getLoanId(), partyRole);
                            } else {
                                declineLoan(loan.getLoanId());
                            }
                        }
                    } catch (APIException e) {
                        logger.error("Unable to process loan", e);
                    }
                }
            }
        }
    }


}