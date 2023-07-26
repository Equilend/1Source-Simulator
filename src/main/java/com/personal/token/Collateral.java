package com.personal.token;

import java.math.BigDecimal;

public class Collateral {
    private BigDecimal contractValue;
    private BigDecimal collateralValue;
    private Currency currency;
    private CollateralType type;
    private DescriptionCd descriptionCd;
    private Integer roundingRule;
    private RoundingMode roundingMode;
    private BigDecimal margin;


    public Collateral(BigDecimal contractValue, BigDecimal collateralValue, Currency currency, CollateralType type,
            Integer roundingRule, RoundingMode roundingMode, BigDecimal margin) {
        this.contractValue = contractValue;
        this.collateralValue = collateralValue;
        this.currency = currency;
        this.type = type;
        this.roundingRule = roundingRule;
        this.roundingMode = roundingMode;
        this.margin = margin;
    }
    public BigDecimal getContractValue() {
        return contractValue;
    }
    public void setContractValue(BigDecimal contractValue) {
        this.contractValue = contractValue;
    }
    public BigDecimal getCollateralValue() {
        return collateralValue;
    }
    public void setCollateralValue(BigDecimal collateralValue) {
        this.collateralValue = collateralValue;
    }
    public Currency getCurrency() {
        return currency;
    }
    public void setCurrency(Currency currency) {
        this.currency = currency;
    }
    public CollateralType getType() {
        return type;
    }
    public void setType(CollateralType type) {
        this.type = type;
    }
    public DescriptionCd getDescriptionCd() {
        return descriptionCd;
    }
    public void setDescriptionCd(DescriptionCd descriptionCd) {
        this.descriptionCd = descriptionCd;
    }
    public BigDecimal getMargin() {
        return margin;
    }
    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }
    public Integer getRoundingRule() {
        return roundingRule;
    }
    public void setRoundingRule(Integer roundingRule) {
        this.roundingRule = roundingRule;
    }
    public RoundingMode getRoundingMode() {
        return roundingMode;
    }
    public void setRoundingMode(RoundingMode roundingMode) {
        this.roundingMode = roundingMode;
    }

    
}
