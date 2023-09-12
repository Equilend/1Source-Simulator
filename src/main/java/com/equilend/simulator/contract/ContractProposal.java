package com.equilend.simulator.contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.equilend.simulator.settlement.Settlement;
import com.equilend.simulator.settlement.instruction.Instruction;
import com.equilend.simulator.settlement.instruction.LocalMarketFields;
import com.equilend.simulator.trade.Currency;
import com.equilend.simulator.trade.SettlementType;
import com.equilend.simulator.trade.Trade;
import com.equilend.simulator.trade.collateral.Collateral;
import com.equilend.simulator.trade.collateral.CollateralType;
import com.equilend.simulator.trade.collateral.RoundingMode;
import com.equilend.simulator.trade.execution_venue.ExecutionVenue;
import com.equilend.simulator.trade.execution_venue.Platform;
import com.equilend.simulator.trade.execution_venue.VenueType;
import com.equilend.simulator.trade.execution_venue.venue_party.VenueParty;
import com.equilend.simulator.trade.instrument.Instrument;
import com.equilend.simulator.trade.rate.BenchmarkCd;
import com.equilend.simulator.trade.rate.FixedRate;
import com.equilend.simulator.trade.rate.FloatingRate;
import com.equilend.simulator.trade.rate.Rate;
import com.equilend.simulator.trade.rate.RebateRate;
import com.equilend.simulator.trade.transacting_party.Party;
import com.equilend.simulator.trade.transacting_party.PartyRole;
import com.equilend.simulator.trade.transacting_party.TransactingParty;

public class ContractProposal {

    private Trade trade;
    private List<Settlement> settlement;

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

    public static Trade createTrade() {
        Platform platform = new Platform("X", "Phone Brokered", "EXTERNAL", "0");
        List<VenueParty> venueParties = new ArrayList<>();
        VenueParty lenderVenueParty = new VenueParty(PartyRole.LENDER);
        venueParties.add(lenderVenueParty);
        VenueParty borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        venueParties.add(borrowerVenueParty);
        ExecutionVenue executionVenue = new ExecutionVenue(VenueType.OFFPLATFORM, platform, venueParties);
        
        Instrument instrument = new Instrument("MSFT", "BBG001S5TD05", "MICROSOFT CORP COM");

        FloatingRate floating = new FloatingRate(BenchmarkCd.OBFR, null, Float.valueOf(".15"), Float.valueOf(".15"), false, null, "2023-09-12", "18:00:00");
        RebateRate rebate = new RebateRate(floating);
        Rate rate = new Rate(rebate);
        
        Long quantity = Long.valueOf(25025);
        
        Currency billingCurrency = Currency.USD;
        
        BigDecimal dividendRatePct = BigDecimal.valueOf(100);
        
        LocalDate today = LocalDate.now();
        String todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
        String tradeDate = todayStr;
        
        LocalDate tPlus2 = today.plusDays(2);
        String tPlus2Str = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(tPlus2);
        String settlementDate = tPlus2Str;
        
        SettlementType settlementType = SettlementType.DVP;
        
        Collateral collateral = new Collateral(BigDecimal.valueOf(8758750), BigDecimal.valueOf(8933925), Currency.USD,
        CollateralType.CASH, 10, RoundingMode.ALWAYSUP, BigDecimal.valueOf(102));
        
        List<TransactingParty> transactingParties = new ArrayList<>();
        TransactingParty lenderTransactingParty = new TransactingParty();
        lenderTransactingParty.setPartyRole(PartyRole.LENDER);
        Party lenderParty = new Party("TLEN-US", "Test Lender US", "KTB500SKZSDI75VSFU40");
        lenderTransactingParty.setParty(lenderParty);
        transactingParties.add(lenderTransactingParty);
        TransactingParty borrowingTransactingParty = new TransactingParty();
        borrowingTransactingParty.setPartyRole(PartyRole.BORROWER);
        Party borrowerParty = new Party("TBORR-US", "Test Borrower US", "KTB500SKZSDI75VSFU40");
        borrowingTransactingParty.setParty(borrowerParty);
        transactingParties.add(borrowingTransactingParty);

        return new Trade(executionVenue, instrument, rate, quantity, billingCurrency, dividendRatePct, tradeDate, settlementDate, settlementType, collateral, transactingParties);
    }

    public static Settlement createSettlement(PartyRole role) {
        List<LocalMarketFields> localMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields localMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        localMarketFieldsList.add(localMarketFields);
        Instruction instruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", localMarketFieldsList);
        return new Settlement(role, instruction);          
    }    

    public static ContractProposal createContractProposal() {
        Trade trade = createTrade();

        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(PartyRole.LENDER));

        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;
    }

    public static ContractProposal createContractProposal(Trade trade) {
        trade.setDividendRatePct(BigDecimal.valueOf(100));

        trade.getCollateral().setRoundingRule(10);
        trade.getCollateral().setRoundingMode(RoundingMode.ALWAYSUP);
        trade.getCollateral().setMargin(BigDecimal.valueOf(102));
        
        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(PartyRole.LENDER));
        
        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;        
    }    

}