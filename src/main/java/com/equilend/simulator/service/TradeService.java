package com.equilend.simulator.service;

import static com.os.client.model.RoundingMode.ALWAYSUP;

import java.time.LocalDate;
import java.util.Optional;

import com.os.client.model.BenchmarkCd;
import com.os.client.model.Collateral;
import com.os.client.model.CollateralType;
import com.os.client.model.CurrencyCd;
import com.os.client.model.FloatingRate;
import com.os.client.model.FloatingRateDef;
import com.os.client.model.Instrument;
import com.os.client.model.Party;
import com.os.client.model.PartyRole;
import com.os.client.model.RebateRate;
import com.os.client.model.SettlementType;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParties;
import com.os.client.model.TransactingParty;
import com.os.client.model.Venue;
import com.os.client.model.VenueTradeAgreement;
import com.os.client.model.Venues;

public class TradeService {

    public static Optional<TransactingParty> getTransactingPartyById(TradeAgreement tradeAgreement, String botPartyId) {
        return tradeAgreement.getTransactingParties().stream()
            .filter(transactingParty -> botPartyId.equals(transactingParty.getParty().getPartyId())).findFirst();
    }

    public static Optional<TransactingParty> getTransactingPartyById(VenueTradeAgreement venueTradeAgreement,
        String botPartyId) {
        return venueTradeAgreement.getTransactingParties().stream()
            .filter(transactingParty -> botPartyId.equals(transactingParty.getParty().getPartyId())).findFirst();
    }

    public static TradeAgreement buildTradeAgreement(VenueTradeAgreement venueTradeAgreement) {
        Venues venues = new Venues();
        venues.add(venueTradeAgreement.getExecutionVenue());
        TradeAgreement trade = new TradeAgreement()
            .venues(venues)
            .instrument(venueTradeAgreement.getInstrument())
            .rate(venueTradeAgreement.getRate())
            .quantity(venueTradeAgreement.getQuantity())
            .billingCurrency(venueTradeAgreement.getBillingCurrency())
            .dividendRatePct(venueTradeAgreement.getDividendRatePct())
            .tradeDate(venueTradeAgreement.getTradeDate())
            .termType(venueTradeAgreement.getTermType())
            .termDate(venueTradeAgreement.getTermDate())
            .settlementDate(venueTradeAgreement.getSettlementDate())
            .settlementType(venueTradeAgreement.getSettlementType())
            .collateral(venueTradeAgreement.getCollateral())
            .transactingParties(venueTradeAgreement.getTransactingParties());
        return trade;
    }

    public static TradeAgreement createTrade(PartyRole partyRole, Party party, Party counterparty, Instrument security,
        Integer desiredQuantity) {
        Venue lenderVenueParty = new Venue().party(new Party().partyId(PartyRole.LENDER.getValue()));
        //Venue borrowerVenueParty = new VenueParty(PartyRole.BORROWER);
        Venue borrowerVenueParty = new Venue().party(new Party().partyId(PartyRole.BORROWER.getValue()));
        Venues venues = new Venues();
        venues.add(lenderVenueParty);
        venues.add(borrowerVenueParty);

        Instrument instrument = security;

//        FloatingRateDef floating = new FloatingRateDef()
//            .benchmark(BenchmarkCd.OBFR)
//            .baseRate(null)
//            .spread(Double.valueOf(".15"))
//            .effectiveRate(Double.valueOf(".15"))
//            .effectiveDate(LocalDate.now())
//            .cutoffTime("18:00:00")
//            .isAutoRerate(false);
//
//        RebateRate rebate = new RebateRate(floating);
//        Rate rate = new Rate(rebate);

		FloatingRateDef floatingRateDef = new FloatingRateDef();
		floatingRateDef.setSpread(Double.valueOf(".15"));
		floatingRateDef.setCutoffTime("18:00");
		floatingRateDef.setEffectiveDate(LocalDate.now());
		floatingRateDef.setBenchmark(BenchmarkCd.OBFR);
		floatingRateDef.setIsAutoRerate(false);

		FloatingRate floatingRate = new FloatingRate();
		floatingRate.setFloating(floatingRateDef);

		RebateRate rebateRate = new RebateRate();
		rebateRate.setRebate(floatingRate);

        Integer quantity = desiredQuantity;

        CurrencyCd billingCurrency = CurrencyCd.USD;

        Double dividendRatePct = Double.valueOf(100);

        LocalDate tPlus2 = LocalDate.now().plusDays(2);

        SettlementType settlementType = SettlementType.DVP;

        Collateral collateral;
        if (partyRole == PartyRole.LENDER) {
            collateral = new Collateral()
                .contractValue(Double.valueOf(8758750))
                .collateralValue(Double.valueOf(8933925))
                .currency(CurrencyCd.USD)
                .type(CollateralType.CASH)
                .roundingRule(10)
                .roundingMode(ALWAYSUP)
                .margin(Double.valueOf(102));
        } else {
            collateral = new Collateral()
                .contractValue(Double.valueOf(8758750))
                .collateralValue(Double.valueOf(8933925))
                .currency(CurrencyCd.USD)
                .type(CollateralType.CASH)
                .margin(Double.valueOf(102));
        }

        TransactingParties transactingParties = new TransactingParties();
        TransactingParty botTransactingParty = new TransactingParty();
        botTransactingParty.setPartyRole(partyRole);
        botTransactingParty.setParty(party);
        transactingParties.add(botTransactingParty);
        TransactingParty counterTransactingParty = new TransactingParty();
        PartyRole counterpartyRole = partyRole == PartyRole.LENDER ? PartyRole.BORROWER : PartyRole.LENDER;
        counterTransactingParty.setPartyRole(counterpartyRole);
        counterTransactingParty.setParty(counterparty);
        transactingParties.add(counterTransactingParty);

        TradeAgreement tradeAgreement = new TradeAgreement()
            .venues(venues)
            .instrument(instrument)
            .quantity(quantity)
            .billingCurrency(billingCurrency)
            .dividendRatePct(dividendRatePct)
            .tradeDate(tPlus2)
            .settlementDate(tPlus2)
            .settlementType(settlementType)
            .collateral(collateral)
            .rate(rebateRate)
            .transactingParties(transactingParties);

        return tradeAgreement;
    }

}
