package com.zone01.lets_play.products;

import com.zone01.lets_play.utils.Response;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@AllArgsConstructor
@RestController
@RequestMapping("/api/v1/products")
public class ProductsController {
    private final ProductsService productsService;

    @GetMapping
    public List<Products> getAllProducts() {
        return productsService.getAllProducts();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Products> getProductById(@PathVariable String id) {
        return productsService.getProductById(id)
                .map(product -> ResponseEntity.ok(product))
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<Products> createProduct(@Validated @RequestBody Products product) {
        Products createdProduct = productsService.createProduct(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdProduct);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Products> updateProduct(
            @PathVariable String id,
            @Validated @RequestBody Products productDetails) {
        Products updatedProduct = productsService.updateProduct(id, productDetails);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        productsService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Response<Map<String, String>> HandleValidationException(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<String, String>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        return new Response<Map<String, String>>(400, errors, "");
    }
}


//{
//        "access_token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzQGdtYWlsLmNvbSIsImlhdCI6MTcyODc2OTk0MywiZXhwIjoxNzI4ODU2MzQzfQ.eGdmrbLqwCg82Y_nymaehIDVJh20JRd-EJ8a-cbSGzwAM7P6NAzMYbEap0PYl8Ej",
//        "refresh_token": "eyJhbGciOiJIUzM4NCJ9.eyJzdWIiOiJtb2lzQGdtYWlsLmNvbSIsImlhdCI6MTcyODc2OTk0MywiZXhwIjoxNzI5Mzc0NzQzfQ.Sz28E-vem1NfuivbozpS6Yg5GHAWW0I2T8uFStPqklEE1k-LfJC-vp7SehrKDQNF"
//        }