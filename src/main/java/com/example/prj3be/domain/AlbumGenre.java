package com.example.prj3be.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(schema = "prj1")
@NoArgsConstructor
@Getter
@Setter
public class AlbumGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private AlbumDetail albumDetail;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "board_id")
    private Board board;

    @Builder //빌더 메소드 자동생성
    public AlbumGenre(Long id, AlbumDetail albumDetail, Board board) {
        this.id = id;
        this.albumDetail = albumDetail;
        this.board = board;
    }

}
