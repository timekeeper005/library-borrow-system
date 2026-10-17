package com.example.librarysystem;

import com.example.librarysystem.dto.CreateBookRequest;
import com.example.librarysystem.entity.Book;
import com.example.librarysystem.repository.BookRepository;
import com.example.librarysystem.service.BookService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    com.example.librarysystem.repository.BorrowRecordRepository borrowRecordRepository;

    @InjectMocks
    private BookService bookService;

    @Test
    void shouldCreateBookSuccessfully() {
        CreateBookRequest request = new CreateBookRequest();
        request.setTitle("Clean Code");
        request.setAuthor("Robert C. Martin");
        request.setIsbn("9780132350884");
        request.setCategory("Computer Science");
        request.setStock(5);
        request.setPublisher("Prentice Hall");
        request.setPublishDate(LocalDate.of(2008, 8, 1));

        when(bookRepository.findByIsbn(any(String.class))).thenReturn(Optional.empty());
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> {
            Book book = invocation.getArgument(0);
            book.setId(1L);
            return book;
        });

        var result = bookService.createBook(request);

        assertEquals("Clean Code", result.getTitle());
        assertEquals("9780132350884", result.getIsbn());
    }
}
