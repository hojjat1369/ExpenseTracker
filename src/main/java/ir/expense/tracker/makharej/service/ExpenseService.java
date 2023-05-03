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

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
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

	public ExpenseResponse updateExpense(@Valid @NotNull ExpenseUpdateRequest request) throws ExpenseNotFoundException, CategoryNotFoundException {

		Expense expense = findExpenseById(request.getId());
		expense.setName(expense.getName());
		expense.setExpenseDate(request.getExpenseDate());
		expense.setNote(request.getNote());
		expense.setTag(request.getTag());
		expense.setAmount(request.getAmount());
		if(request.getCategoryId() != null){
			Category category = categoryService.findCategoryById(request.getCategoryId());
			expense.setCategory(category);
		}
		expenseRepository.save(expense);
		log.info("expense {} is updated.", expense.getId());
		return toExpenseResponse(expense);
	}

	public ExpenseResponse deleteExpense(Long id) throws ExpenseNotFoundException {

		Expense expense = findExpenseById(id);
		expense.setEnable(null);
		expenseRepository.save(expense);
		log.info("expense {} is deleted.", expense.getId());
		return toExpenseResponse(expense);
	}

	public Expense findExpenseById(Long id) throws ExpenseNotFoundException
	{
		return expenseRepository.findById(id).orElseThrow(ExpenseNotFoundException::new);
	}

	public ExpenseResponse findById(Long id) throws ExpenseNotFoundException
	{
		return toExpenseResponse(findExpenseById(id));
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
