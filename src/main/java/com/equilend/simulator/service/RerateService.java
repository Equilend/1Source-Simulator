package com.equilend.simulator.service;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.os.client.model.FeeRate;
import com.os.client.model.FixedRate;
import com.os.client.model.FloatingRate;
import com.os.client.model.Loan;
import com.os.client.model.Rate;
import com.os.client.model.RebateRate;
import com.os.client.model.Rerate;
import com.os.client.model.RerateProposal;
import com.os.client.model.Venue;

public class RerateService {

    private static final Logger logger = LogManager.getLogger(RerateService.class.getName());

    public static int postRerateProposal(Loan loan, Double delta) throws APIException {
        
    	Rate rate = loan.getTrade().getRate();
        
        if (rate instanceof FeeRate) {
        	FeeRate feeRate = (FeeRate)rate;
        	feeRate.getFee().setBaseRate(Math.max(feeRate.getFee().getBaseRate() + delta, 0.01));
        } else if (rate instanceof RebateRate) {
        	RebateRate rebateRate = (RebateRate)rate;
        	if (rebateRate.getRebate() instanceof FixedRate) {
        		FixedRate fixedRebateRate = (FixedRate)rebateRate.getRebate();
        		fixedRebateRate.getFixed().setBaseRate(fixedRebateRate.getFixed().getBaseRate() + delta);
        	} else if (rebateRate.getRebate() instanceof FloatingRate) {
        		FloatingRate floatingRebateRate = (FloatingRate)rebateRate.getRebate();
        		floatingRebateRate.getFloating().setSpread(floatingRebateRate.getFloating().getSpread() + delta);
        	}
        }

        String botPartyId = Config.getInstance().getBotPartyId();

        Venue executionVenue = loan.getTrade().getVenues().stream()
            .filter(venue -> botPartyId.equals(venue.getParty().getPartyId())).findFirst().get();
        RerateProposal rerateProposal = new RerateProposal()
            .executionVenue(executionVenue)
            .rate(rate);
        int responseCode = APIConnector.postRerateProposal(EventHandler.getToken(), loan.getLoanId(),
            rerateProposal);
        return responseCode;
    }

    public static Rerate getRerateById(String rerateId) throws APIException {
        return APIConnector.getRerateById(EventHandler.getToken(), rerateId);
    }

    public static int cancelRerateProposal(Loan loan, Rerate rerate) throws APIException {
        return APIConnector.cancelReratePending(OneSourceToken.getToken(), loan.getLoanId(),
            rerate.getRerateId());
    }

    public static int approveRerateProposal(Loan loan, Rerate rerate) throws APIException {
        return APIConnector.approveRerateProposal(OneSourceToken.getToken(), loan.getLoanId(),
            rerate.getRerateId());
    }

    public static int declineRerateProposal(Loan loan, Rerate rerate) throws APIException {
        return APIConnector.declineRerateProposal(OneSourceToken.getToken(), loan.getLoanId(),
            rerate.getRerateId());
    }

}
