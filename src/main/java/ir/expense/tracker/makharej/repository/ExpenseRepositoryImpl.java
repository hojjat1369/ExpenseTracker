package ir.expense.tracker.makharej.repository;


import com.querydsl.jpa.impl.JPAQuery;
import ir.expense.tracker.makharej.dto.expense.ExpenseListRequest;
import ir.expense.tracker.makharej.entity.Expense;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;

@Repository
public class ExpenseRepositoryImpl implements ExpenseRepositoryCustom{

	@PersistenceContext(type = PersistenceContextType.TRANSACTION)
	private EntityManager entityManager;

	public List<Expense> searchExpenses(ExpenseListRequest request)
	{
		CriteriaBuilder criteriaBuilder = entityManager.getCriteriaBuilder();
		CriteriaQuery<Expense> query = criteriaBuilder.createQuery(Expense.class);

		Root<Expense> expenseRoot = query.from(Expense.class);

		List<Predicate> predicates = new ArrayList<>();
		if(StringUtils.hasText(request.getName()))
		{
			predicates.add(criteriaBuilder.equal(expenseRoot.get("name"), request.getName()));
		}
		if(request.getUserId() != null)
		{
			predicates.add(criteriaBuilder.equal(expenseRoot.get("user").get("id"), request.getUserId()));
		}

		query.where(predicates.toArray(new Predicate[0]));

		return entityManager.createQuery(query).getResultList();

	}
}
