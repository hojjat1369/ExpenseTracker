package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final PasswordEncoder encoder;
	public UserResponse createUser(@Valid @NotNull UserRequest request) {

		// The database only rejects a name that is used by an active user ((username, enable) is unique and enable is
		// null once a user is deleted). A deleted user's name must stay taken: two rows with one username break login,
		// and tokens carry the username, so the old user's tokens would otherwise act as whoever registers it next.
		if(userRepository.existsByUsername(request.getUsername()))
		{
			throw new DataIntegrityViolationException("username " + request.getUsername() + " is already taken");
		}
		User user = User.builder()
				.username(request.getUsername())
				.name(request.getName())
				.password(encoder.encode(request.getPassword()))
				.build();
		userRepository.save(user);
		log.info("user {} is saved.", user.getId());
		return toUserResponse(user);
	}

	/**
	 * A user can only delete himself. Anybody else is reported as not found.
	 */
	public UserResponse deleteUser(Long id, Long callerId) throws UserNotFoundException {

		User user = findUserById(id, callerId);
		user.setEnable(null);
		userRepository.save(user);
		log.info("user {} is deleted.", user.getId());
		return toUserResponse(user);
	}

	/**
	 * A user can only look at himself. Anybody else is reported as not found.
	 */
	public UserResponse findById(Long id, Long callerId) throws UserNotFoundException {
		return toUserResponse(findUserById(id, callerId));
	}

	private User findUserById(Long id, Long callerId) throws UserNotFoundException
	{
		if(callerId == null || !callerId.equals(id))
		{
			throw new UserNotFoundException();
		}
		return findUserById(id);
	}
	public User findUserById(Long id) throws UserNotFoundException
	{
		return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
	}
	public User findUserByUsername(String username)
	{
		return userRepository.findByUsername(username);
	}

	private UserResponse toUserResponse(User user)
	{
		return UserResponse.builder().id(user.getId()).name(user.getName()).username(user.getUsername()).build();
	}
}
