package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.service.CategoryService;
import ir.expense.tracker.makharej.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping(path = "/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public UserResponse createUser(@Valid @RequestBody UserRequest request) {

		return userService.createUser(request);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public UserResponse findUserById(@Valid @RequestParam Long id) throws UserNotFoundException {

		return userService.findById(id);
	}

	@GetMapping(path = "/find/all")
	@ResponseStatus(HttpStatus.OK)
	public List<UserResponse> findAllUsers() {

		return userService.findAllUsers();
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public UserResponse delete(@Valid @RequestParam Long id) throws UserNotFoundException {

		return userService.deleteUser(id);
	}
}
