package com.ssg.productservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="topcategory")
public class TopCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="topcategory_id")
    private int topCategoryId;

    @Column(name = "topname")
    private String topName;
}
