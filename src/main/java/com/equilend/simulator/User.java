package com.equilend.simulator;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Trade.Trade;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;

public class User 
{
    private Map<String, String> loginInfo;
    Token token;
    private PartyRole role;
    private PartyRole counterRole;

    public User(Map<String, String> loginInfo, PartyRole role) throws TokenException, FileNotFoundException
    {
        this.loginInfo = loginInfo;
        refreshToken();
        this.role = role;
        this.counterRole = (role == PartyRole.LENDER) ? PartyRole.BORROWER : PartyRole.LENDER;
    }

    public void refreshToken() throws TokenException
    {
        try {
            this.token = APIConnector.getBearerToken(loginInfo);
        } catch (URISyntaxException | IOException | InterruptedException e){
            throw new TokenException("Error getting bearer token", e);
        }
    }

    public ContractProposalResponse proposeContract(Trade trade) throws URISyntaxException, IOException, InterruptedException
    {
        ContractProposal contractProposal = ContractProposal.createContractProposal(trade);
    
        ContractProposalResponse response = APIConnector.postContractProposal(token, contractProposal);
        return response;
    }

    public ContractProposalResponse cancelContractProposal(String contractId) throws URISyntaxException, IOException, InterruptedException 
    {
        return APIConnector.cancelContractProposal(token, contractId);
    }

    public ContractProposalResponse acceptContractProposal(String contractId) throws URISyntaxException, IOException, InterruptedException
    {
        Settlement settlement = ContractProposal.createSettlement(this.role);
        AcceptSettlement acceptSettlement = new AcceptSettlement(settlement);
        return APIConnector.acceptContractProposal(token, contractId, acceptSettlement);
    }

    public ContractProposalResponse declineContractProposal(String contractId) throws URISyntaxException, IOException, InterruptedException 
    {
        return APIConnector.declineContractProposal(token, contractId);
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

    public List<ContractProposalResponse> proposeContractsFromAgreements(OffsetDateTime since, OffsetDateTime before, String partyId) throws URISyntaxException, IOException, InterruptedException
    {
        List<Agreement> agreements = APIConnector.getAllAgreements(token, since, before);
        if (agreements.size() == 0) System.out.println("No new agreements");
        
        List<ContractProposalResponse> responses = new ArrayList<>();
        for (Agreement agreement : agreements){
            if (partyId.equals("*")){
                responses.add(proposeContract(agreement.getTrade()));
            }
            else{
                if (getCounterPartyId(agreement).equals(partyId)){
                    responses.add(proposeContract(agreement.getTrade()));
                }
            }
        }
        return responses;
    }

    public List<ContractProposalResponse> acceptContractProposals(OffsetDateTime since, OffsetDateTime before) throws URISyntaxException, IOException, InterruptedException
    {
        List<Contract> contracts = APIConnector.getAllContracts(token, since, before);
        if (contracts.size() == 0) System.out.println("No new contracts");

        List<ContractProposalResponse> responses = new ArrayList<>();
        for (Contract contract : contracts){
            if (contract.getContractStatus().equals("PROPOSED")){
                responses.add(acceptContractProposal(contract.getContractId()));
            }
        }
        
        return responses;
    }

}
