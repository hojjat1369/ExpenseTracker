package ir.expense.tracker.makharej.entity;


import lombok.*;

import javax.persistence.*;
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

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_fk")
	private Category category;

}
