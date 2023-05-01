package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class DuplicateCategoryException extends DomainException {

	public DuplicateCategoryException()
	{
		super(ErrorMessages.DUPLICATE_CATEGORY_NAME);
	}
}
