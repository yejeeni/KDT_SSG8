package com.ssg.orderservice.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
public class OrderController {
    private final RestClient restClient;

    public OrderController(RestClient restClient) {
        this.restClient = restClient;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> orders() {
        //스프링MSA를 이루고 있는 마이크로애플리케이션 중 하나인 ProductService 가 보유한
        //Rest API를 호출하자!!
        Map<String, Object> response=restClient.get()
                .uri("http://localhost:7777/products")
                .retrieve()
                .body(Map.class);

        List<String> products=(List<String>)response.get("data");

        log.debug("다른 서비스로부터 가져온 상품정보는 "+products);

        return ResponseEntity.ok(Map.of("result","상품정보 땡겨온 주문목록","products",products));
    }
}
