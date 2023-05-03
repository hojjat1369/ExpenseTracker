package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.ExpenseNotFoundException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseRequest;
import ir.expense.tracker.makharej.dto.expense.ExpenseResponse;
import ir.expense.tracker.makharej.dto.expense.ExpenseUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.Expense;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import ir.expense.tracker.makharej.repository.ExpenseRepository;
import ir.expense.tracker.makharej.repository.ExpenseRepositoryImpl;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExpenseServiceTest {

	@Mock
	private ExpenseRepository expenseRepository;
	@Mock
	private CategoryService categoryService;
	@Mock
	private UserService userService;

	private ExpenseService expenseService;
	private final Date expenseDate = Calendar.getInstance().getTime();
	private final Double amount = 5000.0;
	private final String tag = "sampleExpenseTag";
	private final String note = "sampleExpenseNote";
	private final String name = "sampleExpenseName";
	private final Long categoryId = 123L;
	private final Long expenseId = 123L;
	private final Long userId=123L;
	private User user;

	@BeforeEach
	public void setup()
	{
		expenseService = new ExpenseService(expenseRepository, categoryService, userService);
		user = User.builder().name("testName").username("testUsername").build();
	}

	@Test
	public void createExpenseOk() throws CategoryNotFoundException, UserNotFoundException {

		ExpenseRequest request = ExpenseRequest.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).categoryId(categoryId).build();
		request.setUserId(userId);
		Category category = Category.builder().name("sampleCategoryName").user(user).build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(categoryService.findCategoryByIdAndUserId(categoryId, userId)).thenReturn(category);
		Mockito.when(expenseRepository.save(Mockito.any())).thenReturn(expense);
		Mockito.when(userService.findUserById(Mockito.anyLong())).thenReturn(user);

		ExpenseResponse expenseResponse = expenseService.createExpense(request);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());
	}

	@Test
	public void updateExpenseOk() throws CategoryNotFoundException, ExpenseNotFoundException {

		ExpenseUpdateRequest request = ExpenseUpdateRequest.builder().id(expenseId).name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).categoryId(categoryId).build();
		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(categoryService.findCategoryById(categoryId)).thenReturn(category);
		Mockito.when(expenseRepository.save(Mockito.any())).thenReturn(expense);
		Mockito.when(expenseRepository.findById(expenseId)).thenReturn(Optional.ofNullable(expense));

		ExpenseResponse expenseResponse = expenseService.updateExpense(request);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());
	}

	@Test
	public void updateExpenseIdNotFound() throws CategoryNotFoundException, ExpenseNotFoundException {

		ExpenseUpdateRequest request = ExpenseUpdateRequest.builder().id(expenseId).name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).categoryId(categoryId).build();
		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(expenseRepository.findById(categoryId)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			expenseService.updateExpense(request);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}

	@Test
	public void deleteExpense() throws CategoryNotFoundException, ExpenseNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(expenseRepository.save(Mockito.any())).thenReturn(expense);
		Mockito.when(expenseRepository.findById(expenseId)).thenReturn(Optional.ofNullable(expense));

		ExpenseResponse expenseResponse = expenseService.deleteExpense(123L);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());
	}

	@Test
	public void deleteExpenseIdNotFound() throws ExpenseNotFoundException {

		Mockito.when(expenseRepository.findById(categoryId)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			expenseService.deleteExpense(123L);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}

	@Test
	public void findById() throws ExpenseNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(expenseRepository.findById(expenseId)).thenReturn(Optional.ofNullable(expense));

		ExpenseResponse expenseResponse = expenseService.findById(123L);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());

	}

	@Test
	public void findByIdNotFound() throws ExpenseNotFoundException {

		Mockito.when(expenseRepository.findById(categoryId)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			expenseService.findById(123L);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}
}
