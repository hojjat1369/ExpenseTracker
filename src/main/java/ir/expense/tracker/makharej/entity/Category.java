package ir.expense.tracker.makharej.entity;


import lombok.*;

import javax.persistence.Entity;
import javax.persistence.Table;


@Entity
@Table(name = "category")
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Category extends AbstractEntity {

	private String name;
}
