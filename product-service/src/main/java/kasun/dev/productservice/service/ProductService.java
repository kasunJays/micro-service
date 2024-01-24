package kasun.dev.productservice.service;

import kasun.dev.productservice.dto.ProductRequest;
import kasun.dev.productservice.dto.ProductResponse;
import kasun.dev.productservice.model.Product;
import kasun.dev.productservice.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    public void createProduct(ProductRequest productRequest) {
        Product product = Product.builder().name(productRequest.getName()).price(productRequest.getPrice()).description
                (productRequest.getDescription()).build();
        productRepository.save(product);
        log.info("Product {} is saved",product.getId());
    }

    public List<ProductResponse> getAllProducts() {
        return productRepository.findAll().stream().map(this::mapToProductResource).collect(Collectors.toList());
    }
    private ProductResponse mapToProductResource(Product product) {
        return ProductResponse.builder().id(product.getId())
                .name(product.getName())
                .price(product.getPrice())
                .description(product.getDescription())
                .build();
    }
}
