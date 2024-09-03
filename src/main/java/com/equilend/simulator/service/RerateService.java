package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateService {

    private static final Logger logger = LogManager.getLogger(RerateService.class.getName());

    public static int postRerateProposal(Loan loan, Double delta) throws APIException {
        Rate rate = loan.getTrade().getRate();
        FixedRateDef fee = rate.getFee();
        RebateRate rebate = rate.getRebate();

        if (fee != null) {
            fee.setBaseRate(Math.max(fee.getBaseRate() + delta, 0.01));
        } else if (rebate != null) {
            if (rebate.getFixed() != null) {
                rebate.getFixed().setBaseRate(rebate.getFixed().getBaseRate() + delta);
            } else if (rebate.getFloating() != null) {
                rebate.getFloating().setSpread(rebate.getFloating().getSpread() + delta);
            }
        }
        int responseCode = APIConnector.postRerateProposal(EventHandler.getToken(), loan.getLoanId(),
            new RerateProposal().rate(rate));
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
