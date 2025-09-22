package com.ssg.productservice.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
@Table(name="productfile")
public class ProductFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int productFileId;

    private String fileName;

    private String originalName;

    private String contentType;

    @Column(name = "filepath")
    private String filePath;

    @Column(name = "filesize")
    private Long fileSize;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "product_id")
    private Product product;
}
