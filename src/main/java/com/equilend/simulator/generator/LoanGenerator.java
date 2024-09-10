package com.equilend.simulator.generator;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.loan.LoanProposal;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.service.LoanService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class LoanGenerator implements Runnable {

    private static final Logger logger = LogManager.getLogger(LoanGenerator.class.getName());
    private String botPartyRole;
    private String botPartyId;
    private String counterPartyId;
    private String security;
    private Integer quantity;
    private Double rate;
    private Double price;
    private String termType;

    public LoanGenerator(String botPartyRole, String botPartyId, String counterPartyId, String security,
        Integer quantity, Double rate, Double price, String termType) {
        this.botPartyRole = botPartyRole;
        this.botPartyId = botPartyId;
        this.counterPartyId = counterPartyId;
        this.security = security;
        this.quantity = quantity;
        this.rate = rate;
        this.price = price;
        this.termType = termType;
    }

    public void run() {
        try {
            PartyRole partyRole = (botPartyRole.equalsIgnoreCase("LENDER")) ? PartyRole.LENDER : PartyRole.BORROWER;
            LoanProposal proposal = LoanService.createLoanProposal(partyRole, botPartyId, counterPartyId,
                security, quantity, rate, price, termType);
            APIConnector.postLoanProposal(OneSourceToken.getToken(), proposal);
        } catch (APIException e) {
            logger.error("Unable to propose scheduled loan proposal", e);
        }catch (NumberFormatException e) {
            logger.error("Wrong LOANS outgoing rule", e);
            new RuntimeException("Wrong LOANS outgoing rule", e);
        }
    }

}