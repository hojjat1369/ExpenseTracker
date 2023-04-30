package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class UserNotFoundException extends Exception {

	public UserNotFoundException()
	{
		super(ErrorMessages.USER_NOT_FOUND);
	}
}
