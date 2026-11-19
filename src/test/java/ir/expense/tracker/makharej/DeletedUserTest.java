package ir.expense.tracker.makharej;


import com.fasterxml.jackson.databind.ObjectMapper;
import ir.expense.tracker.makharej.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;


/**
 * A user who deleted his account must not be able to keep using the application.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class DeletedUserTest {

	@LocalServerPort
	private int port;

	@Autowired
	private UserRepository userRepository;

	private final HttpClient http = HttpClient.newBuilder().version(HttpClient.Version.HTTP_1_1).build();
	private final ObjectMapper objectMapper = new ObjectMapper();

	private String username;
	private String token;
	private long userId;

	private String otherUsername;
	private String otherToken;

	@BeforeEach
	void createAndDeleteAUser() throws Exception {

		otherUsername = "other-" + UUID.randomUUID();
		signup(otherUsername);
		otherToken = token(login(otherUsername));

		username = "deleted-" + UUID.randomUUID();
		userId = signup(username);
		token = token(login(username));
		assertThat(send("POST", "/api/category/find", token, "{}").statusCode()).isEqualTo(200);

		HttpResponse<String> deletion = send("DELETE", "/api/user?id=" + userId, token, null);
		assertThat(deletion.statusCode()).isEqualTo(200);
	}

	@Test
	void theTokenStopsWorking() throws Exception {

		HttpResponse<String> response = send("POST", "/api/category/find", token, "{}");

		assertThat(response.statusCode()).isEqualTo(401);
		assertThat(response.body()).isEmpty();
	}

	@Test
	void loginIsRefusedLikeForAnUnknownUser() throws Exception {

		HttpResponse<String> deletedUser = login(username);
		HttpResponse<String> unknownUser = login("nobody-" + UUID.randomUUID());

		assertThat(deletedUser.statusCode()).isEqualTo(unknownUser.statusCode()).isNotEqualTo(200);
		assertThat(deletedUser.body()).isEqualTo(unknownUser.body()).doesNotContain("token");
	}

	@Test
	void theUsernameCannotBeRegisteredAgain() throws Exception {

		HttpResponse<String> signup = send("POST", "/api/auth/signup", null, signupJson(username));

		assertThat(signup.statusCode()).isEqualTo(500);
		assertThat(userRepository.findAll().stream().filter(user -> username.equals(user.getUsername()))).hasSize(1);
		assertThat(login(username).statusCode()).isNotEqualTo(200);
	}

	@Test
	void otherUsersAreNotAffected() throws Exception {

		assertThat(send("POST", "/api/category/find", otherToken, "{}").statusCode()).isEqualTo(200);
		assertThat(login(otherUsername).statusCode()).isEqualTo(200);
	}

	private long signup(String name) throws Exception {

		HttpResponse<String> response = send("POST", "/api/auth/signup", null, signupJson(name));
		assertThat(response.statusCode()).isEqualTo(200);
		return objectMapper.readTree(response.body()).get("id").asLong();
	}

	private HttpResponse<String> login(String name) throws Exception {

		return send("POST", "/api/auth/login", null, "{\"username\":\"" + name + "\",\"password\":\"secret\"}");
	}

	private String token(HttpResponse<String> login) throws Exception {

		assertThat(login.statusCode()).isEqualTo(200);
		return objectMapper.readTree(login.body()).get("token").asText();
	}

	private String signupJson(String name) {

		return "{\"name\":\"Some One\",\"username\":\"" + name + "\",\"password\":\"secret\"}";
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
