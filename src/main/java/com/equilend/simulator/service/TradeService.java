package com.equilend.simulator.service;

import static com.equilend.simulator.model.collateral.RoundingMode.ALWAYSUP;

import com.equilend.simulator.model.party.InternalReference;
import com.equilend.simulator.model.party.Party;
import com.equilend.simulator.model.party.PartyRole;
import com.equilend.simulator.model.party.TransactingParties;
import com.equilend.simulator.model.party.TransactingParty;
import com.equilend.simulator.model.trade.SettlementType;
import com.equilend.simulator.model.trade.TradeAgreement;
import com.equilend.simulator.model.collateral.Collateral;
import com.equilend.simulator.model.collateral.CollateralType;
import com.equilend.simulator.model.instrument.Instrument;
import com.equilend.simulator.model.instrument.price.CurrencyCd;
import com.equilend.simulator.model.rate.BenchmarkCd;
import com.equilend.simulator.model.rate.FloatingRateDef;
import com.equilend.simulator.model.rate.Rate;
import com.equilend.simulator.model.rate.RebateRate;
import com.equilend.simulator.model.venue.Venue;
import com.equilend.simulator.model.venue.VenueTradeAgreement;
import com.equilend.simulator.model.venue.Venues;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

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

        FloatingRateDef floating = new FloatingRateDef()
            .benchmark(BenchmarkCd.OBFR)
            .baseRate(null)
            .spread(Double.valueOf(".15"))
            .effectiveRate(Double.valueOf(".15"))
            .effectiveDate(LocalDate.now())
            .cutoffTime("18:00:00")
            .isAutoRerate(false);

        RebateRate rebate = new RebateRate(floating);
        Rate rate = new Rate(rebate);

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
        botTransactingParty.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
        transactingParties.add(botTransactingParty);
        TransactingParty counterTransactingParty = new TransactingParty();
        PartyRole counterpartyRole = partyRole == PartyRole.LENDER ? PartyRole.BORROWER : PartyRole.LENDER;
        counterTransactingParty.setPartyRole(counterpartyRole);
        counterTransactingParty.setParty(counterparty);
        counterTransactingParty.setInternalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));
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
            .transactingParties(transactingParties);

        return tradeAgreement;
    }

}
