package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.aspect.AppendUser;
import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.DuplicateCategoryException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
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
	@ResponseBody
	@AppendUser
	public CategoryResponse createCategory(@Valid @RequestBody CategoryRequest categoryRequest) throws DuplicateCategoryException, UserNotFoundException {

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

		return categoryService.findById(id);
	}

	@PostMapping(path = "/find")
	@ResponseStatus(HttpStatus.OK)
	@AppendUser
	@ResponseBody
	public List<CategoryResponse> findCategories(@Valid @RequestBody CategoryListRequest request) throws CategoryNotFoundException {

		return categoryService.find(request);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public CategoryResponse delete(@Valid @RequestParam Long id) throws CategoryNotFoundException {

		return categoryService.deleteCategory(id);
	}
}
