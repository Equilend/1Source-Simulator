package com.equilend.simulator.api;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.os.client.model.BenchmarkCd;
import com.os.client.model.FeeRate;
import com.os.client.model.FixedRate;
import com.os.client.model.FixedRateDef;
import com.os.client.model.FloatingRate;
import com.os.client.model.FloatingRateDef;
import com.os.client.model.Rate;
import com.os.client.model.RebateRate;

public class RateGsonAdapter implements JsonDeserializer<Rate> {

	@Override
	public Rate deserialize(final JsonElement json, final Type typeOfT, final JsonDeserializationContext context)
			throws JsonParseException {
		Rate impl = null;

		JsonObject jsonObject = (JsonObject) json;
		JsonObject nodeRateRebate = jsonObject.getAsJsonObject("rebate");

		if (nodeRateRebate != null) {
			RebateRate rebateRate = new RebateRate();

			JsonObject nodeRateRebateFixed = nodeRateRebate.getAsJsonObject("fixed");
			if (nodeRateRebateFixed != null) {
				FixedRate fixedRate = new FixedRate();
				rebateRate.setRebate(fixedRate);

				FixedRateDef fixedRateDef = new FixedRateDef();
				fixedRate.setFixed(fixedRateDef);

				JsonPrimitive nodeBaseRate = nodeRateRebateFixed.getAsJsonPrimitive("baseRate");
				if (nodeBaseRate != null) {
					fixedRateDef.setBaseRate(nodeBaseRate.getAsDouble());
				}

				JsonPrimitive nodeEffectiveRate = nodeRateRebateFixed.getAsJsonPrimitive("effectiveRate");
				if (nodeEffectiveRate != null) {
					fixedRateDef.setEffectiveRate(nodeEffectiveRate.getAsDouble());
				}

				JsonPrimitive nodeEffectiveDate = nodeRateRebateFixed.getAsJsonPrimitive("effectiveDate");
				if (nodeEffectiveDate != null) {
					fixedRateDef.setEffectiveDate(
							LocalDate.parse(nodeEffectiveDate.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE));
				}

				JsonPrimitive nodeCutoffTime = nodeRateRebateFixed.getAsJsonPrimitive("cutoffTime");
				if (nodeCutoffTime != null) {
					fixedRateDef.setCutoffTime(nodeCutoffTime.getAsString());
				}

			} else {
				JsonObject nodeRateRebateFloating = nodeRateRebate.getAsJsonObject("floating");
				if (nodeRateRebateFloating != null) {
					FloatingRate floatingRate = new FloatingRate();
					rebateRate.setRebate(floatingRate);

					FloatingRateDef floatingRateDef = new FloatingRateDef();
					floatingRate.setFloating(floatingRateDef);

					JsonPrimitive nodeBenchmark = nodeRateRebateFloating.getAsJsonPrimitive("benchmark");
					if (nodeBenchmark != null) {
						floatingRateDef.setBenchmark(BenchmarkCd.fromValue(nodeBenchmark.getAsString()));
					}

					JsonPrimitive nodeBaseRate = nodeRateRebateFloating.getAsJsonPrimitive("baseRate");
					if (nodeBaseRate != null) {
						floatingRateDef.setBaseRate(nodeBaseRate.getAsDouble());
					}

					JsonPrimitive nodeSpread = nodeRateRebateFloating.getAsJsonPrimitive("spread");
					if (nodeSpread != null) {
						floatingRateDef.setSpread(nodeSpread.getAsDouble());
					}

					JsonPrimitive nodeIsAutoRerate = nodeRateRebateFloating.getAsJsonPrimitive("isAutoRerate");
					if (nodeIsAutoRerate != null) {
						floatingRateDef.setIsAutoRerate(nodeIsAutoRerate.getAsBoolean());
					}

					JsonPrimitive nodeEffectiveRate = nodeRateRebateFloating.getAsJsonPrimitive("effectiveRate");
					if (nodeEffectiveRate != null) {
						floatingRateDef.setEffectiveRate(nodeEffectiveRate.getAsDouble());
					}

					JsonPrimitive nodeEffectiveDate = nodeRateRebateFloating.getAsJsonPrimitive("effectiveDate");
					if (nodeEffectiveDate != null) {
						floatingRateDef.setEffectiveDate(
								LocalDate.parse(nodeEffectiveDate.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE));
					}

					JsonPrimitive nodeCutoffTime = nodeRateRebateFloating.getAsJsonPrimitive("cutoffTime");
					if (nodeCutoffTime != null) {
						floatingRateDef.setCutoffTime(nodeCutoffTime.getAsString());
					}
				}
			}

			impl = rebateRate;
		} else {
			JsonObject nodeRateFee = jsonObject.getAsJsonObject("fee");
			if (nodeRateFee != null) {
				FeeRate feeRate = new FeeRate();
				impl = feeRate;

				FixedRateDef fixedRateDef = new FixedRateDef();
				feeRate.setFee(fixedRateDef);

				JsonPrimitive nodeBaseRate = nodeRateFee.getAsJsonPrimitive("baseRate");
				if (nodeBaseRate != null) {
					fixedRateDef.setBaseRate(nodeBaseRate.getAsDouble());
				}

				JsonPrimitive nodeEffectiveRate = nodeRateFee.getAsJsonPrimitive("effectiveRate");
				if (nodeEffectiveRate != null) {
					fixedRateDef.setEffectiveRate(nodeEffectiveRate.getAsDouble());
				}

				JsonPrimitive nodeEffectiveDate = nodeRateFee.getAsJsonPrimitive("effectiveDate");
				if (nodeEffectiveDate != null) {
					fixedRateDef.setEffectiveDate(
							LocalDate.parse(nodeEffectiveDate.getAsString(), DateTimeFormatter.ISO_LOCAL_DATE));
				}

				JsonPrimitive nodeCutoffTime = nodeRateFee.getAsJsonPrimitive("cutoffTime");
				if (nodeCutoffTime != null) {
					fixedRateDef.setCutoffTime(nodeCutoffTime.getAsString());
				}

			}

		}
		
		return impl;
	}
}