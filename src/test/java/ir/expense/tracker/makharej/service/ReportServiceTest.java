package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.dto.expense.ExpenseListRequest;
import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import ir.expense.tracker.makharej.dto.report.ReportPeriodResponse;
import ir.expense.tracker.makharej.dto.report.ReportRequest;
import ir.expense.tracker.makharej.dto.report.ReportResponse;
import ir.expense.tracker.makharej.entity.Expense;
import ir.expense.tracker.makharej.repository.ExpenseRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Date;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class ReportServiceTest {

	private static final Long USER_ID = 333L;

	@Mock
	private ExpenseRepository expenseRepository;
	private ReportService reportService;

	@BeforeEach
	public void setup()
	{
		reportService = new ReportService(expenseRepository);
	}

	@Test
	public void monthsAreTehranMonths() {

		// Tehran is UTC+03:30 all year: 20:00Z is still the 31st there, 21:00Z is already the 1st of the next month.
		givenExpenses(
				expense("2023-01-31T20:00:00Z", 10.0),
				expense("2023-01-31T21:00:00Z", 5.0));

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getPeriod).containsExactly("2023-01", "2023-02");
		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getTotalAmount).containsExactly(10.0, 5.0);
	}

	@Test
	public void yearsAreTehranYears() {

		givenExpenses(
				expense("2022-12-31T20:29:59Z", 1.0),
				expense("2022-12-31T20:30:00Z", 2.0),
				expense("2023-07-01T00:00:00Z", 4.0));

		ReportResponse report = reportService.report(request(ReportPeriod.YEAR), USER_ID);

		Assertions.assertThat(report.getPeriod()).isEqualTo(ReportPeriod.YEAR);
		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getPeriod).containsExactly("2022", "2023");
		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getTotalAmount).containsExactly(1.0, 6.0);
		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getCount).containsExactly(1L, 2L);
	}

	@Test
	public void totalsCoverEveryPeriod() {

		givenExpenses(expense("2023-01-10T10:00:00Z", 100.5), expense("2023-03-10T10:00:00Z", 20.0), expense("2023-03-11T10:00:00Z", 0.25));

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getTotalAmount()).isEqualTo(120.75);
		Assertions.assertThat(report.getCount()).isEqualTo(3L);
	}

	@Test
	public void monthsWithoutExpensesBetweenTheFirstAndTheLastAreListedWithZero() {

		givenExpenses(expense("2023-03-10T10:00:00Z", 20.0), expense("2023-01-10T10:00:00Z", 100.0));

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getPeriod).containsExactly("2023-01", "2023-02", "2023-03");
		Assertions.assertThat(report.getPeriods().get(1).getTotalAmount()).isEqualTo(0.0);
		Assertions.assertThat(report.getPeriods().get(1).getCount()).isEqualTo(0L);
	}

	@Test
	public void yearsWithoutExpensesAreListedWithZero() {

		givenExpenses(expense("2021-05-10T10:00:00Z", 1.0), expense("2023-05-10T10:00:00Z", 2.0));

		ReportResponse report = reportService.report(request(ReportPeriod.YEAR), USER_ID);

		Assertions.assertThat(report.getPeriods()).extracting(ReportPeriodResponse::getPeriod).containsExactly("2021", "2022", "2023");
	}

	@Test
	public void deletedExpensesAreLeftOut() {

		Expense deleted = expense("2023-01-10T10:00:00Z", 1000.0);
		deleted.setEnable(null);
		givenExpenses(expense("2023-01-11T10:00:00Z", 5.0), deleted);

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getTotalAmount()).isEqualTo(5.0);
		Assertions.assertThat(report.getCount()).isEqualTo(1L);
	}

	@Test
	public void expensesWithoutDateBelongToNoPeriodAndWithoutAmountAddNothing() {

		Expense undated = Expense.builder().name("undated").amount(50.0).build();
		Expense withoutAmount = expense("2023-01-10T10:00:00Z", null);
		givenExpenses(undated, withoutAmount, expense("2023-01-11T10:00:00Z", 7.0));

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getPeriods()).hasSize(1);
		Assertions.assertThat(report.getPeriods().get(0).getTotalAmount()).isEqualTo(7.0);
		Assertions.assertThat(report.getPeriods().get(0).getCount()).isEqualTo(2L);
	}

	@Test
	public void amountsAreAddedWithoutFloatingPointNoise() {

		givenExpenses(expense("2023-01-10T10:00:00Z", 0.1), expense("2023-01-11T10:00:00Z", 0.2));

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getTotalAmount()).isEqualTo(0.3);
		Assertions.assertThat(report.getPeriods().get(0).getTotalAmount()).isEqualTo(0.3);
	}

	@Test
	public void noExpensesGiveAnEmptyReport() {

		givenExpenses();

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), USER_ID);

		Assertions.assertThat(report.getPeriods()).isEmpty();
		Assertions.assertThat(report.getTotalAmount()).isEqualTo(0.0);
		Assertions.assertThat(report.getCount()).isEqualTo(0L);
	}

	@Test
	public void filtersAreHandedToTheExpenseSearchForTheCallerOnly() {

		givenExpenses();
		Date from = Date.from(Instant.parse("2023-01-01T00:00:00Z"));
		Date to = Date.from(Instant.parse("2023-12-31T00:00:00Z"));
		ReportRequest request = ReportRequest.builder().period(ReportPeriod.YEAR).name("lunch").tag("#daily").categoryId(7L).fromDate(from).toDate(to).build();

		reportService.report(request, USER_ID);

		ArgumentCaptor<ExpenseListRequest> search = ArgumentCaptor.forClass(ExpenseListRequest.class);
		Mockito.verify(expenseRepository).searchExpenses(search.capture());
		Assertions.assertThat(search.getValue().getUserId()).isEqualTo(USER_ID);
		Assertions.assertThat(search.getValue().getName()).isEqualTo("lunch");
		Assertions.assertThat(search.getValue().getTag()).isEqualTo("#daily");
		Assertions.assertThat(search.getValue().getCategoryId()).isEqualTo(7L);
		Assertions.assertThat(search.getValue().getFromDate()).isEqualTo(from);
		Assertions.assertThat(search.getValue().getToDate()).isEqualTo(to);
	}

	@Test
	public void unknownCallerGetsAnEmptyReportWithoutTouchingTheDatabase() {

		ReportResponse report = reportService.report(request(ReportPeriod.MONTH), null);

		Assertions.assertThat(report.getPeriods()).isEmpty();
		Mockito.verifyNoInteractions(expenseRepository);
	}

	private void givenExpenses(Expense... expenses) {

		Mockito.when(expenseRepository.searchExpenses(Mockito.any())).thenReturn(List.of(expenses));
	}

	private ReportRequest request(ReportPeriod period) {

		return ReportRequest.builder().period(period).build();
	}

	private Expense expense(String instant, Double amount) {

		return Expense.builder().name("expense").amount(amount).expenseDate(Date.from(Instant.parse(instant))).build();
	}
}
