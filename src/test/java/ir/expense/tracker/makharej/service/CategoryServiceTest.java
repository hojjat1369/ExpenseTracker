package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.dto.CategoryRequest;
import ir.expense.tracker.makharej.dto.CategoryResponse;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import org.aspectj.lang.annotation.Before;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;


public class CategoryServiceTest {

	private CategoryRepository categoryRepository = Mockito.mock(CategoryRepository.class);
	private CategoryService categoryService;
	private CategoryRequest categoryRequest;

	@BeforeEach
	public void setup()
	{
		categoryService = new CategoryService(categoryRepository);
		categoryRequest = CategoryRequest.builder().name("categoryName").build();
	}

	@Test
	void nullName() {
		CategoryService categoryService = new CategoryService(categoryRepository);
		categoryService.createCategory(null);
	}

	@Test
	public void ok()
	{
		Category category = Category.builder().name(categoryRequest.getName()).build();
		Mockito.when(categoryRepository.save(Mockito.any())).thenReturn(category);

		CategoryResponse categoryResponse = categoryService.createCategory(categoryRequest);

		Assertions.assertThat(categoryResponse.getName()).isEqualTo(categoryRequest.getName());
	}
}
