package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;


@ExtendWith(MockitoExtension.class)
public class CategoryServiceTest {

	@Mock
	private CategoryRepository categoryRepository;
	@Mock
	private UserService userService;
	private CategoryService categoryService;
	private CategoryRequest categoryRequest;

	private final Long userId = 333L;

	private User user;

	@BeforeEach
	public void setup()
	{
		categoryService = new CategoryService(categoryRepository, userService);
		categoryRequest = CategoryRequest.builder().name("categoryName").build();
		categoryRequest.setUserId(userId);
		user = User.builder().username("testUsername").name("testName").build();
	}

	@Test
	public void createCategoryWithDuplicateName() throws DuplicateCategoryException {
		Mockito.when(categoryRepository.findByNameAndUserId(categoryRequest.getName(), categoryRequest.getUserId())).thenReturn(Category.builder().name(categoryRequest.getName()).user(user).build());

		Assertions.assertThatThrownBy(() -> {
			categoryService.createCategory(categoryRequest);
		}).isInstanceOf(DuplicateCategoryException.class).hasMessage(ErrorMessages.DUPLICATE_CATEGORY_NAME);
	}

	@Test
	public void createCategoryOk() throws DuplicateCategoryException, UserNotFoundException {
		Category category = Category.builder().name(categoryRequest.getName()).user(user).build();
		Mockito.when(categoryRepository.save(Mockito.any())).thenReturn(category);
		Mockito.when(categoryRepository.findByNameAndUserId(categoryRequest.getName(), userId)).thenReturn(null);
		Mockito.when(userService.findUserById(userId)).thenReturn(user);

		CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);

		Assertions.assertThat(categoryResponse.getName()).isEqualTo(categoryRequest.getName());
	}

	@Test
	public void findCategoryByIdOk() throws CategoryNotFoundException {
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.of(category));

		Category categoryById = categoryService.findCategoryById(123L);

		Assertions.assertThat(categoryById.getName()).isEqualTo(category.getName());
	}

	@Test
	public void findCategoryByIdNotFound() throws CategoryNotFoundException {
		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			categoryService.findCategoryById(123L);
		}).isInstanceOf(CategoryNotFoundException.class).hasMessage(ErrorMessages.CATEGORY_NOT_FOUND_EXCEPTION);

	}

	@Test
	public void updateCategoryIdNotFound() throws CategoryNotFoundException {

		CategoryUpdateRequest request = CategoryUpdateRequest.builder().id(123L).name("updatedCategoryName").build();
		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			categoryService.updateCategory(request);
		}).isInstanceOf(CategoryNotFoundException.class).hasMessage(ErrorMessages.CATEGORY_NOT_FOUND_EXCEPTION);

	}

	@Test
	public void updateCategoryOk() throws CategoryNotFoundException {

		Category category = Category.builder().name(categoryRequest.getName()).build();
		CategoryUpdateRequest request = CategoryUpdateRequest.builder().id(123L).name("updatedCategoryName").build();
		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.ofNullable(category));

		CategoryResponse updatedCategory = categoryService.updateCategory(request);
		Assertions.assertThat(updatedCategory.getName()).isEqualTo(category.getName());

	}

	@Test
	public void deleteCategoryIdNotFound() throws CategoryNotFoundException {

		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			categoryService.deleteCategory(123L);
		}).isInstanceOf(CategoryNotFoundException.class).hasMessage(ErrorMessages.CATEGORY_NOT_FOUND_EXCEPTION);

	}

	@Test
	public void deleteCategoryOk() throws CategoryNotFoundException {

		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.findById(123L)).thenReturn(Optional.ofNullable(category));

		CategoryResponse deletedCategory = categoryService.deleteCategory(123L);
		Assertions.assertThat(deletedCategory.getName()).isEqualTo(category.getName());

	}
}
