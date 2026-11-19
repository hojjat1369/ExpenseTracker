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
import java.time.Instant;
import java.time.OffsetDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Dates are written in Tehran time (UTC+03:30). Updating an expense has to read them exactly like creating one does.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ExpenseDateTest {

	@LocalServerPort
	private int port;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String token;
	private long category;
	private long expense;

	@BeforeEach
	void createAnExpense() throws Exception {

		token = signupAndLogin();
		category = idOf(send("POST", "/api/category", "{\"name\":\"food\"}"));
		String body = "{\"name\":\"lunch\",\"amount\":10,\"expenseDate\":\"2023-05-03 12:12:12\",\"categoryId\":" + category + ",\"tag\":\"#daily\",\"note\":\"n\"}";
		HttpResponse<String> created = send("POST", "/api/expense", body);
		assertThat(created.statusCode()).isEqualTo(201);
		assertThat(dateOf(created)).isEqualTo(Instant.parse("2023-05-03T08:42:12Z"));
		expense = idOf(created);
	}

	@Test
	void updateReadsTheDateLikeCreateDoes() throws Exception {

		HttpResponse<String> updated = update("{\"id\":" + expense + ",\"amount\":10,\"expenseDate\":\"2024-06-07 08:09:10\"}");

		assertThat(updated.statusCode()).isEqualTo(200);
		assertThat(dateOf(updated)).isEqualTo(Instant.parse("2024-06-07T04:39:10Z"));
		assertThat(dateOf(send("GET", "/api/expense?id=" + expense, null))).isEqualTo(Instant.parse("2024-06-07T04:39:10Z"));
	}

	@Test
	void updateStillAcceptsTheDateWithoutSeconds() throws Exception {

		HttpResponse<String> updated = update("{\"id\":" + expense + ",\"amount\":10,\"expenseDate\":\"2024-06-07 08:09\"}");

		assertThat(updated.statusCode()).isEqualTo(200);
		assertThat(dateOf(updated)).isEqualTo(Instant.parse("2024-06-07T04:39:00Z"));
	}

	@Test
	void updatedExpenseIsFoundInTheRightDateRange() throws Exception {

		update("{\"id\":" + expense + ",\"amount\":10,\"expenseDate\":\"2024-06-07 08:09:10\"}");

		String june = "{\"fromDate\":\"2024-06-01 00:00:00\",\"toDate\":\"2024-06-30 23:59:59\"}";
		String may2023 = "{\"fromDate\":\"2023-05-01 00:00:00\",\"toDate\":\"2023-05-31 23:59:59\"}";
		assertThat(send("POST", "/api/expense/find", june).body()).contains("\"id\":" + expense);
		assertThat(send("POST", "/api/expense/find", may2023).body()).isEqualTo("[]");
	}

	@Test
	void updateWithoutADateKeepsTheDate() throws Exception {

		HttpResponse<String> updated = update("{\"id\":" + expense + ",\"amount\":99}");

		assertThat(updated.statusCode()).isEqualTo(200);
		assertThat(dateOf(updated)).isEqualTo(Instant.parse("2023-05-03T08:42:12Z"));
		assertThat(dateOf(send("GET", "/api/expense?id=" + expense, null))).isEqualTo(Instant.parse("2023-05-03T08:42:12Z"));
	}

	@Test
	void updateRefusesADateThatIsNotOne() throws Exception {

		assertThat(update("{\"id\":" + expense + ",\"expenseDate\":\"yesterday\"}").statusCode()).isEqualTo(400);
		assertThat(update("{\"id\":" + expense + ",\"expenseDate\":\"2024-02-30 10:00:00\"}").statusCode()).isEqualTo(400);
		assertThat(dateOf(send("GET", "/api/expense?id=" + expense, null))).isEqualTo(Instant.parse("2023-05-03T08:42:12Z"));
	}

	private HttpResponse<String> update(String body) throws Exception {

		return send("PATCH", "/api/expense", body);
	}

	private Instant dateOf(HttpResponse<String> response) throws Exception {

		JsonNode date = objectMapper.readTree(response.body()).get("expenseDate");
		assertThat(date.isNull()).as("expenseDate in " + response.body()).isFalse();
		return OffsetDateTime.parse(date.asText()).toInstant();
	}

	private long idOf(HttpResponse<String> response) throws Exception {

		assertThat(response.statusCode()).isIn(200, 201);
		return objectMapper.readTree(response.body()).get("id").asLong();
	}

	private String signupAndLogin() throws Exception {

		String username = "user-" + UUID.randomUUID();
		String signup = "{\"name\":\"Some One\",\"username\":\"" + username + "\",\"password\":\"secret\"}";
		HttpResponse<String> signedUp = http.send(request("POST", "/api/auth/signup", null, signup).build(), HttpResponse.BodyHandlers.ofString());
		assertThat(signedUp.statusCode()).isEqualTo(200);
		HttpResponse<String> login = http.send(request("POST", "/api/auth/login", null, "{\"username\":\"" + username + "\",\"password\":\"secret\"}").build(), HttpResponse.BodyHandlers.ofString());
		assertThat(login.statusCode()).isEqualTo(200);
		return objectMapper.readTree(login.body()).get("token").asText();
	}

	private HttpResponse<String> send(String method, String path, String body) throws Exception {

		return http.send(request(method, path, token, body).build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpRequest.Builder request(String method, String path, String token, String body) {

		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
												 .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		if (body != null) {
			builder.header("Content-Type", "application/json");
		}
		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}
		return builder;
	}
}
