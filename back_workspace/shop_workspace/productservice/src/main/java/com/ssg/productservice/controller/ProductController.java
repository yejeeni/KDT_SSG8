package com.ssg.productservice.controller;

import com.ssg.productservice.controller.dto.ProductDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Slf4j
//Roy폴딩
@RestController
public class ProductController {

    @GetMapping("/products")
    public ResponseEntity<?> products() {
        return ResponseEntity.ok(
                Map.of("data", List.of("노트북","스마트폰","태블릿","마우스","키보드","스피커"))
        );
    }

    //파일업로드 요청 처리
    @PostMapping(value="/products", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> regist(@ModelAttribute ProductDTO productDTO, @RequestPart List<MultipartFile> files) {
        log.debug("넘겨받은 이미지 수는 "+files.size());

        log.debug("서브 카테고리는 "+productDTO.getSubCategoryDTO().getSubCategoryId());
        log.debug("상품명"+productDTO.getProductName());
        log.debug("브랜드"+productDTO.getBrand());
        log.debug("가격"+productDTO.getPrice());
        log.debug("할인가"+productDTO.getDiscount());
        log.debug("상세설명"+productDTO.getDetail());

        return ResponseEntity.ok(Map.of("result", "업로드 성공"));
    }


}
















