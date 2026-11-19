package ir.expense.tracker.makharej;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ir.expense.tracker.makharej.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * A user may only see and change their own categories and expenses. Somebody else's id must look exactly like an
 * id that does not exist.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OwnershipTest {

	private static final String CATEGORY_NOT_FOUND = "{\"statusCode\":\"BAD_REQUEST\",\"message\":\"category not found!\"}";
	private static final String EXPENSE_NOT_FOUND = "{\"statusCode\":\"BAD_REQUEST\",\"message\":\"expense not found!\"}";

	private static final String USER_NOT_FOUND = "{\"statusCode\":\"BAD_REQUEST\",\"message\":\"user not found!\"}";

	@LocalServerPort
	private int port;

	@Autowired
	private UserRepository userRepository;

	@Autowired
	@Qualifier("requestMappingHandlerMapping")
	private RequestMappingHandlerMapping handlerMapping;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String owner;
	private String attacker;
	private long ownerId;
	private long attackerId;
	private long categoryId;
	private long expenseId;
	private long attackerCategoryId;

	@BeforeEach
	void createDataForTwoUsers() throws Exception {

		String ownerName = "owner-" + UUID.randomUUID();
		String attackerName = "attacker-" + UUID.randomUUID();
		ownerId = idOf(send("POST", "/api/auth/signup", null, signupJson(ownerName)));
		attackerId = idOf(send("POST", "/api/auth/signup", null, signupJson(attackerName)));
		owner = login(ownerName);
		attacker = login(attackerName);
		categoryId = idOf(send("POST", "/api/category", owner, "{\"name\":\"food\"}"));
		expenseId = idOf(send("POST", "/api/expense", owner, expenseJson("lunch", categoryId)));
		attackerCategoryId = idOf(send("POST", "/api/category", attacker, "{\"name\":\"mine\"}"));
	}

	@Test
	void otherUsersCategoryCannotBeRead() throws Exception {

		HttpResponse<String> response = send("GET", "/api/category?id=" + categoryId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(CATEGORY_NOT_FOUND);
	}

	@Test
	void otherUsersCategoryCannotBeUpdated() throws Exception {

		HttpResponse<String> response = send("PATCH", "/api/category", attacker, "{\"id\":" + categoryId + ",\"name\":\"hacked\"}");

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(CATEGORY_NOT_FOUND);
		assertThat(send("GET", "/api/category?id=" + categoryId, owner, null).body()).contains("\"name\":\"food\"");
	}

	@Test
	void otherUsersCategoryCannotBeDeleted() throws Exception {

		HttpResponse<String> response = send("DELETE", "/api/category?id=" + categoryId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(CATEGORY_NOT_FOUND);
		assertThat(send("POST", "/api/category/find", owner, "{\"name\":\"food\"}").body()).contains("\"name\":\"food\"");
	}

	@Test
	void otherUsersExpenseCannotBeRead() throws Exception {

		HttpResponse<String> response = send("GET", "/api/expense?id=" + expenseId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(EXPENSE_NOT_FOUND);
	}

	@Test
	void otherUsersExpenseCannotBeUpdated() throws Exception {

		String update = "{\"id\":" + expenseId + ",\"name\":\"hacked\",\"amount\":1.0,\"tag\":\"hacked\",\"note\":\"hacked\",\"expenseDate\":\"2024-06-07 08:09\",\"categoryId\":" + attackerCategoryId + "}";

		HttpResponse<String> response = send("PATCH", "/api/expense", attacker, update);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(EXPENSE_NOT_FOUND);
		JsonNode untouched = objectMapper.readTree(send("GET", "/api/expense?id=" + expenseId, owner, null).body());
		assertThat(untouched.get("tag").asText()).isEqualTo("#daily");
		assertThat(untouched.get("categoryId").asLong()).isEqualTo(categoryId);
	}

	@Test
	void otherUsersExpenseCannotBeDeleted() throws Exception {

		HttpResponse<String> response = send("DELETE", "/api/expense?id=" + expenseId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(EXPENSE_NOT_FOUND);
		assertThat(send("POST", "/api/expense/find", owner, "{}").body()).contains("\"id\":" + expenseId);
	}

	@Test
	void ownExpenseCannotBeMovedIntoOtherUsersCategory() throws Exception {

		String move = "{\"id\":" + expenseId + ",\"categoryId\":" + attackerCategoryId + "}";

		HttpResponse<String> response = send("PATCH", "/api/expense", owner, move);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(CATEGORY_NOT_FOUND);
		assertThat(objectMapper.readTree(send("GET", "/api/expense?id=" + expenseId, owner, null).body()).get("categoryId").asLong()).isEqualTo(categoryId);
	}

	@Test
	void searchesStayScopedEvenIfTheBodyNamesAnotherUser() throws Exception {

		assertThat(send("POST", "/api/expense/find", attacker, "{\"userId\":" + ownerId + "}").body()).isEqualTo("[]");
		assertThat(send("POST", "/api/category/find", attacker, "{\"name\":\"food\",\"userId\":" + ownerId + "}").body()).isEqualTo("[]");
		assertThat(send("POST", "/api/category", attacker, "{\"name\":\"spoof\",\"userId\":" + (ownerId - 1) + "}").statusCode()).isEqualTo(201);
		assertThat(send("POST", "/api/category/find", owner, "{\"name\":\"spoof\"}").body()).isEqualTo("[]");
	}

	@Test
	void otherUserCannotBeDeleted() throws Exception {

		HttpResponse<String> response = send("DELETE", "/api/user?id=" + ownerId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(400);
		assertThat(response.body()).isEqualTo(USER_NOT_FOUND);
		assertThat(userRepository.findById(ownerId).orElseThrow().getEnable()).isTrue();
	}

	@Test
	void otherUserLooksExactlyLikeAUserThatDoesNotExist() throws Exception {

		HttpResponse<String> someoneElse = send("GET", "/api/user?id=" + ownerId, attacker, null);
		HttpResponse<String> nobody = send("GET", "/api/user?id=99999999", attacker, null);

		assertThat(someoneElse.statusCode()).isEqualTo(400);
		assertThat(someoneElse.body()).isEqualTo(USER_NOT_FOUND);
		assertThat(nobody.statusCode()).isEqualTo(someoneElse.statusCode());
		assertThat(nobody.body()).isEqualTo(someoneElse.body());
	}

	@Test
	void aUserCanReadHimself() throws Exception {

		HttpResponse<String> response = send("GET", "/api/user?id=" + ownerId, owner, null);

		assertThat(response.statusCode()).isEqualTo(200);
		JsonNode body = objectMapper.readTree(response.body());
		assertThat(body.get("id").asLong()).isEqualTo(ownerId);
		assertThat(body.get("username").asText()).startsWith("owner-");
		assertThat(body.has("password")).isFalse();
	}

	@Test
	void aUserCanDeleteHimself() throws Exception {

		HttpResponse<String> response = send("DELETE", "/api/user?id=" + attackerId, attacker, null);

		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(objectMapper.readTree(response.body()).get("id").asLong()).isEqualTo(attackerId);
		assertThat(userRepository.findById(attackerId).orElseThrow().getEnable()).isNull();
		assertThat(userRepository.findById(ownerId).orElseThrow().getEnable()).isTrue();
	}

	@Test
	void thereIsNoWayToListAllUsers() throws Exception {

		Set<String> mappedPaths = handlerMapping.getHandlerMethods().keySet().stream()
												.flatMap(mapping -> mapping.getDirectPaths().stream())
												.collect(Collectors.toSet());

		assertThat(mappedPaths).contains("/api/user");
		assertThat(mappedPaths).doesNotContain("/api/user/find/all");
	}

	@Test
	void theOwnerCanStillReadUpdateAndDelete() throws Exception {

		assertThat(send("GET", "/api/category?id=" + categoryId, owner, null).statusCode()).isEqualTo(200);
		assertThat(send("PATCH", "/api/category", owner, "{\"id\":" + categoryId + ",\"name\":\"groceries\"}").body()).contains("\"name\":\"groceries\"");
		assertThat(send("GET", "/api/expense?id=" + expenseId, owner, null).statusCode()).isEqualTo(200);
		long secondCategory = idOf(send("POST", "/api/category", owner, "{\"name\":\"travel\"}"));
		String update = "{\"id\":" + expenseId + ",\"amount\":7.5,\"categoryId\":" + secondCategory + "}";
		JsonNode updated = objectMapper.readTree(send("PATCH", "/api/expense", owner, update).body());
		assertThat(updated.get("amount").asDouble()).isEqualTo(7.5);
		assertThat(updated.get("categoryId").asLong()).isEqualTo(secondCategory);
		assertThat(send("DELETE", "/api/expense?id=" + expenseId, owner, null).statusCode()).isEqualTo(200);
		assertThat(send("DELETE", "/api/category?id=" + secondCategory, owner, null).statusCode()).isEqualTo(200);
	}

	private String expenseJson(String name, long category) {

		return "{\"name\":\"" + name + "\",\"amount\":12.5,\"expenseDate\":\"2023-05-03 12:12:12\",\"categoryId\":" + category + ",\"tag\":\"#daily\",\"note\":\"n\"}";
	}

	private String signupJson(String username) {

		return "{\"name\":\"Some One\",\"username\":\"" + username + "\",\"password\":\"secret\"}";
	}

	private String signupAndLogin() throws Exception {

		String username = "user-" + UUID.randomUUID();
		assertThat(send("POST", "/api/auth/signup", null, signupJson(username)).statusCode()).isEqualTo(200);
		return login(username);
	}

	private String login(String username) throws Exception {

		HttpResponse<String> login = send("POST", "/api/auth/login", null, "{\"username\":\"" + username + "\",\"password\":\"secret\"}");
		assertThat(login.statusCode()).isEqualTo(200);
		return objectMapper.readTree(login.body()).get("token").asText();
	}

	private long idOf(HttpResponse<String> response) throws Exception {

		assertThat(response.statusCode()).isIn(200, 201);
		return objectMapper.readTree(response.body()).get("id").asLong();
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
