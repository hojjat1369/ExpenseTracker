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

		Mockito.when(categoryService.findCategoryById(categoryId, userId)).thenReturn(category);
		Mockito.when(expenseRepository.save(Mockito.any())).thenReturn(expense);
		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(expense);

		ExpenseResponse expenseResponse = expenseService.updateExpense(request, userId);

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

		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(null);

		Assertions.assertThatThrownBy(() -> {
			expenseService.updateExpense(request, userId);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}

	@Test
	public void deleteExpense() throws CategoryNotFoundException, ExpenseNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(expenseRepository.save(Mockito.any())).thenReturn(expense);
		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(expense);

		ExpenseResponse expenseResponse = expenseService.deleteExpense(123L, userId);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());
	}

	@Test
	public void deleteExpenseIdNotFound() throws ExpenseNotFoundException {

		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(null);

		Assertions.assertThatThrownBy(() -> {
			expenseService.deleteExpense(123L, userId);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}

	@Test
	public void findById() throws ExpenseNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();

		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(expense);

		ExpenseResponse expenseResponse = expenseService.findById(123L, userId);

		Assertions.assertThat(name).isEqualTo(expenseResponse.getName());
		Assertions.assertThat(note).isEqualTo(expenseResponse.getNote());
		Assertions.assertThat(expenseDate).isEqualTo(expenseResponse.getExpenseDate());
		Assertions.assertThat(tag).isEqualTo(expenseResponse.getTag());
		Assertions.assertThat(categoryId).isEqualTo(expenseResponse.getCategoryId());
		Assertions.assertThat(amount).isEqualTo(expenseResponse.getAmount());

	}

	@Test
	public void findByIdNotFound() throws ExpenseNotFoundException {

		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(null);

		Assertions.assertThatThrownBy(() -> {
			expenseService.findById(123L, userId);
		}).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}

	@Test
	public void expenseOfAnotherUserIsNotFound() {

		Long anotherUserId = 444L;
		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, anotherUserId)).thenReturn(null);
		ExpenseUpdateRequest request = ExpenseUpdateRequest.builder().id(expenseId).name("hijacked").amount(1.0).build();

		Assertions.assertThatThrownBy(() -> expenseService.findById(expenseId, anotherUserId)).isInstanceOf(ExpenseNotFoundException.class).hasMessage(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
		Assertions.assertThatThrownBy(() -> expenseService.updateExpense(request, anotherUserId)).isInstanceOf(ExpenseNotFoundException.class);
		Assertions.assertThatThrownBy(() -> expenseService.deleteExpense(expenseId, anotherUserId)).isInstanceOf(ExpenseNotFoundException.class);

		Mockito.verify(expenseRepository, Mockito.times(3)).findByIdAndUserId(expenseId, anotherUserId);
		Mockito.verify(expenseRepository, Mockito.never()).findById(Mockito.any());
		Mockito.verify(expenseRepository, Mockito.never()).save(Mockito.any());
	}

	@Test
	public void expenseCannotBeMovedIntoACategoryOfAnotherUser() throws CategoryNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();
		ExpenseUpdateRequest request = ExpenseUpdateRequest.builder().id(expenseId).categoryId(999L).build();
		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(expense);
		Mockito.when(categoryService.findCategoryById(999L, userId)).thenThrow(new CategoryNotFoundException());

		Assertions.assertThatThrownBy(() -> expenseService.updateExpense(request, userId)).isInstanceOf(CategoryNotFoundException.class);

		Mockito.verify(expenseRepository, Mockito.never()).save(Mockito.any());
		Assertions.assertThat(expense.getCategory()).isSameAs(category);
	}

	@Test
	public void unknownCallerCanNotFindAnyExpense() {

		Assertions.assertThatThrownBy(() -> expenseService.findExpenseById(expenseId, null)).isInstanceOf(ExpenseNotFoundException.class);

		Mockito.verifyNoInteractions(expenseRepository);
	}

	@Test
	public void updateWithoutADateKeepsTheDate() throws CategoryNotFoundException, ExpenseNotFoundException {

		Category category = Category.builder().name("sampleCategoryName").build();
		category.setId(categoryId);
		Expense expense = Expense.builder().name(name).expenseDate(expenseDate).amount(amount).tag(tag).note(note).category(category).build();
		ExpenseUpdateRequest request = ExpenseUpdateRequest.builder().id(expenseId).amount(7.5).build();
		Mockito.when(expenseRepository.findByIdAndUserId(expenseId, userId)).thenReturn(expense);

		ExpenseResponse response = expenseService.updateExpense(request, userId);

		Assertions.assertThat(response.getExpenseDate()).isEqualTo(expenseDate);
		Assertions.assertThat(expense.getExpenseDate()).isEqualTo(expenseDate);
		Assertions.assertThat(response.getAmount()).isEqualTo(7.5);
	}
}
