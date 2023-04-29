package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.dto.CategoryRequest;
import ir.expense.tracker.makharej.dto.CategoryResponse;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;


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
}
