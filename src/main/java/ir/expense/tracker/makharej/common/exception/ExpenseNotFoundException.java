package ir.expense.tracker.makharej.common.exception;


import ir.expense.tracker.makharej.common.messages.ErrorMessages;


public class ExpenseNotFoundException extends Exception {

	public ExpenseNotFoundException()
	{
		super(ErrorMessages.Expense_NOT_FOUND_EXCEPTION);
	}
}
