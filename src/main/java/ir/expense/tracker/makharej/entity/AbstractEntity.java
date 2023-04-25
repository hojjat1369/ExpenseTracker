package ir.expense.tracker.makharej.entity;


import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;


@MappedSuperclass
@Getter
@Setter
public class AbstractEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Long id;
	private Boolean enable;
	@Column(name = "creation_time")
	private Date creationTime;

	public AbstractEntity() {

		super();
		creationTime = new Date();
		enable = Boolean.TRUE;
	}
}
