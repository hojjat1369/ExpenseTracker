package ir.expense.tracker.makharej.repository;


import ir.expense.tracker.makharej.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("Select c from Category c where c.name like :place")
	public List<Category> findByNameContaining(@Param("name") String name);
	public Category findByName(String name);
}
