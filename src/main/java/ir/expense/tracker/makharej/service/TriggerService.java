package ir.expense.tracker.makharej.service;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.TriggerNotFoundException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.report.ReportPeriod;
import ir.expense.tracker.makharej.dto.report.ReportRequest;
import ir.expense.tracker.makharej.dto.report.ReportResponse;
import ir.expense.tracker.makharej.dto.trigger.TriggerListRequest;
import ir.expense.tracker.makharej.dto.trigger.TriggerRequest;
import ir.expense.tracker.makharej.dto.trigger.TriggerResponse;
import ir.expense.tracker.makharej.dto.trigger.TriggerUpdateRequest;
import ir.expense.tracker.makharej.entity.Category;
import ir.expense.tracker.makharej.entity.ExpenseTrigger;
import ir.expense.tracker.makharej.entity.User;
import ir.expense.tracker.makharej.repository.ExpenseTriggerRepository;
import jakarta.transaction.Transactional;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;


/**
 * Budget triggers. A trigger is "the expenses that match these filters add up to more than this limit within the
 * current month (or year)". Nothing is pushed anywhere: whether an alarm is going off is worked out whenever the
 * trigger is read.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TriggerService {

	private final ExpenseTriggerRepository triggerRepository;
	private final CategoryService categoryService;
	private final UserService userService;
	private final ReportService reportService;
	private final Clock clock;

	public TriggerResponse createTrigger(@Valid @NotNull TriggerRequest request, Long userId) throws CategoryNotFoundException, UserNotFoundException {

		if(userId == null)
		{
			throw new UserNotFoundException();
		}
		Category category = findCategory(request.getCategoryId(), userId);
		User user = userService.findUserById(userId);
		ExpenseTrigger trigger = ExpenseTrigger.builder()
											   .name(request.getName())
											   .period(request.getPeriod())
											   .limit(request.getLimit())
											   .expenseName(request.getExpenseName())
											   .tag(request.getTag())
											   .category(category)
											   .user(user)
											   .build();
		triggerRepository.save(trigger);
		log.info("trigger {} is saved.", trigger.getId());
		return toTriggerResponse(trigger, userId);
	}

	public TriggerResponse updateTrigger(@Valid @NotNull TriggerUpdateRequest request, Long userId) throws TriggerNotFoundException, CategoryNotFoundException {

		ExpenseTrigger trigger = findTriggerById(request.getId(), userId);
		trigger.setName(request.getName());
		trigger.setPeriod(request.getPeriod());
		trigger.setLimit(request.getLimit());
		trigger.setExpenseName(request.getExpenseName());
		trigger.setTag(request.getTag());
		trigger.setCategory(findCategory(request.getCategoryId(), userId));
		triggerRepository.save(trigger);
		log.info("trigger {} is updated.", trigger.getId());
		return toTriggerResponse(trigger, userId);
	}

	public TriggerResponse deleteTrigger(Long id, Long userId) throws TriggerNotFoundException {

		ExpenseTrigger trigger = findTriggerById(id, userId);
		trigger.setEnable(null);
		triggerRepository.save(trigger);
		log.info("trigger {} is deleted.", trigger.getId());
		return toTriggerResponse(trigger, userId);
	}

	public TriggerResponse findById(Long id, Long userId) throws TriggerNotFoundException {

		return toTriggerResponse(findTriggerById(id, userId), userId);
	}

	public List<TriggerResponse> find(@NotNull TriggerListRequest request, Long userId) {

		if(userId == null)
		{
			return List.of();
		}
		return triggerRepository.findActiveByUserId(userId).stream()
								.filter(trigger -> request.getName() == null || trigger.getName().contains(request.getName()))
								.map(trigger -> toTriggerResponse(trigger, userId))
								.filter(response -> request.getTriggered() == null || request.getTriggered().equals(response.getTriggered()))
								.toList();
	}

	/**
	 * Finds a trigger of the given user. A trigger that belongs to somebody else is reported as not found.
	 */
	public ExpenseTrigger findTriggerById(Long id, Long userId) throws TriggerNotFoundException {

		ExpenseTrigger trigger = userId == null ? null : triggerRepository.findActiveByIdAndUserId(id, userId);
		if(trigger == null)
		{
			throw new TriggerNotFoundException();
		}
		return trigger;
	}

	private Category findCategory(Long categoryId, Long userId) throws CategoryNotFoundException {

		return categoryId == null ? null : categoryService.findCategoryById(categoryId, userId);
	}

	private TriggerResponse toTriggerResponse(ExpenseTrigger trigger, Long userId) {

		ZonedDateTime now = ZonedDateTime.now(clock.withZone(ReportService.ZONE));
		ReportPeriod period = trigger.getPeriod();
		LocalDate firstDay = period == ReportPeriod.YEAR ? LocalDate.of(now.getYear(), 1, 1) : now.toLocalDate().withDayOfMonth(1);
		ZonedDateTime start = firstDay.atStartOfDay(ReportService.ZONE);
		ZonedDateTime end = period == ReportPeriod.YEAR ? start.plusYears(1) : start.plusMonths(1);

		ReportRequest currentPeriod = ReportRequest.builder()
												   .period(period)
												   .name(trigger.getExpenseName())
												   .tag(trigger.getTag())
												   .categoryId(trigger.getCategory() == null ? null : trigger.getCategory().getId())
												   .fromDate(Date.from(start.toInstant()))
												   .toDate(Date.from(end.toInstant().minusMillis(1)))
												   .build();
		ReportResponse report = reportService.report(currentPeriod, userId);

		Double amount = report.getTotalAmount();
		boolean triggered = BigDecimal.valueOf(amount).compareTo(BigDecimal.valueOf(trigger.getLimit())) > 0;
		return TriggerResponse.builder()
							  .id(trigger.getId())
							  .name(trigger.getName())
							  .period(period)
							  .limit(trigger.getLimit())
							  .expenseName(trigger.getExpenseName())
							  .tag(trigger.getTag())
							  .categoryId(trigger.getCategory() == null ? null : trigger.getCategory().getId())
							  .currentPeriod(period == ReportPeriod.YEAR ? String.valueOf(now.getYear()) : YearMonth.from(now).toString())
							  .currentAmount(amount)
							  .triggered(triggered)
							  .build();
	}
}
