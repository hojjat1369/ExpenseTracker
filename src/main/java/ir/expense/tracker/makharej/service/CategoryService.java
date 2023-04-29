package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.dto.CategoryListRequest;
import ir.expense.tracker.makharej.dto.CategoryRequest;
import ir.expense.tracker.makharej.dto.CategoryResponse;
import ir.expense.tracker.makharej.dto.CategoryUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;

	public CategoryResponse createCategory(@Valid @NotNull CategoryRequest categoryRequest)
	{

		Category category = Category.builder().name(categoryRequest.getName()).build();
		categoryRepository.save(category);
		log.info("category {} is saved.", category.getId());
		return CategoryResponse.builder().name(category.getName()).id(category.getId()).build();
	}

	public CategoryResponse updateCategory(@Valid @NotNull CategoryUpdateRequest categoryUpdateRequest) throws CategoryNotFoundException {

		Category category = findCategoryById(categoryUpdateRequest.getId());
		category.setName(categoryUpdateRequest.getName());
		categoryRepository.save(category);
		log.info("category {} is updated.", category.getId());
		return CategoryResponse.builder().name(category.getName()).id(category.getId()).build();
	}

	public List<CategoryResponse> find(@NotNull CategoryListRequest categoryListRequest) {

		List<Category> categories = categoryRepository.findByNameContaining(categoryListRequest.getName());
		return categories.stream().map(this::toCategoryResponse).collect(Collectors.toList());
	}

	public CategoryResponse deleteCategory(Long id) throws CategoryNotFoundException {

		Category category = findCategoryById(id);
		category.setEnable(null);
		categoryRepository.save(category);
		log.info("category {} is deleted.", category.getId());
		return CategoryResponse.builder().name(category.getName()).id(category.getId()).build();
	}

	public Category findCategoryById(Long id) throws CategoryNotFoundException
	{
		return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
	}

	private CategoryResponse toCategoryResponse(Category category)
	{
		CategoryResponse.builder().id(category.getId()).name(category.getName()).build();
	}
}
