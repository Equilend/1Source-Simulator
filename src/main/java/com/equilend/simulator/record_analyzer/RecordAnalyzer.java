package com.equilend.simulator.record_analyzer;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.configurator.Configurator;
import com.equilend.simulator.configurator.rules.contract_rules.ContractResponsiveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateApproveRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateCancelRule;
import com.equilend.simulator.configurator.rules.rerate_rules.RerateProposeRule;
import com.equilend.simulator.events_processor.event_handler.ContractHandler;
import com.equilend.simulator.events_processor.event_handler.RerateHandler;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.rerate.Rerate;
import com.equilend.simulator.service.ContractService;
import java.util.List;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class RecordAnalyzer {

    private static final Logger logger = LogManager.getLogger(RecordAnalyzer.class.getName());
    private Configurator configurator;
    private String botPartyId;
    private boolean rerateAnalysisMode;
    private boolean contractAnalysisMode;
    private String contractStartDate;

    public RecordAnalyzer(Configurator configurator) {
        this.configurator = configurator;
        this.botPartyId = configurator.getBotPartyId();
        this.rerateAnalysisMode = configurator.getRerateRules().getAnalysisMode();
        this.contractAnalysisMode = configurator.getContractRules().getAnalysisMode();
        this.contractStartDate = configurator.getContractRules().getAnalysisStartDate();
    }

    private Contract getContractById(String contractId) {
        Contract contract = null;
        try {
            contract = APIConnector.getContractById(OneSourceToken.getToken(), contractId);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve contract {}", contractId);
        }
        return contract;
    }

    private List<Contract> getContracts(String status) {
        List<Contract> contracts = null;
        try {
            contracts = APIConnector.getAllContracts(OneSourceToken.getToken(), status, contractStartDate);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve approved contracts");
        }
        return contracts;
    }

    private List<Rerate> getOpenReratesOnContract(String contractId) {
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllReratesOnContract(OneSourceToken.getToken(), contractId);
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve open rerates on contract {}", contractId);
        }

        return rerates;
    }

    private List<Rerate> getAllRerates() {
        List<Rerate> rerates = null;
        try {
            rerates = APIConnector.getAllRerates(OneSourceToken.getToken());
        } catch (APIException e) {
            logger.error("Analyzer unable to retrieve approved rerates");
        }
        return rerates;
    }

    public void run() {
        if (rerateAnalysisMode) {
            // get all open rerates
            List<Rerate> rerates = getAllRerates();
            if (rerates != null) {
                for (Rerate rerate : rerates) {
                    Contract contract = getContractById(rerate.getContractId());
                    if (contract == null) {
                        continue;
                    }

                    if (ContractService.getTransactingPartyById(contract, botPartyId).get().getPartyRole()
                        == PartyRole.BORROWER) {
                        // if bot is lender => initiator => cancel/ignore rules
                        RerateCancelRule rule = configurator.getRerateRules()
                            .getCancelRule(rerate, contract, botPartyId);
                        if (rule == null || !rule.shouldCancel()) {
                            continue;
                        }

                        RerateHandler.cancelRerateProposal(rerate.getContractId(), rerate.getRerateId(), 0L, 0.0);
                    } else {
                        // if bot is borrower => recipient => approve/reject rules
                        RerateApproveRule rule = configurator.getRerateRules()
                            .getApproveRule(rerate, contract, botPartyId);
                        if (rule == null) {
                            continue;
                        }
                        if (rule.shouldApprove()) {
                            RerateHandler.approveRerateProposal(rerate.getContractId(), rerate.getRerateId(), 0L, 0.0);
                        } else {
                            RerateHandler.declineRerateProposal(rerate.getContractId(), rerate.getRerateId(), 0L, 0.0);
                        }
                    }
                }
            }
            // Get approved contracts to consider proposing rerates
            List<Contract> contracts = getContracts("APPROVED");
            if (contracts != null) {
                for (Contract contract : contracts) {
                    List<Rerate> reratesOnContract = getOpenReratesOnContract(contract.getContractId());
                    if (reratesOnContract.size() > 0) {
                        continue;
                    }

                    RerateProposeRule rule = configurator.getRerateRules()
                        .getProposeRule(contract, configurator.getBotPartyId());
                    if (rule == null || !rule.shouldPropose()) {
                        continue;
                    }
                    RerateHandler.postRerateProposal(contract.getContractId(), contract.getTrade().getRate(),
                        rule.getDelta(), 0L, 0.0);
                }
            }
        }
        if (contractAnalysisMode) {
            //get all proposed contracts to consider accepting/declining/cancelling
            List<Contract> contracts = getContracts("PROPOSED");
            if (contracts != null) {
                for (Contract contract : contracts) {
                    //determine whether will consider as initiator or as recipient
                    if (ContractHandler.didBotInitiate(botPartyId, contract)) {
                        Double delay = configurator.getContractRules().shouldIgnoreTrade(contract, botPartyId);
                        if (delay == -1.0) {
                            continue;
                        }
                        ContractHandler.cancelContractProposal(contract.getContractId(), 0L, 0.0);
                    } else {
                        ContractResponsiveRule rule = configurator.getContractRules()
                            .getApproveOrRejectApplicableRule(contract, botPartyId);
                        if (rule == null) {
                            continue;
                        }
                        if (rule.isShouldApprove()) {
                            PartyRole partyRole = ContractService.getTransactingPartyById(contract, botPartyId).get()
                                .getPartyRole();
                            ContractHandler.acceptContractProposal(contract.getContractId(),
                                partyRole, 0L, 0.0);
                        } else {
                            ContractHandler.declineContractProposal(contract.getContractId(), 0L, 0.0);
                        }
                    }
                }
            }
        }
    }
}