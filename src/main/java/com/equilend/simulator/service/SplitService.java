package com.equilend.simulator.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.os.client.model.InternalReference;
import com.os.client.model.Loan;
import com.os.client.model.LoanSplit;
import com.os.client.model.LoanSplitLot;
import com.os.client.model.LoanSplitLotAppoval;

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
            loanSplitLotAppoval.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
            loanSplitLotAppoval.setLoanId(splitLot.getLoanId());
            loanSplitLotAppovals.add(loanSplitLotAppoval);
        }
        return loanSplitLotAppovals;
    }

    public static int proposeSplit(Loan loan, List<Integer> splitLotQuantity) throws APIException {
        List<LoanSplitLot> splitLots = buildSplitLots(loan, splitLotQuantity);
        return APIConnector.proposeSplit(OneSourceToken.getToken(), loan.getLoanId(), splitLots);
    }

    private static List<LoanSplitLot> buildSplitLots(Loan loan, List<Integer> quantityList) {
        List<LoanSplitLot> loanSplitLots = new ArrayList<>();
        for (Integer quantity : quantityList) {
            LoanSplitLot loanSplitLot = new LoanSplitLot();
            loanSplitLot.setLoanId(loan.getLoanId());
            loanSplitLot.setQuantity(quantity);
            loanSplitLot.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
            loanSplitLots.add(loanSplitLot);
        }
        return loanSplitLots;
    }
}
