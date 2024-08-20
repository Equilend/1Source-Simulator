package com.equilend.simulator.service;

import com.equilend.simulator.api.APIConnector;
import com.equilend.simulator.api.APIException;
import com.equilend.simulator.auth.OneSourceToken;
import com.equilend.simulator.model.buyin.BuyinComplete;

public class BuyinService {

    public static BuyinComplete getBuyinById(String buyinId) throws APIException {
        return APIConnector.getBuyinById(OneSourceToken.getToken(), buyinId);
    }

    public static int acceptBuyin(BuyinComplete buyin) throws APIException {
        return APIConnector.acceptBuyin(OneSourceToken.getToken(), buyin.getContractId(), buyin.getBuyinCompleteId());
    }
}
