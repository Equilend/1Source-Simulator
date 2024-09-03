package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.buyin.BuyinCompleteRequest;
import com.equilend.simulator.model.loan.Loan;
import com.equilend.simulator.model.instrument.price.Price;
import java.time.LocalDate;

public class BuyinService {

    public static BuyinComplete getBuyinById(String buyinId) throws APIException {
        return APIConnector.getBuyinById(OneSourceToken.getToken(), buyinId);
    }

    public static int acceptBuyin(BuyinComplete buyin) throws APIException {
        return APIConnector.acceptBuyin(OneSourceToken.getToken(), buyin.getLoanId(), buyin.getBuyinCompleteId());
    }

    public static int proposeBuyin(Loan loan, Integer quantity, Double priceValue) throws APIException {
        BuyinCompleteRequest buyinCompleteRequest = buildBuyinCompleteRequest(loan, quantity, priceValue);
        return APIConnector.proposeBuyin(OneSourceToken.getToken(), loan.getLoanId(), buyinCompleteRequest);
    }

    private static BuyinCompleteRequest buildBuyinCompleteRequest(Loan loan, Integer quantity,
        Double priceValue) {
        BuyinCompleteRequest buyinCompleteRequest = new BuyinCompleteRequest();
        buyinCompleteRequest.setQuantity(quantity);
        buyinCompleteRequest.setBuyinDate(LocalDate.now());
        Price price = new Price();
        price.setValue(priceValue);
        price.setCurrency(loan.getTrade().getInstrument().getPrice().getCurrency());
        price.setUnit(loan.getTrade().getInstrument().getPrice().getUnit());
        price.setValueDate(LocalDate.now());
        buyinCompleteRequest.setPrice(price);
        return buyinCompleteRequest;
    }
}
