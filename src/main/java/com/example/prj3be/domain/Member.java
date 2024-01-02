package com.example.prj3be.domain;

import com.example.prj3be.constant.Role;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name="member")
@Getter
@Setter
public class Member extends BaseTimeEntity{
    @Id
    @Column(name="member_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String email;
    private String password;

    @Column(unique = true)
    private String nickName;

    private Integer age;
    private String gender;
    private String address;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ROLE_USER; //ADMIN, USER(default), SOCIAL

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE)
    private Set<SocialToken> socialTokens = new HashSet<>();

    @OneToMany(mappedBy = "member")
    private List<Likes> likes_member;

//    @ManyToMany
//    @JoinTable(
//            name="member_authority",
//            joinColumns = {@JoinColumn(name="member_id", referencedColumnName = "member_id")},
//            inverseJoinColumns = {@JoinColumn(name="authority_name", referencedColumnName = "authority_name")}
//    )
//    private Set<Authority> authorities;
}
