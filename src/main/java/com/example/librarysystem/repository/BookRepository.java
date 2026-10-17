package com.example.librarysystem.repository;

import com.example.librarysystem.entity.Book;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long> {

    Optional<Book> findByIsbn(String isbn);

    Page<Book> findByStockGreaterThan(Integer stock, Pageable pageable);

    @Query("select b from Book b where (:keyword is null or :keyword = '' or lower(b.title) like lower(concat('%', :keyword, '%')) or lower(b.author) like lower(concat('%', :keyword, '%')) or lower(b.isbn) like lower(concat('%', :keyword, '%')) or lower(b.category) like lower(concat('%', :keyword, '%'))) order by b.id desc")
    Page<Book> searchBooks(@Param("keyword") String keyword, Pageable pageable);
}
