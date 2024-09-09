package com.equilend.simulator.rules_processor;

import static com.equilend.simulator.utils.RuleProcessorUtil.waitForDelay;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.configurator.Config;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementProposeRule;
import com.equilend.simulator.configurator.rules.agreement_rules.AgreementRule;
import com.equilend.simulator.model.agreement.Agreement;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.venue.VenueTradeAgreement;
import com.equilend.simulator.service.LoanService;
import com.equilend.simulator.service.TradeService;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TradeRuleProcessor {

    private static final Logger logger = LogManager.getLogger(TradeRuleProcessor.class.getName());

    public static void process(Long startTime, AgreementRule rule, Agreement agreement)
        throws APIException {

        if (rule instanceof AgreementProposeRule) {
            logger.debug("Processing AgreementProposeRule. Agreement: " + agreement.getAgreementId());
            processByAgreementProposeRule(startTime, (AgreementProposeRule) rule, agreement);
        }
    }

    private static void processByAgreementProposeRule(Long startTime, AgreementProposeRule rule, Agreement agreement)
        throws APIException {
        waitForDelay(startTime, rule.getDelay());
        VenueTradeAgreement trade = agreement.getTrade();
        String botPartyId = Config.getInstance().getBotPartyId();
        Optional<TransactingParty> transactingPartyById = TradeService.getTransactingPartyById(trade, botPartyId);
        if (transactingPartyById.isEmpty()) {
            logger.error(
                "Unable to propose loan due to error retrieving bot party id and/or bot party role. Agreement: "
                    + agreement.getAgreementId());
            return;
        }
        PartyRole botPartyRole = transactingPartyById.get().getPartyRole();
        LoanService.postLoanProposal(trade, botPartyRole);
    }
}
