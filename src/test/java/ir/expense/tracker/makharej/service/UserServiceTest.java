package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.UserRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

	@Mock
	private UserRepository userRepository;
	@Mock
	private PasswordEncoder encoder;
	private UserService userService;

	private final Long userId = 333L;
	private final Long anotherUserId = 444L;
	private User user;

	@BeforeEach
	public void setup()
	{
		userService = new UserService(userRepository, encoder);
		user = User.builder().username("testUsername").name("testName").build();
		user.setId(userId);
	}

	@Test
	public void userCanFindHimself() throws UserNotFoundException {

		Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		UserResponse response = userService.findById(userId, userId);

		Assertions.assertThat(response.getId()).isEqualTo(userId);
		Assertions.assertThat(response.getUsername()).isEqualTo("testUsername");
	}

	@Test
	public void userCanDeleteHimself() throws UserNotFoundException {

		Mockito.when(userRepository.findById(userId)).thenReturn(Optional.of(user));

		userService.deleteUser(userId, userId);

		Assertions.assertThat(user.getEnable()).isNull();
		Mockito.verify(userRepository).save(user);
	}

	@Test
	public void userCannotFindAnotherUser() {

		Assertions.assertThatThrownBy(() -> userService.findById(userId, anotherUserId))
				  .isInstanceOf(UserNotFoundException.class).hasMessage(ErrorMessages.USER_NOT_FOUND);

		Mockito.verifyNoInteractions(userRepository);
	}

	@Test
	public void userCannotDeleteAnotherUser() {

		Assertions.assertThatThrownBy(() -> userService.deleteUser(userId, anotherUserId))
				  .isInstanceOf(UserNotFoundException.class).hasMessage(ErrorMessages.USER_NOT_FOUND);

		Mockito.verifyNoInteractions(userRepository);
	}

	@Test
	public void unknownCallerCannotFindOrDeleteAnybody() {

		Assertions.assertThatThrownBy(() -> userService.findById(userId, null)).isInstanceOf(UserNotFoundException.class);
		Assertions.assertThatThrownBy(() -> userService.deleteUser(userId, null)).isInstanceOf(UserNotFoundException.class);

		Mockito.verifyNoInteractions(userRepository);
	}

	@Test
	public void usernameOfExistingOrDeletedUserCannotBeRegistered() {

		Mockito.when(userRepository.existsByUsername("testUsername")).thenReturn(true);
		UserRequest request = UserRequest.builder().username("testUsername").name("newName").password("secret").build();

		Assertions.assertThatThrownBy(() -> userService.createUser(request)).isInstanceOf(DataIntegrityViolationException.class);

		Mockito.verify(userRepository, Mockito.never()).save(Mockito.any());
	}

	@Test
	public void newUsernameCanBeRegistered() {

		Mockito.when(userRepository.existsByUsername("freshName")).thenReturn(false);
		Mockito.when(encoder.encode("secret")).thenReturn("encoded");
		UserRequest request = UserRequest.builder().username("freshName").name("newName").password("secret").build();

		UserResponse response = userService.createUser(request);

		Assertions.assertThat(response.getUsername()).isEqualTo("freshName");
		Mockito.verify(userRepository).save(Mockito.any());
	}
}
