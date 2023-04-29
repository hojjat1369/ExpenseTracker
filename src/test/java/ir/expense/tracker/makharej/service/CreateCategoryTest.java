package ir.expense.tracker.makharej.service;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;


@SpringBootTest
@ActiveProfiles("test")
public class CreateCategoryTest {

	@Autowired
	private CategoryService categoryService;


	@Test
	public void nullInput()
	{
		categoryService.createCategory(null);
	}
}
