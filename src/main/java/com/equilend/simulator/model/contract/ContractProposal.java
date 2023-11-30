package com.equilend.simulator.model.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.api.APIException;
import com.equilend.simulator.api.DatalendAPIConnector;
import com.equilend.simulator.auth.DatalendToken;
import com.equilend.simulator.model.settlement.Settlement;
import com.equilend.simulator.model.settlement.instruction.Instruction;
import com.equilend.simulator.model.settlement.instruction.LocalMarketFields;
import com.equilend.simulator.model.trade.Currency;
import com.equilend.simulator.model.trade.SettlementType;
import com.equilend.simulator.model.trade.Trade;
import com.equilend.simulator.model.trade.collateral.Collateral;
import com.equilend.simulator.model.trade.collateral.CollateralType;
import com.equilend.simulator.model.trade.collateral.RoundingMode;
import com.equilend.simulator.model.trade.execution_venue.ExecutionVenue;
import com.equilend.simulator.model.trade.execution_venue.Platform;
import com.equilend.simulator.model.trade.execution_venue.VenueType;
import com.equilend.simulator.model.trade.execution_venue.venue_party.VenueParty;
import com.equilend.simulator.model.trade.instrument.Instrument;
import com.equilend.simulator.model.trade.rate.BenchmarkCd;
import com.equilend.simulator.model.trade.rate.FloatingRate;
import com.equilend.simulator.model.trade.rate.Rate;
import com.equilend.simulator.model.trade.rate.RebateRate;
import com.equilend.simulator.model.trade.transacting_party.Party;
import com.equilend.simulator.model.trade.transacting_party.PartyRole;
import com.equilend.simulator.model.trade.transacting_party.TransactingParty;

public class ContractProposal {

    private Trade trade;
    private List<Settlement> settlement;
    private static final Logger logger = LogManager.getLogger();
    
    public ContractProposal(Trade trade, List<Settlement> settlement) {
        this.trade = trade;
        this.settlement = settlement;
    }

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }

    public List<Settlement> getSettlement() {
        return settlement;
    }

    public void setSettlement(List<Settlement> settlement) {
        this.settlement = settlement;
    }

    public static Trade createTrade(PartyRole partyRole, Party party, Party counterparty, Instrument security, long desiredQuantity) {
        Platform platform = new Platform("X", "Phone Brokered", "EXTERNAL", "0");
        List<VenueParty> venueParties = new ArrayList<>();
        VenueParty lenderVenueParty = new VenueParty(PartyRole.LENDER);
        venueParties.add(lenderVenueParty);
        VenueParty borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        venueParties.add(borrowerVenueParty);
        ExecutionVenue executionVenue = new ExecutionVenue(VenueType.OFFPLATFORM, platform, venueParties);
        
        Instrument instrument = security;

        LocalDate today = LocalDate.now();
        String todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
        FloatingRate floating = new FloatingRate(BenchmarkCd.OBFR, null, Double.valueOf(".15"), Double.valueOf(".15"), false, null, todayStr, "18:00:00");
        RebateRate rebate = new RebateRate(floating);
        Rate rate = new Rate(rebate);
        
        Long quantity = desiredQuantity;
        
        Currency billingCurrency = Currency.USD;
        
        BigDecimal dividendRatePct = BigDecimal.valueOf(100);
        
        String tradeDate = todayStr;
        
        LocalDate tPlus2 = today.plusDays(2);
        String tPlus2Str = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(tPlus2);
        String settlementDate = tPlus2Str;
        
        SettlementType settlementType = SettlementType.DVP;
        
        Collateral collateral;
        if (partyRole == PartyRole.LENDER){
            collateral = new Collateral(BigDecimal.valueOf(8758750), BigDecimal.valueOf(8933925), Currency.USD, CollateralType.CASH, 10, RoundingMode.ALWAYSUP, BigDecimal.valueOf(102));
        }
        else{
            collateral = new Collateral(BigDecimal.valueOf(8758750), BigDecimal.valueOf(8933925), Currency.USD, CollateralType.CASH, BigDecimal.valueOf(102));
        }
        
        List<TransactingParty> transactingParties = new ArrayList<>();
        TransactingParty botTransactingParty = new TransactingParty();
        botTransactingParty.setPartyRole(partyRole);
        botTransactingParty.setParty(party);
        transactingParties.add(botTransactingParty);
        TransactingParty counterTransactingParty = new TransactingParty();
        PartyRole counterpartyRole = partyRole == PartyRole.LENDER ? PartyRole.BORROWER : PartyRole.LENDER;
        counterTransactingParty.setPartyRole(counterpartyRole);
        counterTransactingParty.setParty(counterparty);
        transactingParties.add(counterTransactingParty);

        return new Trade(executionVenue, instrument, rate, quantity, billingCurrency, dividendRatePct, tradeDate, settlementDate, settlementType, collateral, transactingParties);
    }

    public static Settlement createSettlement(PartyRole role) {
        List<LocalMarketFields> localMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields localMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        localMarketFieldsList.add(localMarketFields);
        Instruction instruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", localMarketFieldsList);
        return new Settlement(role, instruction);          
    }    

    private static void updateTradePrice(Trade trade, String idType){
        String idValue = null;
        switch (idType.toUpperCase()){
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
        if (idValue == null) return;
        double price = 250;
        try {
            price = DatalendAPIConnector.getSecurityPrice(DatalendToken.getToken(), idType, idValue);
            // logger.debug("{} {} has price {}", idType, idValue, price);
        } catch (APIException e) {
            logger.debug("Unable to get current price for security w {} {}, default to $250", idType, idValue); 
        }
        double contractValue = price * trade.getQuantity();
        trade.getCollateral().setContractValue(BigDecimal.valueOf(contractValue));
        double collateralValue = contractValue * trade.getCollateral().getMargin().doubleValue() / 100.0;
        trade.getCollateral().setCollateralValue(BigDecimal.valueOf(collateralValue));
    }

    public static ContractProposal createContractProposal(PartyRole partyRole, Party party, Party counterparty, Instrument security, long desiredQuantity, String idType) {
        Trade trade = createTrade(partyRole, party, counterparty, security, desiredQuantity);    
        updateTradePrice(trade, idType);

        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(partyRole));

        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;
    }


    public static ContractProposal createContractProposal(Trade trade, PartyRole partyRole) {
        trade.setDividendRatePct(BigDecimal.valueOf(100));

        if (partyRole == PartyRole.LENDER) trade.getCollateral().setRoundingRule(10);
        if (partyRole == PartyRole.LENDER) trade.getCollateral().setRoundingMode(RoundingMode.ALWAYSUP);
        trade.getCollateral().setMargin(BigDecimal.valueOf(102));
        updateTradePrice(trade, "S");

        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(partyRole));
        
        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;        
    }    

}