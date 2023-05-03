package ir.expense.tracker.makharej.repository;


import ir.expense.tracker.makharej.dto.expense.ExpenseListRequest;
import ir.expense.tracker.makharej.entity.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


public interface ExpenseRepositoryCustom {

	public List<Expense> searchExpenses(ExpenseListRequest request);

}
