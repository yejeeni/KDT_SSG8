package com.ssg.jwtredis.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Table(name = "role")
@Entity
@Getter @Setter
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int roleId;

    private String roleName;

    // Role에서 Member 목록이 필요할 때
//    @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
//    private List<Member> members = new ArrayList<>();
}
