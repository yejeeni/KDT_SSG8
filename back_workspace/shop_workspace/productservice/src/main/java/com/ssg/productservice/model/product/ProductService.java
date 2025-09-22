package com.ssg.productservice.model.product;

import com.ssg.productservice.domain.Product;
import com.ssg.productservice.domain.ProductFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface ProductService {

    public void save(Product product, ProductFile productFile, List<MultipartFile> files);
}
