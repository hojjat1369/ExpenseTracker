package ir.expense.tracker.makharej.common.util;


import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import ir.expense.tracker.makharej.dto.expense.ExpenseUpdateRequest;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Date;


public class TehranDateTimeDeserializerTest {

	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	public void dateWithSecondsIsTehranTime() throws Exception {

		Assertions.assertThat(read("\"2024-06-07 08:09:10\"")).isEqualTo(Date.from(Instant.parse("2024-06-07T04:39:10Z")));
	}

	@Test
	public void dateWithoutSecondsIsTehranTime() throws Exception {

		Assertions.assertThat(read("\"2024-06-07 08:09\"")).isEqualTo(Date.from(Instant.parse("2024-06-07T04:39:00Z")));
	}

	@Test
	public void surroundingSpacesAreIgnored() throws Exception {

		Assertions.assertThat(read("\" 2024-06-07 08:09:10 \"")).isEqualTo(Date.from(Instant.parse("2024-06-07T04:39:10Z")));
	}

	@Test
	public void numberIsEpochMilliseconds() throws Exception {

		Assertions.assertThat(read("1717735150000")).isEqualTo(new Date(1717735150000L));
	}

	@Test
	public void missingOrNullDateStaysNull() throws Exception {

		Assertions.assertThat(read("null")).isNull();
		Assertions.assertThat(objectMapper.readValue("{\"id\":1}", ExpenseUpdateRequest.class).getExpenseDate()).isNull();
	}

	@Test
	public void somethingThatIsNotADateIsRefused() {

		for (String text : new String[] { "\"yesterday\"", "\"2024-02-30 10:00:00\"", "\"2024-13-01 10:00:00\"", "\"03/05/2023\"", "\"\"", "\"2024-06-07\"", "true" }) {
			Assertions.assertThatThrownBy(() -> read(text)).as(text).isInstanceOf(JsonMappingException.class);
		}
	}

	private Date read(String json) throws Exception {

		return objectMapper.readValue("{\"id\":1,\"expenseDate\":" + json + "}", ExpenseUpdateRequest.class).getExpenseDate();
	}
}
