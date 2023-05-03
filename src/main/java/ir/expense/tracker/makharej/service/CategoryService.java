package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final UserService userService;

	@Transactional
	public CategoryResponse createCategory(@Valid @NotNull CategoryRequest request) throws DuplicateCategoryException, UserNotFoundException {

		Category categoryByName = findCategoryByNameAndUserId(request.getName(), request.getUserId());
		User user = userService.findUserById(request.getUserId());
		if(categoryByName != null)
		{
			throw new DuplicateCategoryException();
		}
		Category category = Category.builder().name(request.getName()).user(user).build();
		categoryRepository.save(category);
		log.info("category {} is saved.", category.getId());
		return toCategoryResponse(category);
	}

	public CategoryResponse updateCategory(@Valid @NotNull CategoryUpdateRequest categoryUpdateRequest) throws CategoryNotFoundException {

		Category category = findCategoryById(categoryUpdateRequest.getId());
		category.setName(categoryUpdateRequest.getName());
		categoryRepository.save(category);
		log.info("category {} is updated.", category.getId());
		return toCategoryResponse(category);
	}

	public List<CategoryResponse> find(@NotNull CategoryListRequest categoryListRequest) {

		List<Category> categories = categoryRepository.findByNameContainingAndUserId(categoryListRequest.getName(), categoryListRequest.getUserId());
		return categories.stream().map(this::toCategoryResponse).collect(Collectors.toList());
	}

	public CategoryResponse deleteCategory(Long id) throws CategoryNotFoundException {

		Category category = findCategoryById(id);
		category.setEnable(null);
		categoryRepository.save(category);
		log.info("category {} is deleted.", category.getId());
		return toCategoryResponse(category);
	}

	public CategoryResponse findById(Long id) throws CategoryNotFoundException
	{
		return toCategoryResponse(categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new));
	}
	public Category findCategoryById(Long id) throws CategoryNotFoundException
	{
		return categoryRepository.findById(id).orElseThrow(CategoryNotFoundException::new);
	}

	public Category findCategoryByNameAndUserId(String name, Long userId)
	{
		return categoryRepository.findByNameAndUserId(name, userId);
	}

	private CategoryResponse toCategoryResponse(Category category)
	{
		return CategoryResponse.builder().id(category.getId()).name(category.getName()).build();
	}
}
