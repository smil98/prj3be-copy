package com.example.prj3be.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBoard is a Querydsl query type for Board
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBoard extends EntityPathBase<Board> {

    private static final long serialVersionUID = -1960555125L;

    public static final QBoard board = new QBoard("board");

    public final StringPath agency = createString("agency");

    public final EnumPath<AlbumFormat> albumFormat = createEnum("albumFormat", AlbumFormat.class);

    public final ListPath<AlbumGenre, QAlbumGenre> albumGenres = this.<AlbumGenre, QAlbumGenre>createList("albumGenres", AlbumGenre.class, QAlbumGenre.class, PathInits.DIRECT2);

    public final StringPath artist = createString("artist");

    public final ListPath<BoardFile, QBoardFile> boardFiles = this.<BoardFile, QBoardFile>createList("boardFiles", BoardFile.class, QBoardFile.class, PathInits.DIRECT2);

    public final ListPath<Comment, QComment> comments = this.<Comment, QComment>createList("comments", Comment.class, QComment.class, PathInits.DIRECT2);

    public final StringPath content = createString("content");

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final ListPath<Likes, QLikes> likes_board = this.<Likes, QLikes>createList("likes_board", Likes.class, QLikes.class, PathInits.DIRECT2);

    public final NumberPath<Double> price = createNumber("price", Double.class);

    public final DatePath<java.time.LocalDate> releaseDate = createDate("releaseDate", java.time.LocalDate.class);

    public final NumberPath<Long> stockQuantity = createNumber("stockQuantity", Long.class);

    public final StringPath title = createString("title");

    public QBoard(String variable) {
        super(Board.class, forVariable(variable));
    }

    public QBoard(Path<? extends Board> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBoard(PathMetadata metadata) {
        super(Board.class, metadata);
    }

}

