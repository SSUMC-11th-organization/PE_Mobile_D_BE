package com.umc.study.repository;

import com.umc.study.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

// Book 엔티티와 PK 타입(Long)을 연결해 주는 Repository 인터페이스
public interface BookRepository extends JpaRepository<Book, Long> {
    List<Book> findAllByOrderByBookIdDesc(); // 최신순으로 모든 도서 조회
}

