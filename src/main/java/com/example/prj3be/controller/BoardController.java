package com.example.prj3be.controller;

import com.example.prj3be.domain.*;
import com.example.prj3be.dto.BoardDto;
import com.example.prj3be.dto.BoardsDto;
import com.example.prj3be.dto.MemberAuthDto;
import com.example.prj3be.jwt.TokenProvider;
import com.example.prj3be.service.BoardService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.Collections.emptyList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
public class BoardController {
    private final BoardService boardService;
    private final TokenProvider tokenProvider;

    @GetMapping("list")
    public Page<Board> list(Pageable pageable,
                            @RequestParam(required = false) String title,
                            @RequestParam(required = false) AlbumFormat albumFormat,
                            @RequestParam(required = false) String[] albumDetails,
                            @RequestParam(required = false) String minPrice,
                            @RequestParam(required = false) String maxPrice
                        ) {

        List<AlbumDetail> albumDetailList = (albumDetails == null) ? null : Arrays.stream(albumDetails).map(AlbumDetail::valueOf).collect(Collectors.toList());

        Page<Board> boardListPage = boardService.boardListAll(pageable, title, albumFormat, albumDetailList, minPrice, maxPrice);

        System.out.println("boardListPage = " + boardListPage);

        // stackoverflowerror 발생 가능한 지점
        return boardListPage;
    }


    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("add")
    public void add(@Validated Board saveBoard,
                    @RequestParam(required = false) String[] albumDetails,
                    @RequestParam(value = "uploadFiles[]", required = false) MultipartFile[] files) throws IOException {
        List<AlbumDetail> AlbumDetailList = Arrays.stream(albumDetails)
                .map(AlbumDetail::valueOf)
                .collect(Collectors.toList());

        boardService.save(saveBoard, AlbumDetailList , files);
    }


    @GetMapping("/{id}")
    public BoardsDto get(@PathVariable Long id) {
        System.out.println("BoardController.get");
        System.out.println("id = " + id);
        Optional<Board> board = boardService.getBoardById(id);
        if(board.isPresent()) {
            return new BoardsDto(board.get());
        } else {
            return null;
        }
    }

//    @GetMapping("file/id/{id}")
//    public List<String> getURL(@PathVariable Long id) {
//        return boardService.getBoardURL(id);
//    }

    @PutMapping("edit/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void update(@PathVariable Long id,
                       Board updateBboard,
                       @RequestParam(value = "uploadFiles", required = false) MultipartFile uploadFiles) throws IOException {
        if (uploadFiles == null) {
            boardService.update(id, updateBboard);
        } else {
            boardService.update(id, updateBboard, uploadFiles);
        }
    }

    @GetMapping("/fetch")
    @PreAuthorize("hasRole('ADMIN')")
    public Page<BoardsDto> fetchList(Pageable pageable,
                                  @RequestParam(value = "k", defaultValue = "") String keyword,
                                  @RequestParam(value = "c", defaultValue = "all") String category,
                                  @RequestHeader("Authorization") String accessToken) {
        System.out.println("BoardController.manage");
        if(StringUtils.hasText(accessToken) && accessToken.startsWith("Bearer ")){
            accessToken = accessToken.substring(7);
        }

        if(tokenProvider.validateToken(accessToken)) {
            Page<Board> boardsPage = boardService.getAllBoards(pageable, keyword, category);
            return boardsPage.map(BoardsDto::new);
        } else {
            return null;
        }
    }


    @DeleteMapping("remove/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public void delete(@PathVariable Long id) {
        boardService.delete(id);
    }

}





