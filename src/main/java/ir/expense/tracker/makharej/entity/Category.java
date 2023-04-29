package ir.expense.tracker.makharej.entity;


import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;


@Entity
@Table(name = "category", uniqueConstraints = @UniqueConstraint(columnNames = { "name", "enalbe"}))
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {

	@Column(nullable = false)
	private String name;
}
