package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.dto.category.CategoryListRequest;
import ir.expense.tracker.makharej.dto.category.CategoryRequest;
import ir.expense.tracker.makharej.dto.category.CategoryResponse;
import ir.expense.tracker.makharej.dto.category.CategoryUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;


@Controller
@RequestMapping(path = "/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest categoryRequest) throws DuplicateCategoryException {

		return categoryService.createCategory(categoryRequest);
	}

	@PatchMapping
	@ResponseStatus(HttpStatus.OK)
	public CategoryResponse updateCategory(@Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest) throws CategoryNotFoundException {
		return categoryService.updateCategory(categoryUpdateRequest);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	public CategoryResponse findCategoryById(@Valid @RequestParam Long id) throws CategoryNotFoundException {

		return categoryService.findById(id);
	}

	@GetMapping(path = "/find")
	@ResponseStatus(HttpStatus.OK)
	public List<CategoryResponse> findCategories(@RequestParam CategoryListRequest categoryListRequest) throws CategoryNotFoundException {

		return categoryService.find(categoryListRequest);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	public CategoryResponse delete(@Valid @RequestParam Long id) throws CategoryNotFoundException {

		return categoryService.deleteCategory(id);
	}
}
