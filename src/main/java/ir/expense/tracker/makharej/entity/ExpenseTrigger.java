package ir.expense.tracker.makharej.entity;


import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import jakarta.persistence.*;
import lombok.*;


/**
 * A budget rule of a user: the alarm goes off when the matching expenses of the current month (or year) add up to
 * more than the limit. The table is not called "trigger" because that is a reserved word.
 */
@Entity
@Table(name = "expense_trigger")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseTrigger extends AbstractEntity {

	@Column(nullable = false)
	private String name;

	@Enumerated(EnumType.STRING)
	@Column(name = "report_period", nullable = false)
	private ReportPeriod period;

	@Column(name = "limit_amount", nullable = false)
	private Double limit;

	@Column(name = "expense_name")
	private String expenseName;
	private String tag;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_fk")
	private Category category;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_fk", nullable = false)
	private User user;

}
