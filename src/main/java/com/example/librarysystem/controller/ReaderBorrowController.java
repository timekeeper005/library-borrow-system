package com.example.librarysystem.controller;

import com.example.librarysystem.dto.BorrowRecordDto;
import com.example.librarysystem.dto.BorrowRequest;
import com.example.librarysystem.dto.BookDto;
import com.example.librarysystem.service.BorrowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ReaderBorrowController {

    private final BorrowService borrowService;

    public ReaderBorrowController(BorrowService borrowService) {
        this.borrowService = borrowService;
    }

    @GetMapping("/books/available")
    public ResponseEntity<List<BookDto>> getAvailableBooks() {
        return ResponseEntity.ok(borrowService.getAvailableBooks());
    }

    @PostMapping("/readers/{readerId}/borrow")
    public ResponseEntity<BorrowRecordDto> borrowBook(@PathVariable Long readerId,
                                                    @RequestBody BorrowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(borrowService.borrowBook(readerId, request));
    }

    @GetMapping("/readers/{readerId}/records")
    public ResponseEntity<List<BorrowRecordDto>> getReaderRecords(@PathVariable Long readerId) {
        return ResponseEntity.ok(borrowService.getReaderRecords(readerId));
    }

    @PostMapping("/readers/{readerId}/return/{recordId}")
    public ResponseEntity<BorrowRecordDto> returnBook(@PathVariable Long readerId,
                                                    @PathVariable Long recordId) {
        return ResponseEntity.ok(borrowService.returnBook(readerId, recordId));
    }
}
