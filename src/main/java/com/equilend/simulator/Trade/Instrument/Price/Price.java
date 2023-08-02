package com.equilend.simulator.Trade.Instrument.Price;

import com.equilend.simulator.Trade.Currency;

public class Price {
    private Float value;
    private Currency currency;
    private Unit unit;
    public Float getValue() {
        return value;
    }
    public void setValue(Float value) {
        this.value = value;
    }
    public Currency getCurrency() {
        return currency;
    }
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    public Unit getUnit() {
        return unit;
    }
    public void setUnit(Unit unit) {
        this.unit = unit;
    } 
}
