package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class CategoryNotFoundException extends Exception {

	public CategoryNotFoundException()
	{
		super(ErrorMessages.CATEGORY_NOT_FOUND_EXCEPTION);
	}
}
