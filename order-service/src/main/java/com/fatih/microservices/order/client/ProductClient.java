package com.fatih.microservices.order.client;

import com.fatih.microservices.order.exception.ProductNotAvailableException;
import com.fatih.microservices.order.exception.ProductNotFoundException;
import com.fatih.microservices.order.exception.ProductServiceUnavailableException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;
import io.github.resilience4j.retry.annotation.Retry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;

@Component
public class ProductClient {

    private final RestClient restClient;
    private static final Logger log =
            LoggerFactory.getLogger(ProductClient.class);

    public ProductClient(@Value("${product.service.url}") String baseUrl) {
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .build();
    }
    @CircuitBreaker(name = "productService")
    public ProductResponse getProduct(Long id) {
        return restClient.get()
                .uri("/api/products/{id}", id)
                .retrieve()
                .body(ProductResponse.class);
    }
    @Retry(name = "productService")
    public ProductResponse getProductById(Long productId) {
        log.info(
                "Calling Product Service for productId={}",
                productId
        );

        try {
            return restClient
                    .get()
                    .uri("/api/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class);

        } catch (HttpClientErrorException.NotFound ex) {
            throw new  ProductNotFoundException(productId);        // product doesn't exist -> 404

        } catch (ResourceAccessException | HttpServerErrorException ex) {
            throw new ProductServiceUnavailableException();       // service down / 5xx -> 503
        }
    }
}