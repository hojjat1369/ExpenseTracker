package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.dto.expense.ExpenseListRequest;
import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import ir.expense.tracker.makharej.dto.report.ReportPeriodResponse;
import ir.expense.tracker.makharej.dto.report.ReportRequest;
import ir.expense.tracker.makharej.dto.report.ReportResponse;
import ir.expense.tracker.makharej.entity.Expense;
import ir.expense.tracker.makharej.repository.ExpenseRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;


/**
 * Monthly / yearly totals of the expenses of one user.
 * <p>
 * Months and years are calendar (Gregorian) ones, counted in Tehran time, the time zone the dates of the API are
 * written in. Deleted expenses and expenses without a date are not part of a report.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class ReportService {

	public static final ZoneId ZONE = ZoneId.of("Asia/Tehran");

	private final ExpenseRepository expenseRepository;

	public ReportResponse report(@Valid @NotNull ReportRequest request, Long userId) {

		ReportPeriod period = request.getPeriod();
		List<Expense> expenses = userId == null ? List.of() : expenseRepository.searchExpenses(toSearch(request, userId));

		TreeMap<YearMonth, Totals> totalsByPeriod = new TreeMap<>();
		Totals overall = new Totals();
		for (Expense expense : expenses) {
			if (!Boolean.TRUE.equals(expense.getEnable()) || expense.getExpenseDate() == null) {
				continue;
			}
			Totals totals = totalsByPeriod.computeIfAbsent(bucketOf(expense.getExpenseDate(), period), key -> new Totals());
			totals.add(expense.getAmount());
			overall.add(expense.getAmount());
		}

		List<ReportPeriodResponse> periods = new ArrayList<>();
		if (!totalsByPeriod.isEmpty()) {
			YearMonth last = totalsByPeriod.lastKey();
			for (YearMonth bucket = totalsByPeriod.firstKey(); !bucket.isAfter(last); bucket = next(bucket, period)) {
				Totals totals = totalsByPeriod.getOrDefault(bucket, new Totals());
				periods.add(ReportPeriodResponse.builder().period(label(bucket, period)).totalAmount(totals.amount()).count(totals.count).build());
			}
		}
		return ReportResponse.builder().period(period).totalAmount(overall.amount()).count(overall.count).periods(periods).build();
	}

	private ExpenseListRequest toSearch(ReportRequest request, Long userId) {

		ExpenseListRequest search = ExpenseListRequest.builder()
													  .name(request.getName())
													  .tag(request.getTag())
													  .fromDate(request.getFromDate())
													  .toDate(request.getToDate())
													  .categoryId(request.getCategoryId())
													  .build();
		search.setUserId(userId);
		return search;
	}

	/** Months and years are both kept as a YearMonth; a year is its January. */
	private YearMonth bucketOf(Date date, ReportPeriod period) {

		YearMonth month = YearMonth.from(Instant.ofEpochMilli(date.getTime()).atZone(ZONE));
		return period == ReportPeriod.YEAR ? YearMonth.of(month.getYear(), 1) : month;
	}

	private YearMonth next(YearMonth bucket, ReportPeriod period) {

		return period == ReportPeriod.YEAR ? bucket.plusYears(1) : bucket.plusMonths(1);
	}

	private String label(YearMonth bucket, ReportPeriod period) {

		return period == ReportPeriod.YEAR ? String.valueOf(bucket.getYear()) : bucket.toString();
	}

	/** Adds up as BigDecimal so that 0.1 + 0.2 is 0.3. */
	private static class Totals {

		private BigDecimal sum = BigDecimal.ZERO;
		private long count;

		void add(Double amount) {

			count++;
			if (amount != null) {
				sum = sum.add(BigDecimal.valueOf(amount));
			}
		}

		Double amount() {

			return sum.doubleValue();
		}
	}
}
