package com.equilend.simulator;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.equilend.simulator.Agreement.Agreement;
import com.equilend.simulator.Settlement.AcceptSettlement;
import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Trade.Trade;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;

public class User 
{
    private Map<String, String> loginInfo;
    private String configFileName;
    Token token;
    private PartyRole role;
    private PartyRole counterRole;

    public User(String fn, PartyRole r) throws TokenException, FileNotFoundException
    {
        this.configFileName = fn;
        this.loginInfo = readFormData(configFileName);
        refreshToken();
        this.role = r;
        this.counterRole = (r == PartyRole.LENDER) ? PartyRole.BORROWER : PartyRole.LENDER;
    }
    
    void refreshToken() throws TokenException
    {
        try {
            this.token = APIConnector.getBearerToken(loginInfo);
        } catch (URISyntaxException | IOException | InterruptedException e){
            throw new TokenException("Error getting bearer token", e);
        }
    }

    public Map<String, String> readFormData (String filename) throws FileNotFoundException
    {
        loginInfo = new HashMap<>();
        try (Scanner scanner = new Scanner(new File(filename))){
            while (scanner.hasNextLine()){
                String line = scanner.nextLine();
                String[] keyValuePair = line.split("=");
                loginInfo.put(keyValuePair[0], keyValuePair[1]);
            }
        } catch (FileNotFoundException e){
            throw new FileNotFoundException(filename + " not found");
        }
        
        return loginInfo;
    }

    public ContractProposalResponse proposeContract(Trade trade) throws URISyntaxException, IOException, InterruptedException
    {
        ContractProposal contractProposal = ContractProposal.createContractProposal(trade);

        ContractProposalResponse response = APIConnector.postContractProposal(token, contractProposal);
        return response;
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

    public Contract getContractById(String contractId) throws URISyntaxException, IOException, InterruptedException
    {
        return APIConnector.getContractById(token, contractId);
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

}
