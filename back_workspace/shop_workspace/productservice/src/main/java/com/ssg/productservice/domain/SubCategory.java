package com.ssg.productservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="subcategory")
public class SubCategory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="subcategory_id")
    private int subCategoryId;

    @Column(name = "subname")
    private String subName;

    @ManyToOne(fetch = FetchType.EAGER)
    private TopCategory topCategory;
}
