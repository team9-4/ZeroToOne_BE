package com.example.mini_project.service;

import com.example.mini_project.dto.requestDto.BoardRequestDto;
import com.example.mini_project.dto.responseDto.BoardListResponseDto;
import com.example.mini_project.dto.responseDto.BoardResponseDto;
import com.example.mini_project.dto.responseDto.CommentResponseDto;
import com.example.mini_project.dto.responseDto.ResponseDto;
import com.example.mini_project.entity.Board;
import com.example.mini_project.entity.Category;
import com.example.mini_project.entity.Comment;
import com.example.mini_project.entity.Member;
import com.example.mini_project.exception.customExceptions.NotFoundBoardException;
import com.example.mini_project.exception.customExceptions.NotValidWriterException;
import com.example.mini_project.repository.BoardRepository;
import com.example.mini_project.repository.CommentRepository;
import com.example.mini_project.repository.HeartRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final HeartRepository heartRepository;
    private final CommentRepository commentRepository;
    private final S3UploadService s3UploadService;

    public ResponseDto<?> getAllRecentPost() {

        Sort sort1 = Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(0, 5, sort1);

        Page<Board> boardList = boardRepository.findAll(pageable);
        List<BoardListResponseDto> boardListResponseDtoList = new ArrayList<>();

        for (Board board : boardList) {
            BoardListResponseDto boardListResponseDto =
                    BoardListResponseDto.builder()
                            .id(board.getId())
                            .title(board.getTitle())
                            .image(board.getImage())
                            .category(board.getCategory())
                            .heartNum(heartRepository.countByBoard(board))
                            .createdAt(board.getCreatedAt())
                            .build();

            boardListResponseDtoList.add(boardListResponseDto);
        }

        return ResponseDto.success(boardListResponseDtoList);
    }

    public ResponseDto<?> getAllRecentCategory(Category category) {

        Sort sort = Sort.by("createdAt").descending();

        Pageable pageable = PageRequest.of(0, 5, sort);

        Page<Board> boardList = boardRepository.findAllByCategory(category, pageable);
        List<BoardListResponseDto> boardListResponseDtoList = new ArrayList<>();

        for(Board board : boardList){
            BoardListResponseDto boardListResponseDto = new BoardListResponseDto(board);
            boardListResponseDtoList.add(boardListResponseDto);
        }

        return ResponseDto.success(boardListResponseDtoList);
    }

    public ResponseDto<?> getAllPostByCategory(Category category) {

        List<Board> boardList = boardRepository.findAllByCategory(category);
        List<BoardListResponseDto> boardListResponseDtoList = new ArrayList<>();

        for (Board board : boardList) {
            BoardListResponseDto boardListResponseDto = new BoardListResponseDto(board);
            boardListResponseDtoList.add(boardListResponseDto);
        }

        return ResponseDto.success(boardListResponseDtoList);
    }


    @Transactional(readOnly = true)
    public ResponseDto<?> getPost(Long boardId, MemberDetailsImpl memberDetails) { // memberDetails == null ->

        Board board = boardRepository.findById(boardId).orElseThrow(
                NotFoundBoardException::new
        );

        List<Comment> commentList = commentRepository.findAllByBoardOrderByCreatedAtDesc(board);
        List<CommentResponseDto> commentResponseDtoList = new ArrayList<>();

        for (Comment comment : commentList) {
            commentResponseDtoList.add(
                    CommentResponseDto.builder()
                            .id(comment.getId())
                            .content(comment.getContent())
                            .createdAt(comment.getCreatedAt())
                            .image(comment.getMember().getImage())
                            .writer(comment.getMember().getName())
                            .build()
            );
        }

        BoardResponseDto boardResponseDto = BoardResponseDto.builder()
                .title(board.getTitle())
                .image(board.getImage())
                .writer(board.getMember().getName())  // 게시물 작성자
                .content(board.getContent())
                .createdAt(board.getCreatedAt())
                .heartNum(heartRepository.countByBoard(board))
                .commentResponseDtoList(commentResponseDtoList)
                .build();

        if (memberDetails != null) {
            boolean heartOrNot = heartRepository.existsByMemberAndBoard(memberDetails.getMember(), board);
            boardResponseDto.updateMemberNameAndHeart(memberDetails.getMember(), heartOrNot);
        }

        return ResponseDto.success(boardResponseDto);
    }

    @Transactional
    public ResponseDto<?> createBoard(BoardRequestDto boardRequestDto, Member member) throws IOException {

        Board board = Board.builder()
                .title(boardRequestDto.getTitle())
                .image(s3UploadService.upload(boardRequestDto.getImage(), "board"))
                .member(member)
                .content(boardRequestDto.getContent())
                .category(boardRequestDto.getCategory())
                .build();

        boardRepository.save(board);
        return ResponseDto.success("게시물 생성 성공");
    }

    @Transactional
    public ResponseDto<?> updateBoard(Long boardId, BoardRequestDto boardRequestDto, Member member)  {

        Board board = boardRepository.findById(boardId).orElseThrow(
                NotFoundBoardException::new
        );

        if (board.getMember().getName().equals(member.getName())) {
            throw new NotValidWriterException();
        }

        board.update(boardRequestDto);

        boardRepository.save(board);

        return ResponseDto.success("게시글 수정 완료");
    }

    @Transactional
    public ResponseDto<?> deletePost(Long boardId, Member member) {

        Board board = boardRepository.findById(boardId).orElseThrow(
                NotFoundBoardException::new
        );

        if (board.getMember().getName().equals(member.getName())) {
            throw new NotValidWriterException();
        }

        boardRepository.delete(board);
        return ResponseDto.success("게시글 삭제 완료");
    }


}

