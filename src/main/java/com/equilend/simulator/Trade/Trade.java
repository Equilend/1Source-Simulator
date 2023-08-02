package com.equilend.simulator.Trade;

import java.math.BigDecimal;
import java.util.List;

import com.equilend.simulator.Trade.Collateral.Collateral;
import com.equilend.simulator.Trade.ExecutionVenue.ExecutionVenue;
import com.equilend.simulator.Trade.Instrument.Instrument;
import com.equilend.simulator.Trade.Rate.Rate;
import com.equilend.simulator.Trade.TransactingParty.TransactingParty;

public class Trade {
    private ExecutionVenue executionVenue;
    private Instrument instrument;
    private Rate rate;
    private Long quantity;
    private Currency billingCurrency;
    private BigDecimal dividendRatePct;
    private String tradeDate;
    private String settlementDate;
    private SettlementType settlementType;
    private Collateral collateral;
    private List<TransactingParty> transactingParties;

    
    public Trade(ExecutionVenue executionVenue, Instrument instrument, Rate rate, Long quantity,
            Currency billingCurrency, BigDecimal dividendRatePct, String tradeDate, String settlementDate,
            SettlementType settlementType, Collateral collateral, List<TransactingParty> transactingParties) {
        this.executionVenue = executionVenue;
        this.instrument = instrument;
        this.rate = rate;
        this.quantity = quantity;
        this.billingCurrency = billingCurrency;
        this.dividendRatePct = dividendRatePct;
        this.tradeDate = tradeDate;
        this.settlementDate = settlementDate;
        this.settlementType = settlementType;
        this.collateral = collateral;
        this.transactingParties = transactingParties;
    }
    public ExecutionVenue getExecutionVenue() {
        return executionVenue;
    }
    public void setExecutionVenue(ExecutionVenue executionVenue) {
        this.executionVenue = executionVenue;
    }
    public Instrument getInstrument() {
        return instrument;
    }
    public void setInstrument(Instrument instrument) {
        this.instrument = instrument;
    }
    public Rate getRate() {
        return rate;
    }
    public void setRate(Rate rate) {
        this.rate = rate;
    }
    public Long getQuantity() {
        return quantity;
    }
    public void setQuantity(Long quantity) {
        this.quantity = quantity;
    }
    public Currency getBillingCurrency() {
        return billingCurrency;
    }
    public void setBillingCurrency(Currency billingCurrency) {
        this.billingCurrency = billingCurrency;
    }
    public BigDecimal getDividendRatePct() {
        return dividendRatePct;
    }
    public void setDividendRatePct(BigDecimal dividendRatePct) {
        this.dividendRatePct = dividendRatePct;
    }
    public String getTradeDate() {
        return tradeDate;
    }
    public void setTradeDate(String tradeDate) {
        this.tradeDate = tradeDate;
    }   
    public String getSettlementDate() {
        return settlementDate;
    }
    public void setSettlementDate(String settlementDate) {
        this.settlementDate = settlementDate;
    }
    public SettlementType getSettlementType() {
        return settlementType;
    }
    public void setSettlementType(SettlementType settlementType) {
        this.settlementType = settlementType;
    }
    public Collateral getCollateral() {
        return collateral;
    }
    public void setCollateral(Collateral collateral) {
        this.collateral = collateral;
    }
    public List<TransactingParty> getTransactingParties() {
        return transactingParties;
    }
    public void setTransactingParties(List<TransactingParty> transactingParties) {
        this.transactingParties = transactingParties;
    }

    
}
