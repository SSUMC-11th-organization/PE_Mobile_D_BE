package com.umc.movie.controller;

import com.umc.movie.entity.Member;
import com.umc.movie.repository.MemberRepository;
import com.umc.movie.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class RatingApiTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("토큰 없이 평점을 등록하면 401과 AUTH401_2를 응답한다")
    void createRatingWithoutTokenReturns401() throws Exception {

        String requestBody = """
                {
                  "movieId": 550,
                  "score": 5,
                  "comment": "재밌어요"
                }
                """;

        mockMvc.perform(
                        post("/ratings")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("AUTH401_2"));
    }

    @Test
    @DisplayName("유효한 토큰으로 평점을 등록하면 201을 응답한다")
    void createRatingWithTokenReturns201() throws Exception {

        Member member = Member.builder()
                .email("ratingtest@example.com")
                .password("password")
                .nickname("ratingTester")
                .build();

        Member savedMember = memberRepository.save(member);

        String token = jwtUtil.createAccessToken(
                savedMember.getMemberId(),
                savedMember.getEmail()
        );

        String requestBody = """
                {
                  "movieId": 550,
                  "score": 5,
                  "comment": "재밌어요"
                }
                """;

        mockMvc.perform(
                        post("/ratings")
                                .header("Authorization", "Bearer " + token)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("COMMON201"));
    }
}