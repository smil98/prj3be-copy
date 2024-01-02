package com.example.prj3be.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class MemberAuthDto implements Serializable {
    private static final long serialVersionUID = 1L; // serialVersionUID 추가
    private String email;
    private String role;
    @JsonCreator
    public MemberAuthDto(@JsonProperty("email")String email,@JsonProperty("role")String role) {
        this.email = email;
        this.role = role;
    }
}
