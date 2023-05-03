package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class InvalidToken extends DomainException {

	public InvalidToken()
	{
		super(ErrorMessages.INVALID_TOKEN);
	}
}
