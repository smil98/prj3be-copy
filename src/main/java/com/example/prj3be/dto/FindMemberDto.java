package com.example.prj3be.dto;

import com.example.prj3be.constant.Role;
import com.example.prj3be.domain.Member;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class FindMemberDto {
    private Long id;
    private String email;
    private String nickName;
    private String address;
    private String gender;
    private Role role;
    private Integer age;
    private LocalDateTime joinDate;

    public FindMemberDto() {
    }

    public FindMemberDto(Member member){
        this.id = member.getId();
        this.nickName=member.getNickName();
        this.address=member.getAddress();
        this.email= member.getEmail();
        this.gender=member.getGender();
        this.role=member.getRole();
        this.age= member.getAge();
        this.joinDate=member.getRegTime();
    }
}
