package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.party.InternalReference;
import com.equilend.simulator.model.split.ContractSplit;
import com.equilend.simulator.model.split.ContractSplitLot;
import com.equilend.simulator.model.split.ContractSplitLotAppoval;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SplitService {

    public static ContractSplit getSplit(String contractId, String splitId) throws APIException {
        return APIConnector.getSplit(OneSourceToken.getToken(), contractId, splitId);
    }

    public static int approveSplit(Contract contract, ContractSplit split) throws APIException {
        List<ContractSplitLotAppoval> splitLotAppovals = buildSplitLotAppovals(split);
        return APIConnector.approveSplit(OneSourceToken.getToken(), contract.getContractId(),
            split.getContractSplitId(), splitLotAppovals);
    }

    private static List<ContractSplitLotAppoval> buildSplitLotAppovals(ContractSplit split) {
        List<ContractSplitLotAppoval> contractSplitLotAppovals = new ArrayList<>();
        for (ContractSplitLot splitLot : split.getSplitLots()) {
            ContractSplitLotAppoval contractSplitLotAppoval = new ContractSplitLotAppoval();
            contractSplitLotAppoval.setInternalRef(splitLot.getInternalRef());
            contractSplitLotAppoval.setContractId(splitLot.getContractId());
            contractSplitLotAppovals.add(contractSplitLotAppoval);
        }
        return contractSplitLotAppovals;
    }

    public static int proposeSplit(Contract contract, List<Integer> quantityList) throws APIException {
        List<ContractSplitLot> splitLots = buildSplitLots(contract, quantityList);
        return APIConnector.proposeSplit(OneSourceToken.getToken(), contract.getContractId(), splitLots);
    }

    private static List<ContractSplitLot> buildSplitLots(Contract contract, List<Integer> quantityList) {
        List<ContractSplitLot> contractSplitLots = new ArrayList<>();
        for(Integer quantity : quantityList) {
            ContractSplitLot contractSplitLot = new ContractSplitLot();
            contractSplitLot.setContractId(contract.getContractId());
            contractSplitLot.setQuantity(quantity);
            contractSplitLot.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
            contractSplitLots.add(contractSplitLot);
        }
        return contractSplitLots;
    }
}
