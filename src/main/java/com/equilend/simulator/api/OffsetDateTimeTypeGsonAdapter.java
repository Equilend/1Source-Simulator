package com.equilend.simulator.api;

import java.lang.reflect.Type;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class OffsetDateTimeTypeGsonAdapter implements JsonSerializer<OffsetDateTime>, JsonDeserializer<OffsetDateTime> {

	private static final Logger logger = LogManager.getLogger(OffsetDateTimeTypeGsonAdapter.class.getName());

	private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter
			.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

	@Override
	public JsonElement serialize(final OffsetDateTime date, final Type typeOfSrc,
			final JsonSerializationContext context) {
		return new JsonPrimitive(date.format(DATE_TIME_FORMATTER));
	}

	@Override
	public OffsetDateTime deserialize(final JsonElement json, final Type typeOfT,
			final JsonDeserializationContext context) throws JsonParseException {

		OffsetDateTime offsetDateTime = null;

		String dateAsString = json.getAsString();

		if (dateAsString == null) {
			throw new JsonParseException("OffsetDateTime argument is null.");
		}

		logger.debug("offsetDateTime: " + dateAsString);

		try {
			offsetDateTime = OffsetDateTime.parse(json.getAsString(), DateTimeFormatter.ISO_INSTANT);
		} catch (DateTimeException d) {
			logger.warn("Could not parse offset date time as ISO_INSTANT: " + dateAsString);
		}

		if (offsetDateTime == null) {
			try {

				if (dateAsString.endsWith("Z")) {
					dateAsString = dateAsString.substring(0, dateAsString.indexOf("Z"));
				}

				int milliSize = dateAsString.substring(dateAsString.indexOf(".") + 1).length();

				for (int i = milliSize; i < 3; i++) {
					dateAsString = dateAsString + "0";
				}

				LocalDateTime dateTime = LocalDateTime.parse(dateAsString, DATE_TIME_FORMATTER);

				offsetDateTime = OffsetDateTime.of(dateTime, ZoneOffset.UTC);

			} catch (DateTimeException d) {
				logger.warn("Could not parse offset date time: " + dateAsString);
				offsetDateTime = OffsetDateTime.MIN;
			}
		}

		return offsetDateTime;
	}
}