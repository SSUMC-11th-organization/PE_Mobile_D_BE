package com.umc.study.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class RentalRepository {
    private final JdbcTemplate jdbcTemplate;

    public void save(Map<String, Object> body) {
        // rented_at과 due_at은 DB 함수를 사용해 자동으로 시간이 들어가게 합니다.
        String sql = "INSERT INTO rental (user_id, book_id, rented_at, due_at) " +
                "VALUES (?, ?, NOW(), DATE_ADD(NOW(), INTERVAL 7 DAY))";

        jdbcTemplate.update(
                sql,
                body.get("userId"), // 첫 번째 ? 에 들어갈 값
                body.get("bookId")  // 두 번째 ? 에 들어갈 값
        );
    }
}