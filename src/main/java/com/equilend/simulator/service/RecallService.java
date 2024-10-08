package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.AcknowledgementType;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.recall.Recall;
import com.equilend.simulator.model.recall.RecallAcknowledgement;
import com.equilend.simulator.model.recall.RecallProposal;
import com.equilend.simulator.model.venue.Venue;
import java.time.LocalDate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecallService {

    private static final Logger logger = LogManager.getLogger(RecallService.class.getName());

    public static Recall getRecallById(String recallId) throws APIException {
        return APIConnector.getRecallById(EventHandler.getToken(), recallId);
    }

    public static int proposeRecall(Loan loan, Integer quantity) throws APIException {
        RecallProposal recallProposal = buildRecallProposal(loan, quantity);
        return APIConnector.proposeRecall(OneSourceToken.getToken(), loan.getLoanId(), recallProposal);
    }

    private static RecallProposal buildRecallProposal(Loan loan, Integer quantity) {
        String botPartyId = Config.getInstance().getBotPartyId();
        Venue executionVenue = loan.getTrade().getVenues().stream()
            .filter(venue -> botPartyId.equals(venue.getParty().getPartyId())).findFirst().get();
        RecallProposal recallProposal = new RecallProposal()
            .quantity(quantity)
            .executionVenue(executionVenue)
            .recallDueDate(LocalDate.now().plusDays(1))
            .recallDate(LocalDate.now());
        return recallProposal;
    }

    public static int cancelRecall(String loanId, String recallId) throws APIException {
        return APIConnector.cancelRecall(OneSourceToken.getToken(), loanId, recallId);
    }

    public static int postRecallAck(Loan loan, Recall recall, AcknowledgementType acknowledgementType) throws APIException {
        RecallAcknowledgement recallAcknowledgement = new RecallAcknowledgement().acknowledgementType(
            acknowledgementType);
        return APIConnector.postRecallAck(OneSourceToken.getToken(), loan.getLoanId(), recall.getRecallId(),
            recallAcknowledgement);
    }
}
