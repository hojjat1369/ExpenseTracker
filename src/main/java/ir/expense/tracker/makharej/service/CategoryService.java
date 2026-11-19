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

import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class CategoryService {

	private final CategoryRepository categoryRepository;
	private final UserService userService;

	public CategoryResponse createCategory(@Valid @NotNull CategoryRequest request) throws DuplicateCategoryException, UserNotFoundException {

		Category categoryByName = findCategoryByNameAndUserId(request.getName(), request.getUserId());
		if(categoryByName != null)
		{
			throw new DuplicateCategoryException();
		}
		User user = userService.findUserById(request.getUserId());
		Category category = Category.builder().name(request.getName()).user(user).build();
		categoryRepository.save(category);
		log.info("category {} is saved.", category.getId());
		return toCategoryResponse(category);
	}

	public CategoryResponse updateCategory(@Valid @NotNull CategoryUpdateRequest categoryUpdateRequest, Long userId) throws CategoryNotFoundException {

		Category category = findCategoryById(categoryUpdateRequest.getId(), userId);
		category.setName(categoryUpdateRequest.getName());
		categoryRepository.save(category);
		log.info("category {} is updated.", category.getId());
		return toCategoryResponse(category);
	}

	public List<CategoryResponse> find(@NotNull CategoryListRequest categoryListRequest) {

		List<Category> categories = categoryRepository.findByNameContainingAndUserId(categoryListRequest.getName(), categoryListRequest.getUserId());
		return categories.stream().map(this	::toCategoryResponse).collect(Collectors.toList());
	}

	public CategoryResponse deleteCategory(Long id, Long userId) throws CategoryNotFoundException {

		Category category = findCategoryById(id, userId);
		category.setEnable(null);
		categoryRepository.save(category);
		log.info("category {} is deleted.", category.getId());
		return toCategoryResponse(category);
	}

	public CategoryResponse findById(Long id, Long userId) throws CategoryNotFoundException
	{
		return toCategoryResponse(findCategoryById(id, userId));
	}

	/**
	 * Finds a category of the given user. A category that belongs to somebody else is reported as not found.
	 */
	public Category findCategoryById(Long id, Long userId) throws CategoryNotFoundException
	{
		Category category = userId == null ? null : categoryRepository.findByIdAndUserId(id, userId);
		if(category == null)
		{
			throw new CategoryNotFoundException();
		}
		return category;
	}

	public Category findCategoryByNameAndUserId(String name, Long userId)
	{
		return categoryRepository.findByNameAndUserId(name, userId);
	}

	public Category findCategoryByIdAndUserId(Long id, Long userId)
	{
		return categoryRepository.findByIdAndUserId(id, userId);
	}

	private CategoryResponse toCategoryResponse(Category category)
	{
		return CategoryResponse.builder().id(category.getId()).name(category.getName()).build();
	}
}
