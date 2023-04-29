package ir.expense.tracker.makharej.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import java.util.Date;


@Entity
@Table(name = "expense")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Expense extends AbstractEntity {

	@Column(nullable = false)
	private String name;

	private Double amount;
	private String tag;
	private String note;
	private Date expenseDate;
}
