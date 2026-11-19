package ir.expense.tracker.makharej.repository;


import ir.expense.tracker.makharej.entity.ExpenseTrigger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface ExpenseTriggerRepository extends JpaRepository<ExpenseTrigger, Long> {

	@Query("Select t from ExpenseTrigger t where t.id=:id and t.user.id=:userId and t.enable=true")
	public ExpenseTrigger findActiveByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

	@Query("Select t from ExpenseTrigger t where t.user.id=:userId and t.enable=true order by t.id")
	public List<ExpenseTrigger> findActiveByUserId(@Param("userId") Long userId);
}
