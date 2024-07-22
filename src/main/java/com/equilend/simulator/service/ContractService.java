package com.equilend.simulator.service;

import static com.equilend.simulator.model.collateral.RoundingMode.ALWAYSUP;
import static com.equilend.simulator.service.TradeService.createTrade;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.model.contract.Contract;
import com.equilend.simulator.model.contract.ContractProposal;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.settlement.PartySettlementInstruction;
import com.equilend.simulator.model.trade.TradeAgreement;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.model.venue.VenueTradeAgreement;
import java.util.List;
import java.util.Optional;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ContractService {

    private static final Logger logger = LogManager.getLogger();

    public static Optional<TransactingParty> getTransactingPartyById(Contract contract, String botPartyId) {
        return TradeService.getTransactingPartyById(contract.getTrade(), botPartyId);
    }

    public static ContractProposal createContractProposal(TradeAgreement trade, PartyRole partyRole) {
        trade.setDividendRatePct(Double.valueOf(100));

        if (partyRole == PartyRole.LENDER) {
            trade.getCollateral().setRoundingRule(10);
        }
        if (partyRole == PartyRole.LENDER) {
            trade.getCollateral().setRoundingMode(ALWAYSUP);
        }
        trade.getCollateral().setMargin(Double.valueOf(102));
        updateTradePrice(trade, "S");

        PartySettlementInstruction partySettlementInstruction = SettlementService.createPartySettlementInstruction(
            partyRole);

        ContractProposal contractProposal = new ContractProposal().trade(trade)
            .settlement(List.of(partySettlementInstruction));
        return contractProposal;
    }

    public static ContractProposal createContractProposal(PartyRole partyRole, Party party, Party counterparty,
        Instrument security, Integer desiredQuantity, String idType) {
        TradeAgreement trade = createTrade(partyRole, party, counterparty, security, desiredQuantity);
        updateTradePrice(trade, idType);

        PartySettlementInstruction partySettlementInstruction = SettlementService.createPartySettlementInstruction(
            partyRole);

        ContractProposal contractProposal = new ContractProposal().trade(trade)
            .settlement(List.of(partySettlementInstruction));
        return contractProposal;
    }

    private static void updateTradePrice(TradeAgreement trade, String idType) {
        String idValue = null;
        switch (idType.toUpperCase()) {
            case "S":
                idType = "sedol";
                idValue = trade.getInstrument().getSedol();
                break;
            case "I":
                idType = "isin";
                idValue = trade.getInstrument().getIsin();
                break;
            case "C":
                idType = "cusip";
                idValue = trade.getInstrument().getCusip();
                break;
            case "F":
                idType = "figi";
                idValue = null;
                break;
            default:
                idType = "ticker";
                idValue = trade.getInstrument().getTicker();
                break;
        }
        if (idValue == null) {
            return;
        }
        double price = 250;
        try {
            price = DatalendAPIConnector.getSecurityPrice(DatalendToken.getToken(), idType, idValue);
            // logger.debug("{} {} has price {}", idType, idValue, price);
        } catch (APIException e) {
            logger.debug("Unable to get current price for security w {} {}, default to $250", idType, idValue);
        }
        double contractValue = price * trade.getQuantity();
        trade.getCollateral().setContractValue(Double.valueOf(contractValue));
        double collateralValue = contractValue * trade.getCollateral().getMargin().doubleValue() / 100.0;
        trade.getCollateral().setCollateralValue(Double.valueOf(collateralValue));
    }

}
