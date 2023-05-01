package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.user.LoginRequest;
import ir.expense.tracker.makharej.dto.user.LoginResponse;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.service.AuthenticationService;
import ir.expense.tracker.makharej.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping(path = "/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

	private final AuthenticationService authenticationService;

	@PostMapping(path = "/login")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public LoginResponse login(@Valid @RequestBody LoginRequest request) {

		return authenticationService.login(request);
	}
}
