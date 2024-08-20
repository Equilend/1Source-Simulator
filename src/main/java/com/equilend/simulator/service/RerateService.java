package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RerateService {

    private static final Logger logger = LogManager.getLogger(RerateService.class.getName());

    public static int postRerateProposal(Contract contract, Double delta) throws APIException {
        Rate rate = contract.getTrade().getRate();
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
        int responseCode = APIConnector.postRerateProposal(EventHandler.getToken(), contract.getContractId(),
            new RerateProposal().rate(rate));
        return responseCode;
    }

    public static Rerate getRerateById(String rerateId) throws APIException {
        return APIConnector.getRerateById(EventHandler.getToken(), rerateId);
    }

    public static int cancelRerateProposal(Contract contract, Rerate rerate) throws APIException {
        return APIConnector.cancelReratePending(OneSourceToken.getToken(), contract.getContractId(),
            rerate.getRerateId());
    }

    public static int approveRerateProposal(Contract contract, Rerate rerate) throws APIException {
        return APIConnector.approveRerateProposal(OneSourceToken.getToken(), contract.getContractId(),
            rerate.getRerateId());
    }

    public static int declineRerateProposal(Contract contract, Rerate rerate) throws APIException {
        return APIConnector.declineRerateProposal(OneSourceToken.getToken(), contract.getContractId(),
            rerate.getRerateId());
    }

}
