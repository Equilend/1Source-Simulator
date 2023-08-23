package com.equilend.simulator.Contract;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import com.equilend.simulator.Settlement.Settlement;
import com.equilend.simulator.Settlement.Instruction.Instruction;
import com.equilend.simulator.Settlement.Instruction.LocalMarketFields;
import com.equilend.simulator.Trade.Currency;
import com.equilend.simulator.Trade.SettlementType;
import com.equilend.simulator.Trade.Trade;
import com.equilend.simulator.Trade.Collateral.Collateral;
import com.equilend.simulator.Trade.Collateral.CollateralType;
import com.equilend.simulator.Trade.Collateral.RoundingMode;
import com.equilend.simulator.Trade.ExecutionVenue.ExecutionVenue;
import com.equilend.simulator.Trade.ExecutionVenue.Platform;
import com.equilend.simulator.Trade.ExecutionVenue.VenueType;
import com.equilend.simulator.Trade.ExecutionVenue.VenueParty.VenueParty;
import com.equilend.simulator.Trade.Instrument.Instrument;
import com.equilend.simulator.Trade.Rate.Rate;
import com.equilend.simulator.Trade.TransactingParty.Party;
import com.equilend.simulator.Trade.TransactingParty.PartyRole;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;

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

    public static Trade createTrade(){
        Platform platform = new Platform("X", "Phone Brokered", "EXTERNAL", "0");
        List<VenueParty> venueParties = new ArrayList<>();
        VenueParty lenderVenueParty = new VenueParty(PartyRole.LENDER);
        venueParties.add(lenderVenueParty);
        VenueParty borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        venueParties.add(borrowerVenueParty);
        ExecutionVenue executionVenue = new ExecutionVenue(VenueType.OFFPLATFORM, platform, venueParties);
        Instrument instrument = new Instrument("MSFT", "594918104", "US5949181045", "2588173", "BBG001S5TD05", "MICROSOFT CORP COM" );
        Rate rate = new Rate(BigDecimal.valueOf(.125));
        Long quantity = Long.valueOf(25025);
        Currency billingCurrency = Currency.USD;
        BigDecimal dividendRatePct = BigDecimal.valueOf(100);
        LocalDate today = LocalDate.now();
        String todayStr = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(today);
        LocalDate tPlus2 = today.plusDays(2);
        String tPlus2Str = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.ENGLISH).format(tPlus2);
        String tradeDate = todayStr;
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

    public static Settlement createSettlement(PartyRole role){
        List<LocalMarketFields> localMarketFieldsList = new ArrayList<LocalMarketFields>();
        LocalMarketFields localMarketFields = new LocalMarketFields("DTCYUS00", "00001");
        localMarketFieldsList.add(localMarketFields);
        Instruction instruction = new Instruction("XXXXXXXX", "YYYYYYYY", "ZZZ Clearing", "2468999", localMarketFieldsList);
        return new Settlement(role, instruction);          
    }    

    public static ContractProposal createContractProposal(){
        Trade trade = createTrade();

        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(PartyRole.LENDER));

        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;
    }

    public static ContractProposal createContractProposal(Trade trade){
        trade.getCollateral().setRoundingRule(10);
        trade.getCollateral().setRoundingMode(RoundingMode.ALWAYSUP);
        trade.getCollateral().setMargin(BigDecimal.valueOf(102));
        
        List<Settlement> settlements = new ArrayList<Settlement>();
        settlements.add(createSettlement(PartyRole.LENDER));
        
        ContractProposal contractProposal = new ContractProposal(trade, settlements);
        return contractProposal;        
    }    

}
