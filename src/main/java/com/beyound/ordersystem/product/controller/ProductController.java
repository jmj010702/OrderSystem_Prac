package com.beyound.ordersystem.product.controller;

import com.beyound.ordersystem.product.dto.ProductCreateDto;
import com.beyound.ordersystem.product.dto.ProductListDto;
import com.beyound.ordersystem.product.dto.ProductSearchDto;
import com.beyound.ordersystem.product.entity.Product;
import com.beyound.ordersystem.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/product")
public class ProductController {

    private final ProductService productService;

    @Autowired
    public ProductController(ProductService productService) {
        this.productService = productService;
    }

@PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/create")
    public ResponseEntity<?> create(@ModelAttribute  ProductCreateDto dto) {
        Product product = productService.create(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(product.getId());

    }

    @GetMapping("/detail/{inputId}")
    public ResponseEntity<?> findById(@PathVariable Long inputId) {
        return ResponseEntity.status(HttpStatus.OK).body(productService.findById(inputId));
    }

    @GetMapping("/list")
    public ResponseEntity<?> findAll(Pageable pageable, ProductSearchDto dto) {
        Page<ProductListDto> productListDtos = productService.findAll(pageable, dto);
        return ResponseEntity.status(HttpStatus.OK).body(productListDtos);

    }

}
