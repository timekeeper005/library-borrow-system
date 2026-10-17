package com.example.librarysystem.service;

import com.example.librarysystem.dto.BookDto;
import com.example.librarysystem.dto.CreateBookRequest;
import com.example.librarysystem.dto.UpdateBookRequest;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.entity.BorrowStatus;
import com.example.librarysystem.exception.BusinessException;
import com.example.librarysystem.exception.ConflictException;
import com.example.librarysystem.exception.ResourceNotFoundException;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.repository.BorrowRecordRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class BookService {

    private final BookRepository bookRepository;
    private final BorrowRecordRepository borrowRecordRepository;

    public BookService(BookRepository bookRepository, BorrowRecordRepository borrowRecordRepository) {
        this.bookRepository = bookRepository;
        this.borrowRecordRepository = borrowRecordRepository;
    }

    @Transactional
    public BookDto createBook(CreateBookRequest request) {
        if (bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new ConflictException("ISBN already exists: " + request.getIsbn());
        }
        Book book = new Book();
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setStock(request.getStock());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        Book saved = bookRepository.save(book);
        return toBookDto(saved);
    }

    public BookDto getBookById(Long id) {
        return toBookDto(findBook(id));
    }

    public Page<BookDto> searchBooks(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Book> pageResult = bookRepository.searchBooks(keyword, pageable);
        return pageResult.map(this::toBookDto);
    }

    @Transactional
    public BookDto updateBook(Long id, UpdateBookRequest request) {
        Book book = findBook(id);
        if (!book.getIsbn().equals(request.getIsbn()) && bookRepository.findByIsbn(request.getIsbn()).isPresent()) {
            throw new ConflictException("ISBN already exists: " + request.getIsbn());
        }
        book.setTitle(request.getTitle());
        book.setAuthor(request.getAuthor());
        book.setIsbn(request.getIsbn());
        book.setCategory(request.getCategory());
        book.setStock(request.getStock());
        book.setPublisher(request.getPublisher());
        book.setPublishDate(request.getPublishDate());
        return toBookDto(bookRepository.save(book));
    }

    @Transactional
    public void deleteBook(Long id) {
        Book book = findBook(id);
        boolean hasActiveBorrow = borrowRecordRepository.existsByBookIdAndStatusIn(
                id,
                java.util.List.of(BorrowStatus.BORROWED, BorrowStatus.OVERDUE)
        );
        if (hasActiveBorrow) {
            throw new ConflictException("Book cannot be deleted because it has an unreturned borrow record");
        }
        bookRepository.delete(book);
    }

    private Book findBook(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Book not found: " + id));
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
}
