package com.example.librarysystem.service;

import com.example.librarysystem.dto.BookDto;
import com.example.librarysystem.dto.BorrowRecordDto;
import com.example.librarysystem.dto.BorrowRequest;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.BorrowRecord;
import com.example.librarysystem.entity.BorrowStatus;
import com.example.librarysystem.entity.Reader;
import com.example.librarysystem.exception.BusinessException;
import com.example.librarysystem.exception.ResourceNotFoundException;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowRecordRepository;
import com.example.librarysystem.repository.ReaderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class BorrowService {

    private final BookRepository bookRepository;
    private final ReaderRepository readerRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BorrowService(BookRepository bookRepository,
                        ReaderRepository readerRepository,
                        BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.readerRepository = readerRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    public List<BookDto> getAvailableBooks() {
        return bookRepository.findByStockGreaterThan(0, org.springframework.data.domain.PageRequest.of(0, 100))
                .stream()
                .map(this::toBookDto)
                .toList();
    }

    @Transactional
    public BorrowRecordDto borrowBook(Long readerId, BorrowRequest request) {
        Reader reader = readerRepository.findById(readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Reader not found: " + readerId));
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + request.getBookId()));

        markOverdueRecords(readerId);

        if (book.getStock() <= 0) {
            throw new BusinessException("Book is out of stock");
        }

        if (borrowRecordRepository.existsByReaderIdAndStatusIn(readerId, java.util.List.of(BorrowStatus.OVERDUE))) {
            throw new BusinessException("Reader has overdue books and cannot borrow new books");
        }

        if (borrowRecordRepository.existsByReaderIdAndBookIdAndStatusIn(
                readerId,
                book.getId(),
                java.util.List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE)
        )) {
            throw new BusinessException("Reader has already borrowed this book and has not returned it");
        }

        LocalDateTime now = LocalDateTime.now();
        BorrowRecord record = new BorrowRecord();
        record.setReader(reader);
        record.setBook(book);
        record.setBorrowDate(now);
        record.setDueDate(now.plusDays(30));
        record.setStatus(BorrowStatus.BORROWED);

        book.setStock(book.getStock() - 1);
        bookRepository.save(book);
        BorrowRecord saved = borrowRecordRepository.save(record);
        return toBorrowRecordDto(saved);
    }

    @Transactional
    public BorrowRecordDto returnBook(Long readerId, Long recordId) {
        BorrowRecord record = borrowRecordRepository.findByIdAndReaderId(recordId, readerId)
                .orElseThrow(() -> new ResourceNotFoundException("Borrow record not found: " + recordId));

        if (record.getStatus() == BorrowStatus.RETURNED) {
            throw new BusinessException("This record has already been returned");
        }

        Book book = record.getBook();
        book.setStock(book.getStock() + 1);
        bookRepository.save(book);

        record.setReturnDate(LocalDateTime.now());
        record.setStatus(BorrowStatus.RETURNED);
        BorrowRecord saved = borrowRecordRepository.save(record);
        return toBorrowRecordDto(saved);
    }

    public List<BorrowRecordDto> getReaderRecords(Long readerId) {
        markOverdueRecords(readerId);
        return borrowRecordRepository.findByReaderIdOrderByBorrowDateDesc(readerId)
                .stream()
                .map(this::toBorrowRecordDto)
                .toList();
    }

    private void markOverdueRecords(Long readerId) {
        List<BorrowRecord> records = borrowRecordRepository.findByReaderIdAndStatus(readerId, BorrowStatus.BORROWED);
        LocalDateTime now = LocalDateTime.now();
        for (BorrowRecord record : records) {
            if (!record.getDueDate().isAfter(now)) {
                record.setStatus(BorrowStatus.OVERDUE);
                borrowRecordRepository.save(record);
            }
        }
    }

    private BookDto toBookDto(Book book) {
        BookDto dto = new BookDto();
        dto.setId(book.getId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setIsbn(book.getIsbn());
        dto.setCategory(book.getCategory());
        dto.setStock(book.getStock());
        dto.setPublisher(book.getPublisher());
        dto.setCreatedAt(book.getCreatedAt());
        dto.setUpdatedAt(book.getUpdatedAt());
        return dto;
    }

    private BorrowRecordDto toBorrowRecordDto(BorrowRecord record) {
        BorrowRecordDto dto = new BorrowRecordDto();
        dto.setId(record.getId());
        dto.setReaderId(record.getReader().getId());
        dto.setBookId(record.getBook().getId());
        dto.setBookTitle(record.getBook().getTitle());
        dto.setReaderName(record.getReader().getName());
        dto.setBorrowDate(record.getBorrowDate());
        dto.setDueDate(record.getDueDate());
        dto.setReturnDate(record.getReturnDate());
        dto.setStatus(record.getStatus().name());
        return dto;
    }
}
