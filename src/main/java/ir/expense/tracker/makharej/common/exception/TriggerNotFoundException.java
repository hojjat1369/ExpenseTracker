package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class TriggerNotFoundException extends DomainException {

	public TriggerNotFoundException()
	{
		super(ErrorMessages.TRIGGER_NOT_FOUND_EXCEPTION);
	}
}
