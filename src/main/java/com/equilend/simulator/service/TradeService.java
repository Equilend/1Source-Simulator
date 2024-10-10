package com.equilend.simulator.service;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.equilend.simulator.configurator.Config;
import com.os.client.model.Collateral;
import com.os.client.model.CollateralType;
import com.os.client.model.CurrencyCd;
import com.os.client.model.FixedRate;
import com.os.client.model.FixedRateDef;
import com.os.client.model.Instrument;
import com.os.client.model.InternalReference;
import com.os.client.model.Party;
import com.os.client.model.PartyRole;
import com.os.client.model.RebateRate;
import com.os.client.model.RoundingMode;
import com.os.client.model.SettlementType;
import com.os.client.model.TermType;
import com.os.client.model.TradeAgreement;
import com.os.client.model.TransactingParties;
import com.os.client.model.TransactingParty;
import com.os.client.model.Venue;
import com.os.client.model.VenueTradeAgreement;
import com.os.client.model.VenueType;
import com.os.client.model.Venues;

public class TradeService {

	private static final Logger logger = LogManager.getLogger(TradeService.class.getName());

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
		TradeAgreement trade = new TradeAgreement().venues(venues).instrument(venueTradeAgreement.getInstrument())
				.rate(venueTradeAgreement.getRate()).quantity(venueTradeAgreement.getQuantity())
				.billingCurrency(venueTradeAgreement.getBillingCurrency())
				.dividendRatePct(venueTradeAgreement.getDividendRatePct()).tradeDate(venueTradeAgreement.getTradeDate())
				.termType(venueTradeAgreement.getTermType()).termDate(venueTradeAgreement.getTermDate())
				.settlementDate(venueTradeAgreement.getSettlementDate())
				.settlementType(venueTradeAgreement.getSettlementType()).collateral(venueTradeAgreement.getCollateral())
				.transactingParties(venueTradeAgreement.getTransactingParties());
		return trade;
	}

	public static TradeAgreement createTrade(PartyRole partyRole, String partyId, String counterPartyId,
			String security, Integer quantity, Double rate, Double price, String termType) {
		Party party = Config.getInstance().getParties().get(partyId);
		Party counterParty = Config.getInstance().getParties().get(counterPartyId);
		Venue venueParty = new Venue().party(party).type(VenueType.OFFPLATFORM);
		Venue venueCounterParty = new Venue().party(counterParty).type(VenueType.OFFPLATFORM);
		Venues venues = new Venues();
		venues.add(venueParty);
		venues.add(venueCounterParty);

		int bang = security.indexOf("!");
		Instrument instrument;
		if (bang == -1) {
			instrument = Config.getInstance().getInstruments().get(security);
		} else {
			String idValue = security.substring(bang + 1).trim();
			instrument = new Instrument().figi(idValue).description("Security LLC");
		}

		FixedRateDef fixedRateDef = new FixedRateDef().baseRate(rate).effectiveRate(rate).effectiveDate(LocalDate.now())
				.cutoffTime("18:00:00");
		FixedRate fixedRate = new FixedRate();
		fixedRate.setFixed(fixedRateDef);
		RebateRate rebateRate = new RebateRate();
		rebateRate.setRebate(fixedRate);

		Double collateralMargin = Double.valueOf(102);
		Double contractValue = price * quantity;
		Double collateralValue = contractValue * collateralMargin / 100.0;
		Collateral collateral;
		if (partyRole == PartyRole.LENDER) {
			collateral = new Collateral().collateralValue(collateralValue).contractPrice(price)
					.contractValue(contractValue).currency(CurrencyCd.USD).type(CollateralType.CASH).roundingRule(10)
					.roundingMode(RoundingMode.ALWAYSUP).margin(collateralMargin);
		} else {
			collateral = new Collateral().collateralValue(collateralValue).contractPrice(price)
					.contractValue(contractValue).currency(CurrencyCd.USD).type(CollateralType.CASH)
					.margin(collateralMargin);
		}

		TransactingParty botTransactingParty = new TransactingParty().partyRole(partyRole).party(party)
				.internalRef(new InternalReference().internalRefId(UUID.randomUUID().toString()));

		PartyRole counterpartyRole = partyRole == PartyRole.LENDER ? PartyRole.BORROWER : PartyRole.LENDER;
		TransactingParty counterTransactingParty = new TransactingParty().partyRole(counterpartyRole)
				.party(counterParty).internalRef(new InternalReference());
		TransactingParties transactingParties = new TransactingParties();
		transactingParties.add(botTransactingParty);
		transactingParties.add(counterTransactingParty);

		TradeAgreement tradeAgreement = new TradeAgreement().venues(venues).instrument(instrument).quantity(quantity)
				.rate(rebateRate).billingCurrency(CurrencyCd.USD).dividendRatePct(Double.valueOf(100))
				.tradeDate(LocalDate.now()).settlementDate(LocalDate.now()).settlementType(SettlementType.DVP)
				.termType(TermType.fromValue(termType)).collateral(collateral).transactingParties(transactingParties);

		return tradeAgreement;
	}

}
