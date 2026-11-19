package ir.expense.tracker.makharej;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * Talks to the running application over HTTP and pins down behaviour that is easy to lose when upgrading
 * Spring Boot / Spring Security / Hibernate (it was all verified against the Spring Boot 2.7 version).
 * Runs against the in-memory HSQLDB of the "test" profile.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ApiBehaviorTest {

	private static final String JSON = "application/json";

	@LocalServerPort
	private int port;

	@Autowired
	private JdbcTemplate jdbcTemplate;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Test
	void loginReturnsAJwtThatAuthenticatesLaterCalls() throws Exception {

		String token = signupAndLogin();

		assertThat(token.split("\\.")).hasSize(3);
		HttpResponse<String> response = send("POST", "/api/category/find", token, "{}");
		assertThat(response.statusCode()).isEqualTo(200);
		assertThat(response.body()).isEqualTo("[]");
	}

	@Test
	void entitiesAreOwnedByTheLoggedInUser() throws Exception {

		String owner = signupAndLogin();
		String someoneElse = signupAndLogin();

		HttpResponse<String> created = send("POST", "/api/category", owner, "{\"name\":\"food\"}");
		assertThat(created.statusCode()).isEqualTo(201);
		assertThat(send("POST", "/api/category/find", owner, "{\"name\":\"food\"}").body()).contains("\"name\":\"food\"");
		assertThat(send("POST", "/api/category/find", someoneElse, "{\"name\":\"food\"}").body()).isEqualTo("[]");
	}

	@Test
	void allEntitiesDrawTheirIdFromOneSharedSequence() {

		Integer sharedSequences = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.sequences where upper(sequence_name) = 'HIBERNATE_SEQUENCE'", Integer.class);
		Integer perEntitySequences = jdbcTemplate.queryForObject(
				"select count(*) from information_schema.sequences where upper(sequence_name) like '%\\_SEQ' escape '\\'", Integer.class);

		assertThat(sharedSequences).isEqualTo(1);
		assertThat(perEntitySequences).isZero();
	}

	@Test
	void requestsWithoutAValidTokenGet401WithAnEmptyBody() throws Exception {

		HttpResponse<String> noToken = send("GET", "/api/category?id=1", null, null);
		HttpResponse<String> badToken = send("GET", "/api/category?id=1", "abc.def.ghi", null);

		assertThat(noToken.statusCode()).isEqualTo(401);
		assertThat(noToken.body()).isEmpty();
		assertThat(badToken.statusCode()).isEqualTo(401);
		assertThat(badToken.body()).isEmpty();
	}

	@Test
	void unknownPathsOfAuthenticatedUsersGetTheBootErrorJson() throws Exception {

		HttpResponse<String> response = send("GET", "/api/does-not-exist", signupAndLogin(), null);

		assertThat(response.statusCode()).isEqualTo(404);
		JsonNode body = objectMapper.readTree(response.body());
		assertThat(body.get("status").asInt()).isEqualTo(404);
		assertThat(body.get("error").asText()).isEqualTo("Not Found");
		assertThat(body.get("path").asText()).isEqualTo("/api/does-not-exist");
	}

	@Test
	void domainAndValidationErrorsKeepTheirShape() throws Exception {

		String token = signupAndLogin();

		HttpResponse<String> invalid = send("POST", "/api/category", token, "{}");
		assertThat(invalid.statusCode()).isEqualTo(400);
		assertThat(objectMapper.readTree(invalid.body()).get("name").asText()).isEqualTo("name cannot be null/empty!");

		HttpResponse<String> notFound = send("GET", "/api/category?id=99999", token, null);
		assertThat(notFound.statusCode()).isEqualTo(400);
		JsonNode body = objectMapper.readTree(notFound.body());
		assertThat(body.get("statusCode").asText()).isEqualTo("BAD_REQUEST");
		assertThat(body.get("message").asText()).isEqualTo("category not found!");
	}

	@Test
	void frameworkErrorsHaveNoBody() throws Exception {

		String token = signupAndLogin();

		HttpResponse<String> malformed = send("POST", "/api/category", token, "{not json");
		HttpResponse<String> missingParameter = send("GET", "/api/category", token, null);
		HttpResponse<String> wrongMethod = send("GET", "/api/auth/signup", null, null);

		assertThat(malformed.statusCode()).isEqualTo(400);
		assertThat(malformed.body()).isEmpty();
		assertThat(missingParameter.statusCode()).isEqualTo(400);
		assertThat(missingParameter.body()).isEmpty();
		assertThat(wrongMethod.statusCode()).isEqualTo(405);
		assertThat(wrongMethod.body()).isEmpty();
	}

	@Test
	void trailingSlashStillMatches() throws Exception {

		String token = signupAndLogin();
		long id = objectMapper.readTree(send("POST", "/api/category", token, "{\"name\":\"slash\"}").body()).get("id").asLong();

		assertThat(send("GET", "/api/category/?id=" + id, token, null).statusCode()).isEqualTo(200);
		assertThat(send("POST", "/api/category/find/", token, "{\"name\":\"slash\"}").statusCode()).isEqualTo(200);
		assertThat(send("POST", "/api/auth/login/", null, "{\"username\":\"nobody\",\"password\":\"x\"}").statusCode()).isNotEqualTo(404);
	}

	@Test
	void onlyJsonIsSpoken() throws Exception {

		String token = signupAndLogin();
		long id = objectMapper.readTree(send("POST", "/api/category", token, "{\"name\":\"yaml\"}").body()).get("id").asLong();

		HttpRequest yamlResponse = request("GET", "/api/category?id=" + id, token, null, JSON).header("Accept", "application/yaml").build();
		HttpRequest yamlRequest = request("POST", "/api/auth/signup", null, "name: a\nusername: b\npassword: c\n", "application/yaml").build();

		assertThat(http.send(yamlResponse, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(406);
		assertThat(http.send(yamlRequest, HttpResponse.BodyHandlers.ofString()).statusCode()).isEqualTo(415);
	}

	private String signupAndLogin() throws Exception {

		String username = "user-" + UUID.randomUUID();
		String credentials = "{\"username\":\"" + username + "\",\"password\":\"secret\"}";

		HttpResponse<String> signup = send("POST", "/api/auth/signup", null, "{\"name\":\"Some One\",\"username\":\"" + username + "\",\"password\":\"secret\"}");
		assertThat(signup.statusCode()).isEqualTo(200);
		HttpResponse<String> login = send("POST", "/api/auth/login", null, credentials);
		assertThat(login.statusCode()).isEqualTo(200);
		return objectMapper.readTree(login.body()).get("token").asText();
	}

	private HttpResponse<String> send(String method, String path, String token, String body) throws Exception {

		return http.send(request(method, path, token, body, JSON).build(), HttpResponse.BodyHandlers.ofString());
	}

	private HttpRequest.Builder request(String method, String path, String token, String body, String contentType) {

		HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create("http://localhost:" + port + path))
												 .method(method, body == null ? HttpRequest.BodyPublishers.noBody() : HttpRequest.BodyPublishers.ofString(body));
		if (body != null) {
			builder.header("Content-Type", contentType);
		}
		if (token != null) {
			builder.header("Authorization", "Bearer " + token);
		}
		return builder;
	}
}
