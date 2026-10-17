package com.example.librarysystem.repository;

import com.example.librarysystem.entity.BorrowRecord;
import com.example.librarysystem.entity.BorrowStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord, Long> {

    List<BorrowRecord> findByReaderIdOrderByBorrowDateDesc(Long readerId);

    List<BorrowRecord> findByReaderIdAndStatus(Long readerId, BorrowStatus status);

    boolean existsByBookIdAndStatusIn(Long bookId, Collection<BorrowStatus> statuses);

    boolean existsByReaderIdAndBookIdAndStatusIn(Long readerId, Long bookId, Collection<BorrowStatus> statuses);

    boolean existsByReaderIdAndStatusIn(Long readerId, Collection<BorrowStatus> statuses);

    Optional<BorrowRecord> findByIdAndReaderId(Long id, Long readerId);

    @Query("""
        select br.book.id as bookId,
               br.book.title as title,
               count(br.id) as borrowCount
        from BorrowRecord br
        where br.borrowDate >= :start and br.borrowDate <= :end
        group by br.book.id, br.book.title
        order by borrowCount desc
        """)
    List<BorrowCountProjection> countBorrowedBooksBetween(@Param("start") LocalDateTime start,
                                                          @Param("end") LocalDateTime end);
}
