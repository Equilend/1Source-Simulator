package com.equilend.simulator;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Trade.Trade;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;

public class User 
{
    private Map<String, String> loginInfo;
    private Token token;
    private PartyRole role;
    private PartyRole counterRole;
    private static final Logger logger = LogManager.getLogger();

    //Should assert isValid before use..
    public User(Map<String, String> loginInfo, PartyRole role) 
    {
        this.loginInfo = loginInfo;
        this.role = role;
        this.counterRole = (role == PartyRole.LENDER) ? PartyRole.BORROWER : PartyRole.LENDER;
    }

    public boolean isValid (){
        return refreshToken();
    }

    public boolean refreshToken()
    {
        try {
            this.token = APIConnector.getBearerToken(loginInfo);
        } catch (APIException e){
            return false;
        }
        return true;
    }

    public boolean proposeContract(Trade trade)
    {
        ContractProposal contractProposal = ContractProposal.createContractProposal(trade);
    
        try {
            APIConnector.postContractProposal(token, contractProposal);
        } catch (APIException e) {
            return false;
        }
        return true;
        
    }

    public boolean cancelContractProposal(String contractId) 
    {
        try {
            APIConnector.cancelContractProposal(token, contractId);
        } catch (APIException e) {
            return false;
        }
        return true;
    }

    public boolean acceptContractProposal(String contractId) 
    {
        Settlement settlement = ContractProposal.createSettlement(this.role);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        try {
            APIConnector.acceptContractProposal(token, contractId, acceptSettlement);
        } catch (APIException e) {
            return false;
        }
        return true;
    }

    public boolean declineContractProposal(String contractId) 
    {
        try {
            APIConnector.declineContractProposal(token, contractId);
        } catch (APIException e) {
            return false;
        }
        return true;
    }    

    private String getCounterPartyId(Agreement agreement)
    {
        Trade trade = agreement.getTrade();
        List<TransactingParty> transactingParties = trade.getTransactingParties();
        for (TransactingParty tp : transactingParties){
            if (tp.getPartyRole() == this.counterRole){
                return tp.getParty().getPartyId();
            }
        }
        return "";
    }

    public boolean proposeContractsFromAgreements(OffsetDateTime since, OffsetDateTime before, String partyId) 
    {
        List<Agreement> agreements;
        try {
            agreements = APIConnector.getAllAgreements(token, since, before);
        } catch (APIException e) {
            return false;
        }
        if (agreements.size() == 0){
            logger.info("No new trade agreements to create contracts from");
        } 
        
        for (Agreement agreement : agreements){
            if (partyId.equals("*")){
                if (!proposeContract(agreement.getTrade())){
                    return false;
                }
            }
            else{
                if (getCounterPartyId(agreement).equals(partyId)){
                    if (!proposeContract(agreement.getTrade())){
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public boolean acceptContractProposals(OffsetDateTime since, OffsetDateTime before)
    {
        List<Contract> contracts;
        try {
            contracts = APIConnector.getAllContracts(token, since, before);
        } catch (APIException e) {
            return false;
        }
        if (contracts.size() == 0){
            logger.info("No new contract proposals");
        } 

        for (Contract contract : contracts){
            if (contract.getContractStatus().equals("PROPOSED")){
                if(!acceptContractProposal(contract.getContractId())){
                    return false;
                }
            }
        }
        
        return true;
    }

}
