package com.example.prj3be.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MemberEditFormDto {
    private String nickName;
    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    private String password;
    private String address;
    private Integer age;
    private String gender;
}
