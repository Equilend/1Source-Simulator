package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.buyin.BuyinComplete;
import com.equilend.simulator.model.buyin.BuyinCompleteRequest;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.instrument.price.Price;
import java.time.LocalDate;

public class BuyinService {

    public static BuyinComplete getBuyinById(String buyinId) throws APIException {
        return APIConnector.getBuyinById(OneSourceToken.getToken(), buyinId);
    }

    public static int acceptBuyin(BuyinComplete buyin) throws APIException {
        return APIConnector.acceptBuyin(OneSourceToken.getToken(), buyin.getContractId(), buyin.getBuyinCompleteId());
    }

    public static int proposeBuyin(Contract contract, Integer quantity, Double priceValue) throws APIException {
        BuyinCompleteRequest buyinCompleteRequest = buildBuyinCompleteRequest(contract, quantity, priceValue);
        return APIConnector.proposeBuyin(OneSourceToken.getToken(), contract.getContractId(), buyinCompleteRequest);
    }

    private static BuyinCompleteRequest buildBuyinCompleteRequest(Contract contract, Integer quantity,
        Double priceValue) {
        BuyinCompleteRequest buyinCompleteRequest = new BuyinCompleteRequest();
        buyinCompleteRequest.setQuantity(quantity);
        buyinCompleteRequest.setBuyinDate(LocalDate.now());
        Price price = new Price();
        price.setValue(priceValue);
        price.setCurrency(contract.getTrade().getInstrument().getPrice().getCurrency());
        price.setUnit(contract.getTrade().getInstrument().getPrice().getUnit());
        price.setValueDate(LocalDate.now());
        buyinCompleteRequest.setPrice(price);
        return buyinCompleteRequest;
    }
}
