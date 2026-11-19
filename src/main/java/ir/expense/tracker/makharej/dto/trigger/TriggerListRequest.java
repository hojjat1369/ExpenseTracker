package ir.expense.tracker.makharej.dto.trigger;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;


@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TriggerListRequest {

	/** Part of the name of the trigger. */
	private String name;

	/** true: only the alarms that are going off now, false: only the quiet ones. */
	private Boolean triggered;
}
