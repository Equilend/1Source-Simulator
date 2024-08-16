package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rate.FixedRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
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

}
