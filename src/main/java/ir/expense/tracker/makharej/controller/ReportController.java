package ir.expense.tracker.makharej.controller;


import ir.expense.tracker.makharej.dto.report.ReportRequest;
import ir.expense.tracker.makharej.dto.report.ReportResponse;
import ir.expense.tracker.makharej.service.ReportService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;


@Controller
@RequestMapping(path = "/api/report")
@RequiredArgsConstructor
public class ReportController {

	private final ReportService reportService;

	@PostMapping
	@ResponseStatus(HttpStatus.OK)
	@ResponseBody
	public ReportResponse report(@Valid @RequestBody ReportRequest request, @AuthenticationPrincipal(expression = "id") Long userId) {

		return reportService.report(request, userId);
	}
}
