package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class DomainException extends Exception {

	public DomainException(String message)
	{
		super(message);
	}
}
