package ir.expense.tracker.makharej.repository;


import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ExpenseRepository extends JpaRepository<Expense, Long> {

	@Query("Select e from Expense e where e.name like :name")
	public List<Expense> findByNameContaining(@Param("name") String name);
}
