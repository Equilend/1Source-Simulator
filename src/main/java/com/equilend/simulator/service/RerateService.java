package com.equilend.simulator.service;

import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.events_processor.event_handler.EventHandler;
import com.os.client.model.Contract;
import com.os.client.model.FeeRate;
import com.os.client.model.FixedRate;
import com.os.client.model.FixedRateDef;
import com.os.client.model.FloatingRate;
import com.os.client.model.FloatingRateDef;
import com.os.client.model.RebateRate;
import com.os.client.model.Rerate;
import com.os.client.model.RerateProposal;
import com.os.client.model.Venue;
import com.os.client.model.VenueType;

public class RerateService {

    private static final Logger logger = LogManager.getLogger(RerateService.class.getName());

    public static int postRerateProposal(Contract contract, Double delta) throws APIException {
		RerateProposal proposal = new RerateProposal();

		Venue venue = new Venue();
		venue.setType(VenueType.OFFPLATFORM);
		venue.setVenueRefKey("CONSOLE" + System.currentTimeMillis());

		proposal.setExecutionVenue(venue);

		LocalDate rerateDate = LocalDate.now(ZoneId.of("UTC"));
		
		if (contract.getTrade().getRate() instanceof FeeRate) {
			
			FeeRate feeRate = new FeeRate();
			
			FixedRateDef fixedRateDef = new FixedRateDef();
			fixedRateDef.setBaseRate(delta);
			fixedRateDef.setEffectiveDate(rerateDate);
			feeRate.setFee(fixedRateDef);
			
			proposal.setRate(feeRate);

		} else if (contract.getTrade().getRate() instanceof RebateRate) {

			RebateRate rebateRate = new RebateRate();

			if (((RebateRate) contract.getTrade().getRate()).getRebate() instanceof FixedRate) {
			
				FixedRate fixedRate = new FixedRate();
				FixedRateDef fixedRateDef = new FixedRateDef();
				fixedRateDef.setBaseRate(delta);
				fixedRateDef.setEffectiveDate(rerateDate);
				fixedRate.setFixed(fixedRateDef);
				
				rebateRate.setRebate(fixedRate);
				
			} else if (((RebateRate) contract.getTrade().getRate()).getRebate() instanceof FloatingRate) {
				
				FloatingRateDef origRate = ((FloatingRate)((RebateRate) contract.getTrade().getRate()).getRebate()).getFloating();
				
				FloatingRate floatingRate = new FloatingRate();
				FloatingRateDef floatingRateDef = new FloatingRateDef();
				floatingRateDef.setBenchmark(origRate.getBenchmark());
				floatingRateDef.setIsAutoRerate(origRate.isIsAutoRerate());
				if (!floatingRateDef.isIsAutoRerate()) {
					floatingRateDef.setBaseRate(origRate.getBaseRate());
				}
				floatingRateDef.setSpread(delta);
				floatingRateDef.setEffectiveDate(rerateDate);
				floatingRate.setFloating(floatingRateDef);
				
				rebateRate.setRebate(floatingRate);
			}

			proposal.setRate(rebateRate);
		}
		
        int responseCode = APIConnector.postRerateProposal(EventHandler.getToken(), contract.getContractId(),
        		proposal);
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
