package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.UserRepository;
import ir.expense.tracker.makharej.security.AppUserDetails;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;


@ExtendWith(MockitoExtension.class)
public class UserDetailServiceImplTest {

	@Mock
	private UserRepository userRepository;
	private UserDetailServiceImpl userDetailService;
	private User user;

	@BeforeEach
	public void setup()
	{
		userDetailService = new UserDetailServiceImpl(userRepository);
		user = User.builder().username("testUsername").name("testName").password("encoded").build();
		user.setId(333L);
	}

	@Test
	public void activeUserIsLoadedWithHisId() {

		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(user);

		UserDetails userDetails = userDetailService.loadUserByUsername("testUsername");

		Assertions.assertThat(userDetails).isInstanceOf(AppUserDetails.class);
		Assertions.assertThat(((AppUserDetails) userDetails).getId()).isEqualTo(333L);
		Assertions.assertThat(userDetails.getUsername()).isEqualTo("testUsername");
	}

	@Test
	public void deletedUserIsUnknown() {

		user.setEnable(null);
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(user);

		Assertions.assertThatThrownBy(() -> userDetailService.loadUserByUsername("testUsername")).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	public void disabledUserIsUnknown() {

		user.setEnable(Boolean.FALSE);
		Mockito.when(userRepository.findByUsername("testUsername")).thenReturn(user);

		Assertions.assertThatThrownBy(() -> userDetailService.loadUserByUsername("testUsername")).isInstanceOf(UsernameNotFoundException.class);
	}

	@Test
	public void missingUserIsUnknown() {

		Assertions.assertThatThrownBy(() -> userDetailService.loadUserByUsername("nobody")).isInstanceOf(UsernameNotFoundException.class);
	}
}
