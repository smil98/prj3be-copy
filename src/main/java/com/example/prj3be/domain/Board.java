package com.example.prj3be.domain;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import software.amazon.awssdk.services.s3.endpoints.internal.Value;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(schema = "prj1")
@NoArgsConstructor
@Getter
@Setter
public class Board {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "board_id")
    private Long id;
    private String title;
    private String artist;

    @Enumerated(EnumType.STRING)
    private AlbumFormat albumFormat;

    private Double price;
    private String agency;
    @Column(name = "content", length = 3000)
    private String content;
    private LocalDate releaseDate;

    @Column(name = "stock_quantity", columnDefinition = "BIGINT DEFAULT 100")
    private Long stockQuantity;

    @OneToMany(mappedBy = "board")
    private List <Likes> likes_board;

    @OneToMany(mappedBy = "board")
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    private List<AlbumGenre> albumGenres = new ArrayList<>();

    @OneToMany(mappedBy = "board")
    @JsonManagedReference
    private List<BoardFile> boardFiles = new ArrayList<>();


    public Board(Long id, String title, String artist, AlbumFormat albumFormat, Double price, String agency, String content, LocalDate releaseDate, Long stockQuantity, List<Comment> comments, List<AlbumGenre> albumGenres, List<BoardFile> boardFiles) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.albumFormat = albumFormat;
        this.price = price;
        this.agency = agency;
        this.content = content;
        this.releaseDate = releaseDate;
        this.stockQuantity = stockQuantity;
        this.comments = comments;
        this.albumGenres = albumGenres;
        this.boardFiles = boardFiles;
    }

}
