package com.legendary.legendary.service;

import com.legendary.legendary.model.Product;
import com.legendary.legendary.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import jakarta.annotation.PostConstruct;
import java.io.IOException;
import java.nio.file.*;

@Service
public class ProductService {
    @Autowired
    private ProductRepo productRepository;
    private final Path root = Paths.get("./uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(root);
    }

    public Product saveProduct(Product product, MultipartFile file) throws IOException {
        if (file != null && !file.isEmpty()) {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Files.copy(file.getInputStream(), this.root.resolve(filename));
            product.setImagePath(filename);
        }
        return productRepository.save(product);
    }
}

