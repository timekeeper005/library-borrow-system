package com.example.librarysystem.dto;

import java.util.List;

public class BorrowStatsResult {
    private Long bookId;
    private String title;
    private Long borrowCount;

    public BorrowStatsResult() {
    }

    public BorrowStatsResult(Long bookId, String title, Long borrowCount) {
        this.bookId = bookId;
        this.title = title;
        this.borrowCount = borrowCount;
    }

    public Long getBookId() {
        return bookId;
    }

    public void setBookId(Long bookId) {
        this.bookId = bookId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Long getBorrowCount() {
        return borrowCount;
    }

    public void setBorrowCount(Long borrowCount) {
        this.borrowCount = borrowCount;
    }
}

class BorrowStatsTaskResponse {
    private String taskId;
    private String status;
    private List<BorrowStatsResult> result;

    public BorrowStatsTaskResponse() {
    }

    public BorrowStatsTaskResponse(String taskId, String status, List<BorrowStatsResult> result) {
        this.taskId = taskId;
        this.status = status;
        this.result = result;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<BorrowStatsResult> getResult() {
        return result;
    }

    public void setResult(List<BorrowStatsResult> result) {
        this.result = result;
    }
}
