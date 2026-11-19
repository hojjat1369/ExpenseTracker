package ir.expense.tracker.makharej;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class TriggerApiTest {

	private static final ZoneId TEHRAN = ZoneId.of("Asia/Tehran");
	private static final String TRIGGER_NOT_FOUND = "{\"statusCode\":\"BAD_REQUEST\",\"message\":\"trigger not found!\"}";

	@LocalServerPort
	private int port;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	private long coffee;
	private long trigger;

	private final String thisMonth = YearMonth.now(TEHRAN).toString();
	private final String aDayThisMonth = LocalDate.now(TEHRAN).withDayOfMonth(1) + " 12:00:00";
	private final String aDayLastYear = LocalDate.now(TEHRAN).withDayOfMonth(1).minusYears(1) + " 12:00:00";

	@BeforeEach
	void createTrigger() throws Exception {

		token = signupAndLogin();
		coffee = idOf(send("POST", "/api/category", token, "{\"name\":\"coffee\"}"));
		String body = "{\"name\":\"too much coffee\",\"period\":\"MONTH\",\"limit\":50,\"tag\":\"coffee\",\"categoryId\":" + coffee + "}";
		HttpResponse<String> created = send("POST", "/api/trigger", token, body);
		assertThat(created.statusCode()).isEqualTo(201);
		trigger = objectMapper.readTree(created.body()).get("id").asLong();
	}

	@Test
	void theAlarmGoesOffWhenTheMonthGetsTooExpensive() throws Exception {

		JsonNode quiet = get(trigger);
		assertThat(quiet.get("currentPeriod").asText()).isEqualTo(thisMonth);
		assertThat(quiet.get("currentAmount").asDouble()).isEqualTo(0.0);
		assertThat(quiet.get("triggered").asBoolean()).isFalse();

		long first = expense(token, "latte", 30.0, aDayThisMonth, coffee, "coffee");
		assertThat(get(trigger).get("currentAmount").asDouble()).isEqualTo(30.0);
		assertThat(get(trigger).get("triggered").asBoolean()).isFalse();

		expense(token, "espresso", 30.0, aDayThisMonth, coffee, "coffee");
		JsonNode loud = get(trigger);
		assertThat(loud.get("currentAmount").asDouble()).isEqualTo(60.0);
		assertThat(loud.get("triggered").asBoolean()).isTrue();
		assertThat(ids(find(token, "{\"triggered\":true}"))).containsExactly(trigger);
		assertThat(ids(find(token, "{\"triggered\":false}"))).isEmpty();

		assertThat(send("DELETE", "/api/expense?id=" + first, token, null).statusCode()).isEqualTo(200);
		assertThat(get(trigger).get("currentAmount").asDouble()).isEqualTo(30.0);
		assertThat(get(trigger).get("triggered").asBoolean()).isFalse();
	}

	@Test
	void onlyMatchingExpensesOfTheCurrentPeriodCount() throws Exception {

		expense(token, "latte", 30.0, aDayThisMonth, coffee, "coffee");
		expense(token, "old latte", 500.0, aDayLastYear, coffee, "coffee");
		expense(token, "taxi", 500.0, aDayThisMonth, coffee, "taxi");
		assertThat(get(trigger).get("currentAmount").asDouble()).isEqualTo(30.0);

		String yearly = "{\"name\":\"coffee year\",\"period\":\"YEAR\",\"limit\":1000,\"categoryId\":" + coffee + "}";
		long yearlyId = idOf(send("POST", "/api/trigger", token, yearly));
		JsonNode year = get(yearlyId);
		assertThat(year.get("currentPeriod").asText()).isEqualTo(String.valueOf(YearMonth.now(TEHRAN).getYear()));
		assertThat(year.get("currentAmount").asDouble()).isEqualTo(530.0);
	}

	@Test
	void aTriggerCanBeChangedAndDeleted() throws Exception {

		expense(token, "latte", 30.0, aDayThisMonth, coffee, "coffee");

		String update = "{\"id\":" + trigger + ",\"name\":\"stricter\",\"period\":\"MONTH\",\"limit\":20,\"tag\":\"coffee\"}";
		JsonNode updated = objectMapper.readTree(send("PATCH", "/api/trigger", token, update).body());
		assertThat(updated.get("name").asText()).isEqualTo("stricter");
		assertThat(updated.get("limit").asDouble()).isEqualTo(20.0);
		assertThat(updated.get("triggered").asBoolean()).isTrue();
		assertThat(updated.get("categoryId").isNull()).isTrue();

		assertThat(send("DELETE", "/api/trigger?id=" + trigger, token, null).statusCode()).isEqualTo(200);
		HttpResponse<String> gone = send("GET", "/api/trigger?id=" + trigger, token, null);
		assertThat(gone.statusCode()).isEqualTo(400);
		assertThat(gone.body()).isEqualTo(TRIGGER_NOT_FOUND);
		assertThat(find(token, "{}")).isEmpty();
	}

	@Test
	void triggersBelongToTheirOwner() throws Exception {

		String other = signupAndLogin();

		assertThat(send("GET", "/api/trigger?id=" + trigger, other, null).body()).isEqualTo(TRIGGER_NOT_FOUND);
		assertThat(send("DELETE", "/api/trigger?id=" + trigger, other, null).body()).isEqualTo(TRIGGER_NOT_FOUND);
		String update = "{\"id\":" + trigger + ",\"name\":\"mine now\",\"period\":\"MONTH\",\"limit\":1}";
		assertThat(send("PATCH", "/api/trigger", other, update).body()).isEqualTo(TRIGGER_NOT_FOUND);
		assertThat(find(other, "{}")).isEmpty();
		assertThat(get(trigger).get("name").asText()).isEqualTo("too much coffee");

		String withForeignCategory = "{\"name\":\"sneaky\",\"period\":\"MONTH\",\"limit\":1,\"categoryId\":" + coffee + "}";
		HttpResponse<String> sneaky = send("POST", "/api/trigger", other, withForeignCategory);
		assertThat(sneaky.statusCode()).isEqualTo(400);
		assertThat(sneaky.body()).contains("category not found!");
	}

	@Test
	void theRuleIsValidated() throws Exception {

		HttpResponse<String> empty = send("POST", "/api/trigger", token, "{}");
		HttpResponse<String> zero = send("POST", "/api/trigger", token, "{\"name\":\"x\",\"period\":\"MONTH\",\"limit\":0}");

		assertThat(empty.statusCode()).isEqualTo(400);
		JsonNode errors = objectMapper.readTree(empty.body());
		assertThat(errors.get("name").asText()).isEqualTo("name cannot be null/empty!");
		assertThat(errors.get("period").asText()).isEqualTo("period cannot be null!");
		assertThat(errors.get("limit").asText()).isEqualTo("limit cannot be null!");
		assertThat(zero.statusCode()).isEqualTo(400);
		assertThat(objectMapper.readTree(zero.body()).get("limit").asText()).isEqualTo("limit must be greater than zero!");
	}

	@Test
	void aLoginIsRequired() throws Exception {

		assertThat(send("GET", "/api/trigger?id=" + trigger, null, null).statusCode()).isEqualTo(401);
		assertThat(send("POST", "/api/trigger/find", null, "{}").statusCode()).isEqualTo(401);
	}

	private JsonNode get(long id) throws Exception {

		HttpResponse<String> response = send("GET", "/api/trigger?id=" + id, token, null);
		assertThat(response.statusCode()).isEqualTo(200);
		return objectMapper.readTree(response.body());
	}

	private JsonNode find(String token, String body) throws Exception {

		HttpResponse<String> response = send("POST", "/api/trigger/find", token, body);
		assertThat(response.statusCode()).isEqualTo(200);
		return objectMapper.readTree(response.body());
	}

	private List<Long> ids(JsonNode triggers) {

		List<Long> ids = new ArrayList<>();
		triggers.forEach(node -> ids.add(node.get("id").asLong()));
		return ids;
	}

	private long expense(String token, String name, double amount, String date, long category, String tag) throws Exception {

		String body = "{\"name\":\"" + name + "\",\"amount\":" + amount + ",\"expenseDate\":\"" + date + "\",\"categoryId\":" + category + ",\"tag\":\"" + tag + "\"}";
		return idOf(send("POST", "/api/expense", token, body));
	}

	private long idOf(HttpResponse<String> response) throws Exception {

		assertThat(response.statusCode()).isIn(200, 201);
		return objectMapper.readTree(response.body()).get("id").asLong();
	}

	private String signupAndLogin() throws Exception {

		String username = "user-" + UUID.randomUUID();
		String signup = "{\"name\":\"Some One\",\"username\":\"" + username + "\",\"password\":\"secret\"}";
		assertThat(send("POST", "/api/auth/signup", null, signup).statusCode()).isEqualTo(200);
		HttpResponse<String> login = send("POST", "/api/auth/login", null, "{\"username\":\"" + username + "\",\"password\":\"secret\"}");
		assertThat(login.statusCode()).isEqualTo(200);
		return objectMapper.readTree(login.body()).get("token").asText();
	}

	private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {

		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
												 .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		if (body != null) {
			builder.header("Content-Type", "application/json");
		}
		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}
		return http.send(builder.build(), HttpResponse.BodyHandlers.ofString());
	}
}
