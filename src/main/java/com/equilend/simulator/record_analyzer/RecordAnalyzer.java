package com.equilend.simulator.record_analyzer;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.FedAPIConnector;
import com.equilend.simulator.api.FedAPIException;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.model.rerate.RerateProposal;
import com.equilend.simulator.model.trade.rate.FixedRate;
import com.equilend.simulator.model.trade.rate.Rate;
import com.equilend.simulator.model.trade.rate.RebateRate;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.token.OneSourceToken;

public class RecordAnalyzer {

    private Configurator configurator;    
    private String botPartyId;
    private boolean rerateAnalysisMode;
    private static final Logger logger = LogManager.getLogger();

    public RecordAnalyzer(Configurator configurator){
        this.configurator = configurator;
        this.botPartyId = configurator.getGeneralRules().getBotPartyId();
        this.rerateAnalysisMode = configurator.getRerateRules().getAnalysisMode();
    }

    public List<Contract> getApprovedContracts(){
        List<Contract> contracts = null;
        try {
            contracts = APIConnector.getAllContracts(OneSourceToken.getToken(), "APPROVED");
        }catch (APIException e){
            logger.error("Error retrieving approved contracts");
        }
        return contracts;
    }

    public Contract getContractById(String contractId) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(OneSourceToken.getToken(), contractId);
        } catch (APIException e){
            logger.error("Error retrieving contract {}", contractId);
        }
        return contract;
    }

    public List<Rerate> getOpenReratesOnContract(String contractId){
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllReratesOnContract(OneSourceToken.getToken(), contractId);
        } catch(APIException e){
            logger.error("Error retrieving open rerates on contract {}", contractId);
        }
        
        return rerates;
    }

    public List<Rerate> getAllRerates(){
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllRerates(OneSourceToken.getToken());
        }catch (APIException e){
            logger.error("Error retrieving approved rerates");
        }
        return rerates;        
    }

    public void postRerateProposal(String contractId, Rate rate, Double delta){
        FixedRate fee = rate.getFee();
        RebateRate rebate = rate.getRebate();

        String today = APIConnector.getCurrentTime().toString().substring(0, 10);
        if (fee != null){
            fee.setBaseRate(Math.max(fee.getBaseRate() + delta, 0.01));
            fee.setEffectiveDate(today);
        }
        else if (rebate != null){
            if (rebate.getFixed() != null) {
                rebate.getFixed().setBaseRate(Math.max(rebate.getFixed().getBaseRate() + delta, 0.01));
                rebate.getFixed().setEffectiveDate(today);
            }
            else if (rebate.getFloating() != null){
                String benchmarkStr = rebate.getFloating().getBenchmark().name();
                Double benchmarkRate = 5.0;
                try {
                    benchmarkRate = FedAPIConnector.getRefRate(benchmarkStr).getPercentRate();
                } catch (FedAPIException e) {
                    logger.debug("Analyzer unable to get benchmark rate, default to 5%");
                }

                rebate.getFloating().setBaseRate(benchmarkRate);
                rebate.getFloating().setSpread(Math.max(rebate.getFloating().getSpread() + delta, 0.01));
                rebate.getFloating().setEffectiveDate(today);
            }
        }

        try {
            APIConnector.postRerateProposal(OneSourceToken.getToken(), contractId, new RerateProposal(rate));
        } catch (APIException e) {
            logger.debug("Analyzer unable to propose rerate");
        }

    }

    public void cancelRerateProposal(String contractId, String rerateId){
        try {
            APIConnector.cancelRerateProposal(OneSourceToken.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Analyzer unable to cancel rerate");
        }
    }

    public void approveRerateProposal(String contractId, String rerateId){
        try {
            APIConnector.approveRerateProposal(OneSourceToken.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Analyzer unable to approve rerate");
        }
    }    

    public void declineRerateProposal(String contractId, String rerateId){
        try {
            APIConnector.declineRerateProposal(OneSourceToken.getToken(), contractId, rerateId);
        } catch (APIException e) {
            logger.debug("Analyzer unable to decline rerate");
        }        
    }        

    public void run(){
        if (rerateAnalysisMode){
            // get all open rerates
            List<Rerate> rerates = getAllRerates();
            if (rerates != null){
                for (Rerate rerate : rerates){
                    if (rerate.getPartyRole(botPartyId) == PartyRole.LENDER){
                        // if bot is lender => initiator => cancel/ignore rules
                        Contract contract = getContractById(rerate.getLoanId());
                        if (contract == null) continue;
                        RerateCancelRule rule = configurator.getRerateRules().getCancelRule(rerate, contract, botPartyId);
                        if (rule == null || !rule.shouldCancel()) continue;
    
                        cancelRerateProposal(rerate.getLoanId(), rerate.getRerateId());
                    }
                    else{
                        // if bot is borrower => recipient => approve/reject rules
                        Contract contract = getContractById(rerate.getLoanId());
                        if (contract == null) continue;
                        RerateApproveRule rule = configurator.getRerateRules().getApproveRule(rerate, contract, botPartyId);
                        if (rule == null) continue;
                        if (rule.shouldApprove()){
                            approveRerateProposal(rerate.getLoanId(), rerate.getRerateId());
                        }
                        else{
                            declineRerateProposal(rerate.getLoanId(), rerate.getRerateId());
                        }
                    }
                }
            }
            // Get approved contracts to consider proposing rerates
            List<Contract> contracts = getApprovedContracts();
            if (contracts != null) {
                for (Contract contract : contracts){
                    List<Rerate> reratesOnContract = getOpenReratesOnContract(contract.getContractId());
                    if (reratesOnContract.size() > 0) continue;
        
                    RerateProposeRule rule = configurator.getRerateRules().getProposeRule(contract, configurator.getGeneralRules().getBotPartyId());
                    if (rule == null || !rule.shouldPropose()) continue;
                    postRerateProposal(contract.getContractId(), contract.getTrade().getRate(), rule.getDelta());
                }
            }
        }

    }

}
