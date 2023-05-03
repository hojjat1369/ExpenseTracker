package ir.expense.tracker.makharej.entity;


import lombok.*;

import javax.persistence.*;


@Entity
@Table(name = "category", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "user_fk", "enable"}))
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {

	@Column(nullable = false)
	private String name;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_fk", nullable = false)
	private User user;

}
