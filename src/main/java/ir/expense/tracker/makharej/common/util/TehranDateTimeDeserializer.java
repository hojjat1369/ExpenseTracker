package ir.expense.tracker.makharej.common.util;


import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Date;


/**
 * Reads "yyyy-MM-dd HH:mm:ss" (seconds are optional) as Tehran time, the way the rest of the API reads dates.
 * A number is still taken as epoch milliseconds.
 */
public class TehranDateTimeDeserializer extends JsonDeserializer<Date> {

	private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");
	private static final DateTimeFormatter FORMAT = new DateTimeFormatterBuilder()
			.appendPattern("uuuu-MM-dd HH:mm")
			.optionalStart().appendPattern(":ss").optionalEnd()
			.toFormatter()
			.withResolverStyle(ResolverStyle.STRICT);

	@Override
	public Date deserialize(JsonParser parser, DeserializationContext context) throws IOException {

		if (parser.currentToken() == JsonToken.VALUE_NUMBER_INT) {
			return new Date(parser.getLongValue());
		}
		String text = parser.getValueAsString();
		try {
			return Date.from(LocalDateTime.parse(text.trim(), FORMAT).atZone(TEHRAN).toInstant());
		}
		catch (DateTimeParseException | NullPointerException e) {
			return (Date) context.handleWeirdStringValue(Date.class, text, "expected a date like 2023-05-03 12:12:12");
		}
	}
}
