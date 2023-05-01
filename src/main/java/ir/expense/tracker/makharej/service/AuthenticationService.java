package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.common.util.JwtUtil;
import ir.expense.tracker.makharej.dto.user.LoginRequest;
import ir.expense.tracker.makharej.dto.user.LoginResponse;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

	private final UserService userService;
	private final AuthenticationManager authenticationManager;

	private final JwtUtil jwtUtil;

	public LoginResponse login(LoginRequest request)
	{

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwtToken = jwtUtil.generateJwtToken(authentication);

		UserDetailsImpl userDetails = (UserDetailsImpl)authentication.getPrincipal();

		return LoginResponse.builder().username(userDetails.getUsername()).userId(userDetails.getId()).token(jwtToken).build();

	}
}
