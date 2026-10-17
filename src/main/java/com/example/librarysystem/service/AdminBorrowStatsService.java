package com.example.librarysystem.service;

import com.example.librarysystem.dto.BorrowStatsResult;
import com.example.librarysystem.repository.BorrowCountProjection;
import com.example.librarysystem.repository.BorrowRecordRepository;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

@Service
public class AdminBorrowStatsService {

    private final BorrowRecordRepository borrowRecordRepository;
    private final Executor statsTaskExecutor;
    private final Map<String, TaskState> tasks = new ConcurrentHashMap<>();

    public AdminBorrowStatsService(BorrowRecordRepository borrowRecordRepository,
                                  @Qualifier("statsTaskExecutor") Executor statsTaskExecutor) {
        this.borrowRecordRepository = borrowRecordRepository;
        this.statsTaskExecutor = statsTaskExecutor;
    }

    public String triggerBorrowStats(LocalDateTime start, LocalDateTime end) {
        String taskId = UUID.randomUUID().toString();
        tasks.put(taskId, new TaskState("RUNNING", null));

        CompletableFuture.runAsync(() -> {
            try {
                List<BorrowCountProjection> projections = borrowRecordRepository.countBorrowedBooksBetween(start, end);
                List<BorrowStatsResult> result = projections.stream()
                        .map(item -> new BorrowStatsResult(item.getBookId(), item.getTitle(), item.getBorrowCount()))
                        .toList();
                tasks.put(taskId, new TaskState("COMPLETED", result));
            } catch (Exception ex) {
                tasks.put(taskId, new TaskState("FAILED", null));
            }
        }, statsTaskExecutor);

        return taskId;
    }

    public TaskStatusResponse getTaskStatus(String taskId) {
        TaskState taskState = tasks.get(taskId);
        if (taskState == null) {
            throw new RuntimeException("Task not found: " + taskId);
        }
        return new TaskStatusResponse(taskId, taskState.status(), taskState.result());
    }

    public static class TaskStatusResponse {
        private final String taskId;
        private final String status;
        private final List<BorrowStatsResult> result;

        public TaskStatusResponse(String taskId, String status, List<BorrowStatsResult> result) {
            this.taskId = taskId;
            this.status = status;
            this.result = result;
        }

        public String getTaskId() {
            return taskId;
        }

        public String getStatus() {
            return status;
        }

        public List<BorrowStatsResult> getResult() {
            return result;
        }
    }

    public record TaskState(String status, List<BorrowStatsResult> result) {
    }
}
