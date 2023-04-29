package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.dto.CategoryRequest;
import ir.expense.tracker.makharej.dto.CategoryResponse;
import ir.expense.tracker.makharej.dto.CategoryUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;


@Controller
@RequestMapping(path = "/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest categoryRequest)
	{

		return categoryService.createCategory(categoryRequest);
	}

	@PatchMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CategoryResponse updateCategory(@Valid @RequestBody CategoryUpdateRequest categoryUpdateRequest) throws CategoryNotFoundException {
		return categoryService.updateCategory(categoryUpdateRequest);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CategoryResponse findCategoryById(@Valid @RequestParam Long id) throws CategoryNotFoundException {

		Category category = categoryService.findCategoryById(id);
		return CategoryResponse.builder().name(category.getName()).id(category.getId()).build();
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CategoryResponse delete(@Valid @RequestParam Long id) throws CategoryNotFoundException {

		return categoryService.deleteCategory(id);
	}
}
