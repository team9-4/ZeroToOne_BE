package com.example.mini_project.controller;

import com.example.mini_project.dto.requestDto.BoardRequestDto;
import com.example.mini_project.dto.responseDto.ResponseDto;
import com.example.mini_project.entity.Category;
import com.example.mini_project.service.BoardService;
import com.example.mini_project.service.MemberDetailsImpl;
import lombok.RequiredArgsConstructor;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;

    // 전체 게시물 조회
    @GetMapping("/board/recent")
    public ResponseDto<?> getAllRecentPost() {

        return boardService.getAllRecentPost();
    }

    @GetMapping("/board/recent-category")
    public ResponseDto<?> getAllRecentCategory(@RequestParam(value = "category", required = true) Category category){
        return boardService.getAllRecentCategory(category);
    }

    @GetMapping("/board/category")
    public ResponseDto<?> getAllPostByCategory(@RequestParam(value = "category", required = true) Category category){
        return boardService.getAllPostByCategory(category);
    }

    // 게시글 상세 조회
    @GetMapping("/board/{boardId}")
    public ResponseDto<?> getPost(@PathVariable Long boardId, @AuthenticationPrincipal MemberDetailsImpl memberDetails) {
        return boardService.getPost(boardId, memberDetails);
    }

    // 카텔고리별 데이터 불러오기

    // 게시물 등록
    @PostMapping("/board")
    public ResponseDto<?> createBoard(@RequestBody BoardRequestDto boardRequestDto, @AuthenticationPrincipal MemberDetailsImpl memberDetails) throws IOException {
        return boardService.createBoard(boardRequestDto, memberDetails.getMember());
    }

    @PutMapping("/board/{boardId}")
    public ResponseDto<?> updateBoard(@PathVariable Long boardId, @RequestBody BoardRequestDto boardRequestDto,
                                      @AuthenticationPrincipal MemberDetailsImpl memberDetails){
        return boardService.updateBoard(boardId, boardRequestDto, memberDetails.getMember());
    }

    @DeleteMapping("/board/{boardId}")
    public ResponseDto<?> deleteBoard(@PathVariable Long boardId, @AuthenticationPrincipal MemberDetailsImpl memberDetails){
        return boardService.deletePost(boardId, memberDetails.getMember());
    }

}
