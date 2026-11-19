package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.common.exception.CategoryNotFoundException;
import ir.expense.tracker.makharej.common.exception.TriggerNotFoundException;
import ir.expense.tracker.makharej.common.exception.UserNotFoundException;
import ir.expense.tracker.makharej.dto.trigger.TriggerListRequest;
import ir.expense.tracker.makharej.dto.trigger.TriggerRequest;
import ir.expense.tracker.makharej.dto.trigger.TriggerResponse;
import ir.expense.tracker.makharej.dto.trigger.TriggerUpdateRequest;
import ir.expense.tracker.makharej.service.TriggerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;


@Controller
@RequestMapping(path = "/api/trigger")
@RequiredArgsConstructor
public class TriggerController {

	private final TriggerService triggerService;

	@PostMapping
	@ResponseStatus(HttpStatus.CREATED)
	@ResponseBody
	public TriggerResponse createTrigger(@Valid @RequestBody TriggerRequest request, @AuthenticationPrincipal(expression = "id") Long userId) throws CategoryNotFoundException, UserNotFoundException {

		return triggerService.createTrigger(request, userId);
	}

	@PatchMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public TriggerResponse updateTrigger(@Valid @RequestBody TriggerUpdateRequest request, @AuthenticationPrincipal(expression = "id") Long userId) throws TriggerNotFoundException, CategoryNotFoundException {

		return triggerService.updateTrigger(request, userId);
	}

	@GetMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public TriggerResponse findTriggerById(@Valid @RequestParam Long id, @AuthenticationPrincipal(expression = "id") Long userId) throws TriggerNotFoundException {

		return triggerService.findById(id, userId);
	}

	@PostMapping(path = "/find")
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public List<TriggerResponse> findTriggers(@Valid @RequestBody TriggerListRequest request, @AuthenticationPrincipal(expression = "id") Long userId) {

		return triggerService.find(request, userId);
	}

	@DeleteMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public TriggerResponse delete(@Valid @RequestParam Long id, @AuthenticationPrincipal(expression = "id") Long userId) throws TriggerNotFoundException {

		return triggerService.deleteTrigger(id, userId);
	}
}
