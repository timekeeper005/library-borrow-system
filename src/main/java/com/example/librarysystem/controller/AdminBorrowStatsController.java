package com.example.librarysystem.controller;

import com.example.librarysystem.dto.BorrowStatsResult;
import com.example.librarysystem.service.AdminBorrowStatsService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/admin/stats")
public class AdminBorrowStatsController {

    private final AdminBorrowStatsService adminBorrowStatsService;

    public AdminBorrowStatsController(AdminBorrowStatsService adminBorrowStatsService) {
        this.adminBorrowStatsService = adminBorrowStatsService;
    }

    @PostMapping("/borrow-counts")
    public ResponseEntity<AdminBorrowStatsService.TaskStatusResponse> createStats(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to
    ) {
        String taskId = adminBorrowStatsService.triggerBorrowStats(from, to);
        return ResponseEntity.accepted().body(new AdminBorrowStatsService.TaskStatusResponse(taskId, "RUNNING", List.of()));
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<AdminBorrowStatsService.TaskStatusResponse> getStats(@PathVariable String taskId) {
        return ResponseEntity.ok(adminBorrowStatsService.getTaskStatus(taskId));
    }
}
