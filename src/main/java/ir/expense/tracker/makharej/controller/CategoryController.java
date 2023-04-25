package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.dto.CategoryRequest;
import ir.expense.tracker.makharej.dto.CategoryResponse;
import ir.expense.tracker.makharej.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;


@Controller
@RequestMapping(path = "/api/category")
@RequiredArgsConstructor
public class CategoryController {

	private final CategoryService categoryService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	public CategoryResponse createCategory(CategoryRequest categoryRequest)
	{
		return categoryService.createCategory(categoryRequest);
	}
}
