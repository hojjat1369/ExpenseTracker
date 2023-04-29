package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.messages.ErrorMessages;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.entity.Category;
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
	private CategoryService categoryService;
	private CategoryRequest categoryRequest;

	@BeforeEach
	public void setup()
	{
		categoryService = new CategoryService(categoryRepository);
		categoryRequest = CategoryRequest.builder().name("categoryName").build();
	}

	@Test
	public void createCategoryWithDuplicateName() throws DuplicateCategoryException {
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.findByName(Mockito.any())).thenReturn(Category.builder().name(categoryRequest.getName()).build());

		Assertions.assertThatThrownBy(() -> {
			categoryService.createCategory(categoryRequest);
		}).isInstanceOf(DuplicateCategoryException.class).hasMessage(ErrorMessages.DUPLICATE_CATEGORY_NAME);
	}

	@Test
	public void okCreateCategory() throws DuplicateCategoryException {
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.save(Mockito.any())).thenReturn(category);
		Mockito.when(categoryRepository.findByName(categoryRequest.getName())).thenReturn(null);

		CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);

		Assertions.assertThat(categoryResponse.getName()).isEqualTo(categoryRequest.getName());
	}

	@Test
	public void findCategoryById() throws CategoryNotFoundException {
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.findById(123l)).thenReturn(Optional.of(category));

		Category categoryById = categoryService.findCategoryById(123l);

		Assertions.assertThat(categoryById.getName()).isEqualTo(category.getName());
	}

	@Test
	public void findCategoryByIdNotFound() throws CategoryNotFoundException {
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.findById(123l)).thenReturn(Optional.ofNullable(null));

		Assertions.assertThatThrownBy(() -> {
			categoryService.findCategoryById(123l);
		}).isInstanceOf(CategoryNotFoundException.class).hasMessage(ErrorMessages.CATEGORY_NOT_FOUND_EXCEPTION);

	}
}
