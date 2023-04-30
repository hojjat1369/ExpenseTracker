package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.ExpenseNotFoundException;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseResponse;
import ir.expense.tracker.makharej.dto.expense.ExpenseUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.service.CategoryService;
import ir.expense.tracker.makharej.service.ExpenseService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping(path = "/api/expense")
@RequiredArgsConstructor
public class ExpenseController {

	private final ExpenseService expenseService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public ExpenseResponse createExpense(@Valid @RequestBody ExpenseRequest request) throws CategoryNotFoundException {

		return expenseService.createExpense(request);
	}

	@PatchMapping
	@ResponseStatus(HttpStatus.OK)
	public ExpenseResponse updateExpense(@Valid @RequestBody ExpenseUpdateRequest request) throws ExpenseNotFoundException, CategoryNotFoundException {

		return expenseService.updateExpense(request);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public ExpenseResponse findExpenseById(@Valid @RequestParam Long id) throws ExpenseNotFoundException {

		return expenseService.findById(id);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public ExpenseResponse delete(@Valid @RequestParam Long id) throws ExpenseNotFoundException {

		return expenseService.deleteExpense(id);
	}
}
