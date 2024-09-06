package com.equilend.simulator.service;

import static com.equilend.simulator.model.collateral.RoundingMode.ALWAYSUP;
import static com.equilend.simulator.service.SettlementService.createPartySettlementInstruction;
import static com.equilend.simulator.service.TradeService.createTrade;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.loan.LoanProposal;
import com.equilend.simulator.model.loan.LoanProposalApproval;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.model.settlement.SettlementStatus;
import com.equilend.simulator.model.trade.TradeAgreement;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoanService {

    private static final Logger logger = LogManager.getLogger(LoanService.class.getName());

    public static Optional<TransactingParty> getTransactingPartyById(Loan loan, String botPartyId) {
        return TradeService.getTransactingPartyById(loan.getTrade(), botPartyId);
    }

    public static LoanProposal createLoanProposal(TradeAgreement trade, PartyRole partyRole) {
        trade.setDividendRatePct(Double.valueOf(100));

        if (partyRole == PartyRole.LENDER) {
            trade.getCollateral().setRoundingRule(10);
        }
        if (partyRole == PartyRole.LENDER) {
            trade.getCollateral().setRoundingMode(ALWAYSUP);
        }
        trade.getCollateral().setMargin(Double.valueOf(102));
        updateTradePrice(trade, "S");

        PartySettlementInstruction partySettlementInstruction = createPartySettlementInstruction(
            partyRole);

        LoanProposal loanProposal = new LoanProposal().trade(trade)
            .settlement(List.of(partySettlementInstruction));
        return loanProposal;
    }

    public static LoanProposal createLoanProposal(PartyRole partyRole, String partyId, String counterPartyId,
        String security, Integer quantity, Double rate, Double price, String termType) {

        TradeAgreement trade = createTrade(partyRole, partyId, counterPartyId, security, quantity, rate, price, termType);

        PartySettlementInstruction partySettlementInstruction = createPartySettlementInstruction(
            partyRole);

        LoanProposal loanProposal = new LoanProposal().trade(trade)
            .settlement(List.of(partySettlementInstruction));
        return loanProposal;
    }

    private static void updateTradePrice(TradeAgreement trade, String idType) {
        String idValue = null;
        switch (idType.toUpperCase()) {
            case "S":
                idType = "sedol";
                idValue = trade.getInstrument().getSedol();
                break;
            case "I":
                idType = "isin";
                idValue = trade.getInstrument().getIsin();
                break;
            case "C":
                idType = "cusip";
                idValue = trade.getInstrument().getCusip();
                break;
            case "F":
                idType = "figi";
                idValue = null;
                break;
            default:
                idType = "ticker";
                idValue = trade.getInstrument().getTicker();
                break;
        }
        if (idValue == null) {
            return;
        }
        double price = 250;
        try {
            price = DatalendAPIConnector.getSecurityPrice(DatalendToken.getToken(), idType, idValue);
            // logger.debug("{} {} has price {}", idType, idValue, price);
        } catch (APIException e) {
            logger.debug("Unable to get current price for security w {} {}, default to $250", idType, idValue);
        }
        double contractValue = price * trade.getQuantity();
        trade.getCollateral().setContractValue(Double.valueOf(contractValue));
        double collateralValue = contractValue * trade.getCollateral().getMargin().doubleValue() / 100.0;
        trade.getCollateral().setCollateralValue(Double.valueOf(collateralValue));
    }

    public static boolean isInitiator(Loan loan, String botPartyId) {
        // Currently, lender only provides its settlement info it only has lender settlement on contract
        //But borrower creates both lender and borrower settlement, even if lender is empty
        boolean lenderInitiated = loan.getSettlement().size() == 1;
        Optional<TransactingParty> transactingPartyOptional = LoanService.getTransactingPartyById(loan,
            botPartyId);
        if (lenderInitiated) {
            return transactingPartyOptional.isPresent()
                && transactingPartyOptional.get().getPartyRole() == PartyRole.LENDER;
        }
        //Of course, this won't work if lender provides both its own and the borrower's settlement info
        //But this is the best we can do until initiator party id given in contract json
        return transactingPartyOptional.isPresent()
            && transactingPartyOptional.get().getPartyRole() == PartyRole.BORROWER;
    }

    public static Loan getLoanById(String loanId) throws APIException {
        return APIConnector.getLoanById(EventHandler.getToken(), loanId);
    }

    public static void cancelLoan(String loanId) throws APIException {
        APIConnector.cancelLoan(OneSourceToken.getToken(), loanId);
    }

    public static void acceptLoan(String loanId, PartyRole role)
        throws APIException {
        PartySettlementInstruction partySettlementInstruction = createPartySettlementInstruction(
            role);
        LoanProposalApproval loanProposalApproval = new LoanProposalApproval()
            .internalRefId(UUID.randomUUID().toString())
            .settlement(partySettlementInstruction);
        if (role == PartyRole.LENDER) {
            loanProposalApproval = loanProposalApproval.roundingRule(10).roundingMode(ALWAYSUP);
        }
        APIConnector.acceptLoan(OneSourceToken.getToken(), loanId, loanProposalApproval);
    }

    public static void declineLoan(String loanId) throws APIException {
        APIConnector.declineLoan(OneSourceToken.getToken(), loanId);
    }

    public static void cancelPendingLoan(String loanId) throws APIException {
        APIConnector.cancelPendingLoan(OneSourceToken.getToken(), loanId);
    }

    public static void updateLoanSettlementStatus(String loanId, SettlementStatus settlementStatus)
        throws APIException {
        APIConnector.instructLoanSettlementStatus(OneSourceToken.getToken(), loanId, settlementStatus);
    }
}
