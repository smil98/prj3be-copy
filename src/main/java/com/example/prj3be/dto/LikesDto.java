package com.example.prj3be.dto;

import com.example.prj3be.domain.Likes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class LikesDto {
    private Long id;
    private Long boardId;
    private String title;
    private String artist;
    private Double price;

    public LikesDto(Likes likes) {
        this.id = likes.getId();
        this.boardId = likes.getBoard().getId();
        this.title = likes.getBoard().getTitle();
        this.artist = likes.getBoard().getArtist();
        this.price = likes.getBoard().getPrice();;
    }
}
