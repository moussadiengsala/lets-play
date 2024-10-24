package com.zone01.products.products;

import com.zone01.products.config.AccessValidation;
import com.zone01.products.utils.Response;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductsService {
    private final ProductsRepository productsRepository;

    @Autowired
    public ProductsService(ProductsRepository productsRepository) {
        this.productsRepository = productsRepository;
    }

    public List<Products> getAllProducts() {
        return productsRepository.findAll();
    }

    public Optional<Products> getProductById(String id) {
        return productsRepository.findById(id);
    }

    public Products createProduct(Products product, HttpServletRequest request) {

        UserDTO currentUser = AccessValidation.getCurrentUser(request);

        Products newProduct = Products
                .builder()
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .userID(currentUser.getId())
                .build();

        return productsRepository.save(newProduct);
    }

    private Response<Products> authorizeAndGetProduct(String id) {
        // Find the product by its ID
        Optional<Products> productOptional = productsRepository.findById(id);

        // Check if the product exists
        if (productOptional.isEmpty()) {
            return Response.<Products>builder()
                    .status(HttpStatus.NOT_FOUND.value())
                    .data(null)
                    .message("Product not found")
                    .build();
        }

        return Response.<Products>builder()
                .status(HttpStatus.OK.value())
                .data(productOptional.get())
                .build();
    }

    public Response<Products> updateProduct(String id, Products productDetails, HttpServletRequest request) {
        UserDTO currentUser = AccessValidation.getCurrentUser(request);

        // Authorize and get the product
        Response<Products> authorizationResponse = authorizeAndGetProduct(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return authorizationResponse;
        }

        // Update product details
        Products product = authorizationResponse.getData();
        product.setName(productDetails.getName());
        product.setDescription(productDetails.getDescription());
        product.setPrice(productDetails.getPrice());

        // Save updated product
        Products updatedProduct = productsRepository.save(product);

        // Build and return response
        return Response.<Products>builder()
                .status(HttpStatus.OK.value())
                .data(updatedProduct)
                .message("Product updated successfully")
                .build();
    }

    public Response<Products> deleteProduct(String id, HttpServletRequest request) {
        UserDTO currentUser = AccessValidation.getCurrentUser(request);

        // Authorize and get the product
        Response<Products> authorizationResponse = authorizeAndGetProduct(id);
        if (authorizationResponse.getStatus() != HttpStatus.OK.value()) {
            return authorizationResponse;
        }

        // Delete the product
        productsRepository.deleteById(id);

        // Return success response
        return Response.<Products>builder()
                .status(HttpStatus.OK.value())
                .data(authorizationResponse.getData())
                .message("Product deleted successfully")
                .build();
    }

}
