package com.example.librarysystem.repository;

public interface BorrowCountProjection {
    Long getBookId();
    String getTitle();
    Long getBorrowCount();
}
