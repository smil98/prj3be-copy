package com.example.prj3be.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QFreshToken is a Querydsl query type for FreshToken
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFreshToken extends EntityPathBase<FreshToken> {

    private static final long serialVersionUID = -1950704154L;

    public static final QFreshToken freshToken = new QFreshToken("freshToken");

    public final StringPath email = createString("email");

    public final DateTimePath<java.time.LocalDateTime> regTime = createDateTime("regTime", java.time.LocalDateTime.class);

    public final StringPath token = createString("token");

    public QFreshToken(String variable) {
        super(FreshToken.class, forVariable(variable));
    }

    public QFreshToken(Path<? extends FreshToken> path) {
        super(path.getType(), path.getMetadata());
    }

    public QFreshToken(PathMetadata metadata) {
        super(FreshToken.class, metadata);
    }

}

