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
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
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

		JsonObject rateJsonObject = json.getAsJsonObject();

		if (rateJsonObject.has("rebate")) {
			RebateRate rebateRate = new RebateRate();

			JsonObject rebateJsonObject = rateJsonObject.getAsJsonObject("rebate");
			if (rebateJsonObject.has("fixed")) {

				JsonObject fixedRebateJsonObject = rebateJsonObject.getAsJsonObject("fixed");

				FixedRate fixedRate = new FixedRate();
				rebateRate.setRebate(fixedRate);

				FixedRateDef fixedRateDef = new FixedRateDef();
				fixedRate.setFixed(fixedRateDef);

				if (fixedRebateJsonObject.has("baseRate")) {
					fixedRateDef.setBaseRate(fixedRebateJsonObject.get("baseRate").getAsDouble());
				}

				if (fixedRebateJsonObject.has("effectiveRate")) {
					fixedRateDef.setEffectiveRate(fixedRebateJsonObject.get("baseRate").getAsDouble());
				}

				if (fixedRebateJsonObject.has("effectiveDate")) {
					fixedRateDef
							.setEffectiveDate(LocalDate.parse(fixedRebateJsonObject.get("effectiveDate").getAsString(),
									DateTimeFormatter.ISO_LOCAL_DATE));
				}

				if (fixedRebateJsonObject.has("cutoffTime")) {
					fixedRateDef.setCutoffTime(fixedRebateJsonObject.get("cutoffTime").getAsString());
				}

			} else if (rebateJsonObject.has("floating")) {

				JsonObject floatingRebateJsonObject = rebateJsonObject.getAsJsonObject("floating");

				FloatingRate floatingRate = new FloatingRate();
				rebateRate.setRebate(floatingRate);

				FloatingRateDef floatingRateDef = new FloatingRateDef();
				floatingRate.setFloating(floatingRateDef);

				if (floatingRebateJsonObject.has("benchmark")) {
					floatingRateDef.setBenchmark(
							BenchmarkCd.fromValue(floatingRebateJsonObject.get("benchmark").getAsString()));
				}

				if (floatingRebateJsonObject.has("baseRate")) {
					floatingRateDef.setBaseRate(floatingRebateJsonObject.get("baseRate").getAsDouble());
				}

				if (floatingRebateJsonObject.has("spread")) {
					floatingRateDef.setSpread(floatingRebateJsonObject.get("spread").getAsDouble());
				}

				if (floatingRebateJsonObject.has("effectiveRate")) {
					floatingRateDef.setEffectiveRate(floatingRebateJsonObject.get("baseRate").getAsDouble());
				}

				if (floatingRebateJsonObject.has("effectiveDate")) {
					floatingRateDef.setEffectiveDate(
							LocalDate.parse(floatingRebateJsonObject.get("effectiveDate").getAsString(),
									DateTimeFormatter.ISO_LOCAL_DATE));
				}

				if (floatingRebateJsonObject.has("cutoffTime")) {
					floatingRateDef.setCutoffTime(floatingRebateJsonObject.get("cutoffTime").getAsString());
				}

				if (floatingRebateJsonObject.has("isAutoRerate")) {
					floatingRateDef.setIsAutoRerate(floatingRebateJsonObject.get("isAutoRerate").getAsBoolean());
				}
			}

			impl = rebateRate;
		} else if (rateJsonObject.has("fee")) {

			JsonObject feeJsonObject = rateJsonObject.getAsJsonObject("fee");

			FeeRate feeRate = new FeeRate();

			FixedRateDef fixedRateDef = new FixedRateDef();
			feeRate.setFee(fixedRateDef);

			if (feeJsonObject.has("baseRate")) {
				fixedRateDef.setBaseRate(feeJsonObject.get("baseRate").getAsDouble());
			}

			if (feeJsonObject.has("effectiveRate")) {
				fixedRateDef.setEffectiveRate(feeJsonObject.get("baseRate").getAsDouble());
			}

			if (feeJsonObject.has("effectiveDate")) {
				fixedRateDef.setEffectiveDate(LocalDate.parse(feeJsonObject.get("effectiveDate").getAsString(),
						DateTimeFormatter.ISO_LOCAL_DATE));
			}

			if (feeJsonObject.has("cutoffTime")) {
				fixedRateDef.setCutoffTime(feeJsonObject.get("cutoffTime").getAsString());
			}
		}

		return impl;
	}
}
