package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.dto.user.LoginRequest;
import ir.expense.tracker.makharej.dto.user.UserRequest;
import ir.expense.tracker.makharej.dto.user.UserResponse;
import ir.expense.tracker.makharej.service.CategoryService;
import ir.expense.tracker.makharej.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;


@Controller
@RequestMapping(path = "/api/user")
@RequiredArgsConstructor
public class UserController {

	private final UserService userService;

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserResponse findUserById(@Valid @RequestParam Long id, @AuthenticationPrincipal(expression = "id") Long userId) throws UserNotFoundException {

		return userService.findById(id, userId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public UserResponse delete(@Valid @RequestParam Long id, @AuthenticationPrincipal(expression = "id") Long userId) throws UserNotFoundException {

		return userService.deleteUser(id, userId);
	}
}
