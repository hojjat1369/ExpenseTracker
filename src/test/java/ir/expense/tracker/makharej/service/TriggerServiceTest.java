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
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.List;


@ExtendWith(MockitoExtension.class)
public class TriggerServiceTest {

	private static final Long USER_ID = 333L;
	private static final Long OTHER_USER_ID = 444L;

	@Mock
	private ExpenseTriggerRepository triggerRepository;
	@Mock
	private CategoryService categoryService;
	@Mock
	private UserService userService;
	@Mock
	private ReportService reportService;

	private User user;

	@BeforeEach
	public void setup()
	{
		user = User.builder().username("testUsername").name("testName").build();
		user.setId(USER_ID);
	}

	@Test
	public void triggerIsSavedForTheCaller() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Category category = Category.builder().name("coffee").user(user).build();
		category.setId(7L);
		Mockito.when(categoryService.findCategoryById(7L, USER_ID)).thenReturn(category);
		Mockito.when(userService.findUserById(USER_ID)).thenReturn(user);
		givenTotal(0.0);
		TriggerRequest request = TriggerRequest.builder().name("coffee budget").period(ReportPeriod.MONTH).limit(50.0).tag("coffee").expenseName("latte").categoryId(7L).build();

		TriggerResponse response = service.createTrigger(request, USER_ID);

		ArgumentCaptor<ExpenseTrigger> saved = ArgumentCaptor.forClass(ExpenseTrigger.class);
		Mockito.verify(triggerRepository).save(saved.capture());
		Assertions.assertThat(saved.getValue().getUser()).isSameAs(user);
		Assertions.assertThat(saved.getValue().getCategory()).isSameAs(category);
		Assertions.assertThat(saved.getValue().getLimit()).isEqualTo(50.0);
		Assertions.assertThat(saved.getValue().getPeriod()).isEqualTo(ReportPeriod.MONTH);
		Assertions.assertThat(saved.getValue().getTag()).isEqualTo("coffee");
		Assertions.assertThat(saved.getValue().getExpenseName()).isEqualTo("latte");
		Assertions.assertThat(response.getName()).isEqualTo("coffee budget");
		Assertions.assertThat(response.getCategoryId()).isEqualTo(7L);
	}

	@Test
	public void triggerCannotUseACategoryOfAnotherUser() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(categoryService.findCategoryById(7L, USER_ID)).thenThrow(new CategoryNotFoundException());
		TriggerRequest request = TriggerRequest.builder().name("coffee budget").period(ReportPeriod.MONTH).limit(50.0).categoryId(7L).build();

		Assertions.assertThatThrownBy(() -> service.createTrigger(request, USER_ID)).isInstanceOf(CategoryNotFoundException.class);

		Mockito.verify(triggerRepository, Mockito.never()).save(Mockito.any());
	}

	@Test
	public void unknownCallerCannotCreateOrListTriggers() {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		TriggerRequest request = TriggerRequest.builder().name("coffee budget").period(ReportPeriod.MONTH).limit(50.0).build();

		Assertions.assertThatThrownBy(() -> service.createTrigger(request, null)).isInstanceOf(UserNotFoundException.class);
		Assertions.assertThat(service.find(new TriggerListRequest(), null)).isEmpty();

		Mockito.verifyNoInteractions(triggerRepository);
	}

	@Test
	public void monthlyTriggerLooksAtTheCurrentTehranMonth() throws Exception {

		// 2023-05-15 in Tehran (UTC+03:30): May starts at 2023-04-30T20:30:00Z and ends at 2023-05-31T20:29:59.999Z
		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger(1L, ReportPeriod.MONTH, 50.0, "coffee"));
		givenTotal(12.0);

		TriggerResponse response = service.findById(1L, USER_ID);

		ReportRequest asked = askedReport();
		Assertions.assertThat(asked.getPeriod()).isEqualTo(ReportPeriod.MONTH);
		Assertions.assertThat(asked.getFromDate()).isEqualTo(Date.from(Instant.parse("2023-04-30T20:30:00Z")));
		Assertions.assertThat(asked.getToDate()).isEqualTo(Date.from(Instant.parse("2023-05-31T20:29:59.999Z")));
		Assertions.assertThat(asked.getTag()).isEqualTo("coffee");
		Assertions.assertThat(response.getCurrentPeriod()).isEqualTo("2023-05");
		Assertions.assertThat(response.getCurrentAmount()).isEqualTo(12.0);
	}

	@Test
	public void yearlyTriggerLooksAtTheCurrentTehranYear() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger(1L, ReportPeriod.YEAR, 500.0, null));
		givenTotal(0.0);

		TriggerResponse response = service.findById(1L, USER_ID);

		ReportRequest asked = askedReport();
		Assertions.assertThat(asked.getFromDate()).isEqualTo(Date.from(Instant.parse("2022-12-31T20:30:00Z")));
		Assertions.assertThat(asked.getToDate()).isEqualTo(Date.from(Instant.parse("2023-12-31T20:29:59.999Z")));
		Assertions.assertThat(response.getCurrentPeriod()).isEqualTo("2023");
	}

	@Test
	public void theMonthChangesAtMidnightInTehranNotInUtc() throws Exception {

		// 21:00Z on the 31st of January is 00:30 on the 1st of February in Tehran
		TriggerService service = serviceAt("2023-01-31T21:00:00Z");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger(1L, ReportPeriod.MONTH, 50.0, null));
		givenTotal(0.0);

		Assertions.assertThat(service.findById(1L, USER_ID).getCurrentPeriod()).isEqualTo("2023-02");
	}

	@Test
	public void alarmGoesOffOnlyAboveTheLimit() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger(1L, ReportPeriod.MONTH, 50.0, null));

		givenTotal(49.99);
		Assertions.assertThat(service.findById(1L, USER_ID).getTriggered()).isFalse();
		givenTotal(50.0);
		Assertions.assertThat(service.findById(1L, USER_ID).getTriggered()).isFalse();
		givenTotal(50.01);
		Assertions.assertThat(service.findById(1L, USER_ID).getTriggered()).isTrue();
	}

	@Test
	public void triggerOfAnotherUserIsNotFound() {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, OTHER_USER_ID)).thenReturn(null);
		TriggerUpdateRequest update = TriggerUpdateRequest.builder().id(1L).name("x").period(ReportPeriod.YEAR).limit(1.0).build();

		Assertions.assertThatThrownBy(() -> service.findById(1L, OTHER_USER_ID)).isInstanceOf(TriggerNotFoundException.class);
		Assertions.assertThatThrownBy(() -> service.updateTrigger(update, OTHER_USER_ID)).isInstanceOf(TriggerNotFoundException.class);
		Assertions.assertThatThrownBy(() -> service.deleteTrigger(1L, OTHER_USER_ID)).isInstanceOf(TriggerNotFoundException.class);
		Assertions.assertThatThrownBy(() -> service.findById(1L, null)).isInstanceOf(TriggerNotFoundException.class);

		Mockito.verify(triggerRepository, Mockito.never()).save(Mockito.any());
		Mockito.verify(triggerRepository, Mockito.never()).findById(Mockito.any());
	}

	@Test
	public void updateReplacesTheRule() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		ExpenseTrigger trigger = trigger(1L, ReportPeriod.MONTH, 50.0, "coffee");
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger);
		givenTotal(30.0);
		TriggerUpdateRequest update = TriggerUpdateRequest.builder().id(1L).name("renamed").period(ReportPeriod.YEAR).limit(20.0).build();

		TriggerResponse response = service.updateTrigger(update, USER_ID);

		Assertions.assertThat(trigger.getName()).isEqualTo("renamed");
		Assertions.assertThat(trigger.getPeriod()).isEqualTo(ReportPeriod.YEAR);
		Assertions.assertThat(trigger.getLimit()).isEqualTo(20.0);
		Assertions.assertThat(trigger.getTag()).isNull();
		Assertions.assertThat(trigger.getCategory()).isNull();
		Assertions.assertThat(response.getTriggered()).isTrue();
		Mockito.verify(triggerRepository).save(trigger);
	}

	@Test
	public void deleteSwitchesTheTriggerOff() throws Exception {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		ExpenseTrigger trigger = trigger(1L, ReportPeriod.MONTH, 50.0, null);
		Mockito.when(triggerRepository.findActiveByIdAndUserId(1L, USER_ID)).thenReturn(trigger);
		givenTotal(0.0);

		service.deleteTrigger(1L, USER_ID);

		Assertions.assertThat(trigger.getEnable()).isNull();
		Mockito.verify(triggerRepository).save(trigger);
	}

	@Test
	public void listCanBeNarrowedByNameAndByAlarmState() {

		TriggerService service = serviceAt("2023-05-15T10:00:00Z");
		Mockito.when(triggerRepository.findActiveByUserId(USER_ID)).thenReturn(List.of(
				trigger(1L, ReportPeriod.MONTH, 50.0, "coffee"),
				trigger(2L, ReportPeriod.MONTH, 50.0, "taxi")));
		Mockito.when(reportService.report(Mockito.any(), Mockito.eq(USER_ID))).thenAnswer(invocation -> {
			ReportRequest asked = invocation.getArgument(0);
			return ReportResponse.builder().totalAmount("coffee".equals(asked.getTag()) ? 80.0 : 10.0).build();
		});

		Assertions.assertThat(service.find(new TriggerListRequest(), USER_ID)).extracting(TriggerResponse::getId).containsExactly(1L, 2L);
		Assertions.assertThat(service.find(TriggerListRequest.builder().triggered(true).build(), USER_ID)).extracting(TriggerResponse::getId).containsExactly(1L);
		Assertions.assertThat(service.find(TriggerListRequest.builder().triggered(false).build(), USER_ID)).extracting(TriggerResponse::getId).containsExactly(2L);
		Assertions.assertThat(service.find(TriggerListRequest.builder().name("trigger-2").build(), USER_ID)).extracting(TriggerResponse::getId).containsExactly(2L);
	}

	private TriggerService serviceAt(String instant) {

		Clock clock = Clock.fixed(Instant.parse(instant), ZoneOffset.UTC);
		return new TriggerService(triggerRepository, categoryService, userService, reportService, clock);
	}

	private void givenTotal(double total) {

		Mockito.when(reportService.report(Mockito.any(), Mockito.any())).thenReturn(ReportResponse.builder().totalAmount(total).count(1L).build());
	}

	private ReportRequest askedReport() {

		ArgumentCaptor<ReportRequest> asked = ArgumentCaptor.forClass(ReportRequest.class);
		Mockito.verify(reportService).report(asked.capture(), Mockito.eq(USER_ID));
		return asked.getValue();
	}

	private ExpenseTrigger trigger(Long id, ReportPeriod period, double limit, String tag) {

		ExpenseTrigger trigger = ExpenseTrigger.builder().name("trigger-" + id).period(period).limit(limit).tag(tag).user(user).build();
		trigger.setId(id);
		return trigger;
	}
}
