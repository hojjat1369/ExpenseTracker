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
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

	private final UserRepository userRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

		User user = findUserByUsername(username);
		if(user == null)
		{
			log.error("user {} not found!", username);
			throw new UsernameNotFoundException(ErrorMessages.USER_NOT_FOUND);
		}
		return new org.springframework.security.core.userdetails.User(user.getUsername(), user.getPassword(), new ArrayList<>());
	}

	public UserResponse createUser(@Valid @NotNull UserRequest request) {


		User user = User.builder()
				.username(request.getUsername())
				.name(request.getName())
				.password(request.getPassword())
				.build();
		userRepository.save(user);
		log.info("user {} is saved.", user.getId());
		return toUserResponse(user);
	}

	public UserResponse deleteUser(Long id) throws UserNotFoundException {

		User user = findUserById(id);
		user.setEnable(null);
		userRepository.save(user);
		log.info("user {} is deleted.", user.getId());
		return toUserResponse(user);
	}

	public UserResponse findById(Long id) throws UserNotFoundException {
		return toUserResponse(findUserById(id));
	}
	public User findUserById(Long id) throws UserNotFoundException
	{
		return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
	}
	public User findUserByUsername(String username)
	{
		return userRepository.findByUsername(username);
	}

	public List<UserResponse> findAllUsers()
	{
		return userRepository.findAll().stream().map(user -> toUserResponse(user)).toList();
	}

	private UserResponse toUserResponse(User user)
	{
		return UserResponse.builder().id(user.getId()).name(user.getName()).username(user.getUsername()).build();
	}
}
