package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.ExpenseNotFoundException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.expense.ExpenseListRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseResponse;
import ir.expense.tracker.makharej.dto.expense.ExpenseUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.Expense;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ExpenseService {

	private final ExpenseRepository expenseRepository;
	private final CategoryService categoryService;
	private final UserService userService;

	public ExpenseResponse createExpense(@Valid @NotNull ExpenseRequest request) throws CategoryNotFoundException, UserNotFoundException {

		Category category = categoryService.findCategoryByIdAndUserId(request.getCategoryId(), request.getUserId());
		if(category == null)
		{
			throw new CategoryNotFoundException();
		}
		User user = userService.findUserById(request.getUserId());
		if(user == null){
			throw new UserNotFoundException();
		}
		Expense expense = Expense.builder()
								 .name(request.getName())
								 .expenseDate(request.getExpenseDate())
								 .amount(request.getAmount())
								 .category(category)
								 .note(request.getNote())
								 .tag(request.getTag())
								 .user(user)
								 .build();
		expenseRepository.save(expense);
		log.info("expense {} is saved.", expense.getId());
		return toExpenseResponse(expense);
	}

	public ExpenseResponse updateExpense(@Valid @NotNull ExpenseUpdateRequest request, Long userId) throws ExpenseNotFoundException, CategoryNotFoundException {

		Expense expense = findExpenseById(request.getId(), userId);
		expense.setName(expense.getName());
		if(request.getExpenseDate() != null){
			expense.setExpenseDate(request.getExpenseDate());
		}
		expense.setNote(request.getNote());
		expense.setTag(request.getTag());
		expense.setAmount(request.getAmount());
		if(request.getCategoryId() != null){
			Category category = categoryService.findCategoryById(request.getCategoryId(), userId);
			expense.setCategory(category);
		}
		expenseRepository.save(expense);
		log.info("expense {} is updated.", expense.getId());
		return toExpenseResponse(expense);
	}

	public ExpenseResponse deleteExpense(Long id, Long userId) throws ExpenseNotFoundException {

		Expense expense = findExpenseById(id, userId);
		expense.setEnable(null);
		expenseRepository.save(expense);
		log.info("expense {} is deleted.", expense.getId());
		return toExpenseResponse(expense);
	}

	/**
	 * Finds an expense of the given user. An expense that belongs to somebody else is reported as not found.
	 */
	public Expense findExpenseById(Long id, Long userId) throws ExpenseNotFoundException
	{
		Expense expense = userId == null ? null : expenseRepository.findByIdAndUserId(id, userId);
		if(expense == null)
		{
			throw new ExpenseNotFoundException();
		}
		return expense;
	}

	public ExpenseResponse findById(Long id, Long userId) throws ExpenseNotFoundException
	{
		return toExpenseResponse(findExpenseById(id, userId));
	}

	public List<ExpenseResponse> findAll(ExpenseListRequest request)
	{

		List<Expense> expenses = expenseRepository.searchExpenses(request);
		return expenses.stream().map(this::toExpenseResponse).toList();
	}

	private ExpenseResponse toExpenseResponse(Expense expense)
	{
		return ExpenseResponse.builder()
							  .id(expense.getId()).name(expense.getName())
							  .expenseDate(expense.getExpenseDate())
							  .tag(expense.getTag())
							  .amount(expense.getAmount())
							  .note(expense.getNote())
							  .categoryId(expense.getCategory().getId())
							  .build();
	}
}
