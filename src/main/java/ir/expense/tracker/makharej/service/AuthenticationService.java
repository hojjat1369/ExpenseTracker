package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.util.JwtUtil;
import ir.expense.tracker.makharej.dto.user.LoginRequest;
import ir.expense.tracker.makharej.dto.user.LoginResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;


@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {

	private final AuthenticationManager authenticationManager;
	private final JwtUtil jwtUtil;

	public LoginResponse login(LoginRequest request)
	{

		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

		SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwtToken = jwtUtil.generateJwtToken(authentication);

		UserDetails userDetails = (UserDetails)authentication.getPrincipal();

		return LoginResponse.builder().username(userDetails.getUsername()).token(jwtToken).build();

	}
}
