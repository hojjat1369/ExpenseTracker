package ir.expense.tracker.makharej.entity;


import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;


@Entity
@Table(name = "user", uniqueConstraints = @UniqueConstraint(columnNames = { "username", "enable"}))
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User extends AbstractEntity {

	@Column(nullable = false)
	private String username;
	private String name;
	private String password;
}
