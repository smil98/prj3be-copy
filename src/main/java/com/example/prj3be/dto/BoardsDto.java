package com.example.prj3be.dto;

import com.example.prj3be.domain.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Getter @Setter
public class BoardsDto {
    private Long id;
    private String title;
    private String artist;
    private Double price;
    private String agency;
    private LocalDate releaseDate;
    private AlbumFormat albumFormat;
    private String fileUrl;
    private Long stockQuantity;
    private String content;
    private List<AlbumDetail> albumGenres;

    public BoardsDto(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.artist = board.getArtist();
        this.price = board.getPrice();
        this.agency = board.getAgency();
        this.releaseDate = board.getReleaseDate();
        this.albumFormat = board.getAlbumFormat();
        this.content = board.getContent();
        this.fileUrl = board.getBoardFiles().get(0).getFileUrl();
        this.stockQuantity = board.getStockQuantity();

        if (board.getAlbumGenres() != null && !board.getAlbumGenres().isEmpty()) {
            this.albumGenres = board.getAlbumGenres().stream()
                    .map(albumGenre -> albumGenre.getAlbumDetail())
                    .collect(Collectors.toList());
        }
    }
}
