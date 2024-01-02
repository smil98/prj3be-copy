package com.example.prj3be.dto;

import com.example.prj3be.constant.Role;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class MemberFormDto {

    @Email(message = "이메일 형식에 맞게 입력해주세요.")
    private String email;

    @NotBlank(message = "비밀번호는 필수 입력값입니다.")
    @Size(min = 6,max = 16)
    private String password;

    @NotBlank(message = "닉네임은 필수 입력값입니다.")
    private String nickName;

    private Integer age;

    private String address;
    private Role role;
    private String gender;
}
