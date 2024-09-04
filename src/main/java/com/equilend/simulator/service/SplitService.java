package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.party.InternalReference;
import com.equilend.simulator.model.split.LoanSplit;
import com.equilend.simulator.model.split.LoanSplitLot;
import com.equilend.simulator.model.split.LoanSplitLotAppoval;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SplitService {

    public static LoanSplit getSplit(String loanId, String splitId) throws APIException {
        return APIConnector.getSplit(OneSourceToken.getToken(), loanId, splitId);
    }

    public static int approveSplit(Loan loan, LoanSplit split) throws APIException {
        List<LoanSplitLotAppoval> splitLotAppovals = buildSplitLotAppovals(split);
        return APIConnector.approveSplit(OneSourceToken.getToken(), loan.getLoanId(),
            split.getLoanSplitId(), splitLotAppovals);
    }

    private static List<LoanSplitLotAppoval> buildSplitLotAppovals(LoanSplit split) {
        List<LoanSplitLotAppoval> loanSplitLotAppovals = new ArrayList<>();
        for (LoanSplitLot splitLot : split.getSplitLots()) {
            LoanSplitLotAppoval loanSplitLotAppoval = new LoanSplitLotAppoval();
            loanSplitLotAppoval.setInternalRef(splitLot.getInternalRef());
            loanSplitLotAppoval.setLoanId(splitLot.getLoanId());
            loanSplitLotAppovals.add(loanSplitLotAppoval);
        }
        return loanSplitLotAppovals;
    }

    public static int proposeSplit(Loan loan, List<Integer> quantityList) throws APIException {
        List<LoanSplitLot> splitLots = buildSplitLots(loan, quantityList);
        return APIConnector.proposeSplit(OneSourceToken.getToken(), loan.getLoanId(), splitLots);
    }

    private static List<LoanSplitLot> buildSplitLots(Loan loan, List<Integer> quantityList) {
        List<LoanSplitLot> loanSplitLots = new ArrayList<>();
        for(Integer quantity : quantityList) {
            LoanSplitLot loanSplitLot = new LoanSplitLot();
            loanSplitLot.setLoanId(loan.getLoanId());
            loanSplitLot.setQuantity(quantity);
            loanSplitLot.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
            loanSplitLots.add(loanSplitLot);
        }
        return loanSplitLots;
    }
}
