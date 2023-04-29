package ir.expense.tracker.makharej.service;


import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;


public class CategoryServiceTest {

	@Test
	void nullName() {
		CategoryService categoryService = new CategoryService(null);
		categoryService.createCategory(null);
	}
}
