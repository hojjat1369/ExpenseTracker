package ir.expense.tracker.makharej.repository;


import ir.expense.tracker.makharej.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

	@Query("Select c from Category c where c.name like :name and c.user.id=:userId")
	public List<Category> findByNameContainingAndUserId(@Param("name") String name, @Param("userId") Long userId);

	@Query("Select c from Category c where c.name=:name and c.user.id=:userId")
	public Category findByNameAndUserId(@Param("name") String name, @Param("userId") Long userId);
}
