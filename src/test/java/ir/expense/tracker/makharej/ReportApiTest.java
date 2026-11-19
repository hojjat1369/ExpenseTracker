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
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReportApiTest {

	@LocalServerPort
	private int port;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	private long food;
	private long travel;

	@BeforeEach
	void createExpenses() throws Exception {

		token = signupAndLogin();
		food = createCategory(token, "food");
		travel = createCategory(token, "travel");
		createExpense(token, "lunch", 100.5, "2023-01-15 10:00:00", food, "#daily");
		createExpense(token, "taxi", 50.0, "2023-01-20 10:00:00", travel, "#trip");
		createExpense(token, "dinner", 20.0, "2023-03-01 10:00:00", food, "#daily");
		createExpense(token, "coffee", 10.0, "2024-06-01 10:00:00", food, "#daily");
		long mistake = createExpense(token, "mistake", 1000.0, "2023-01-25 10:00:00", food, "#daily");
		assertThat(send("DELETE", "/api/expense?id=" + mistake, token, null).statusCode()).isEqualTo(200);
	}

	@Test
	void monthlyReportListsEveryMonthBetweenTheFirstAndTheLastExpense() throws Exception {

		JsonNode report = report(token, "{\"period\":\"MONTH\"}");

		assertThat(report.get("period").asText()).isEqualTo("MONTH");
		assertThat(report.get("totalAmount").asDouble()).isEqualTo(180.5);
		assertThat(report.get("count").asLong()).isEqualTo(4);
		List<String> months = labels(report);
		assertThat(months).hasSize(18).startsWith("2023-01", "2023-02", "2023-03", "2023-04").endsWith("2024-05", "2024-06");
		assertThat(entry(report, "2023-01").get("totalAmount").asDouble()).isEqualTo(150.5);
		assertThat(entry(report, "2023-01").get("count").asLong()).isEqualTo(2);
		assertThat(entry(report, "2023-02").get("totalAmount").asDouble()).isEqualTo(0.0);
		assertThat(entry(report, "2023-02").get("count").asLong()).isEqualTo(0);
		assertThat(entry(report, "2023-03").get("totalAmount").asDouble()).isEqualTo(20.0);
		assertThat(entry(report, "2024-06").get("totalAmount").asDouble()).isEqualTo(10.0);
	}

	@Test
	void yearlyReport() throws Exception {

		JsonNode report = report(token, "{\"period\":\"YEAR\"}");

		assertThat(labels(report)).containsExactly("2023", "2024");
		assertThat(entry(report, "2023").get("totalAmount").asDouble()).isEqualTo(170.5);
		assertThat(entry(report, "2023").get("count").asLong()).isEqualTo(3);
		assertThat(entry(report, "2024").get("totalAmount").asDouble()).isEqualTo(10.0);
	}

	@Test
	void filtersWorkLikeInTheExpenseSearch() throws Exception {

		JsonNode byCategory = report(token, "{\"period\":\"YEAR\",\"categoryId\":" + travel + "}");
		JsonNode byTag = report(token, "{\"period\":\"YEAR\",\"tag\":\"daily\"}");
		JsonNode byName = report(token, "{\"period\":\"YEAR\",\"name\":\"lunch\"}");
		JsonNode byDates = report(token, "{\"period\":\"MONTH\",\"fromDate\":\"2023-01-16 00:00:00\",\"toDate\":\"2023-12-31 23:59:59\"}");

		assertThat(byCategory.get("totalAmount").asDouble()).isEqualTo(50.0);
		assertThat(labels(byCategory)).containsExactly("2023");
		assertThat(byTag.get("totalAmount").asDouble()).isEqualTo(130.5);
		assertThat(byName.get("totalAmount").asDouble()).isEqualTo(100.5);
		assertThat(byDates.get("totalAmount").asDouble()).isEqualTo(70.0);
		assertThat(labels(byDates)).containsExactly("2023-01", "2023-02", "2023-03");
	}

	@Test
	void aReportOnlyCoversTheExpensesOfTheCaller() throws Exception {

		String other = signupAndLogin();
		assertThat(report(other, "{\"period\":\"YEAR\"}").get("periods")).isEmpty();
		long otherCategory = createCategory(other, "mine");
		createExpense(other, "secret", 777.0, "2023-01-15 10:00:00", otherCategory, "#x");

		JsonNode othersReport = report(other, "{\"period\":\"YEAR\"}");
		JsonNode myReportForTheirCategory = report(token, "{\"period\":\"YEAR\",\"categoryId\":" + otherCategory + "}");

		assertThat(othersReport.get("totalAmount").asDouble()).isEqualTo(777.0);
		assertThat(report(token, "{\"period\":\"YEAR\"}").get("totalAmount").asDouble()).isEqualTo(180.5);
		assertThat(myReportForTheirCategory.get("periods")).isEmpty();
		assertThat(myReportForTheirCategory.get("totalAmount").asDouble()).isEqualTo(0.0);
	}

	@Test
	void thePeriodIsRequired() throws Exception {

		HttpResponse<String> missing = send("POST", "/api/report", token, "{}");
		HttpResponse<String> unknown = send("POST", "/api/report", token, "{\"period\":\"WEEK\"}");

		assertThat(missing.statusCode()).isEqualTo(400);
		assertThat(objectMapper.readTree(missing.body()).get("period").asText()).isEqualTo("period cannot be null!");
		assertThat(unknown.statusCode()).isEqualTo(400);
	}

	@Test
	void aLoginIsRequired() throws Exception {

		HttpResponse<String> response = send("POST", "/api/report", null, "{\"period\":\"YEAR\"}");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).isEmpty();
	}

	private JsonNode report(String token, String body) throws Exception {

		HttpResponse<String> response = send("POST", "/api/report", token, body);
		assertThat(response.statusCode()).isEqualTo(200);
		return objectMapper.readTree(response.body());
	}

	private List<String> labels(JsonNode report) {

		List<String> labels = new ArrayList<>();
		report.get("periods").forEach(period -> labels.add(period.get("period").asText()));
		return labels;
	}

	private JsonNode entry(JsonNode report, String label) {

		for (JsonNode period : report.get("periods")) {
			if (label.equals(period.get("period").asText())) {
				return period;
			}
		}
		throw new AssertionError("no period " + label + " in " + report);
	}

	private long createCategory(String token, String name) throws Exception {

		HttpResponse<String> response = send("POST", "/api/category", token, "{\"name\":\"" + name + "\"}");
		assertThat(response.statusCode()).isEqualTo(201);
		return objectMapper.readTree(response.body()).get("id").asLong();
	}

	private long createExpense(String token, String name, double amount, String date, long category, String tag) throws Exception {

		String body = "{\"name\":\"" + name + "\",\"amount\":" + amount + ",\"expenseDate\":\"" + date + "\",\"categoryId\":" + category + ",\"tag\":\"" + tag + "\"}";
		HttpResponse<String> response = send("POST", "/api/expense", token, body);
		assertThat(response.statusCode()).isEqualTo(201);
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
