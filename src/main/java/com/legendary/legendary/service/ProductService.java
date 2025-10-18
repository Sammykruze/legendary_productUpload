package com.legendary.legendary.service;

import com.legendary.legendary.model.Product;
import com.legendary.legendary.repository.ProductRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.util.StringUtils;
import jakarta.annotation.PostConstruct;

import java.io.IOException;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    @Autowired
    private ProductRepo productRepository;

    //Make upload directory configurable (can change in application.properties)
    @Value("${app.upload.dir:./uploads}")
    private String uploadDir;

    private Path root;

    //List of allowed image types for security
    private final List<String> allowedImageTypes = Arrays.asList("image/jpeg", "image/png", "image/gif");

    @PostConstruct
    public void init() throws IOException {
        this.root = Paths.get(uploadDir);
        Files.createDirectories(root);
    }

    public Product saveProduct(Product product, MultipartFile file) throws IOException {
        //Validate product price before saving
        if (product.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }

        if (file != null && !file.isEmpty()) {
            //Security check: Validate file type
            String fileType = file.getContentType();
            if (fileType == null || !allowedImageTypes.contains(fileType)) {
                throw new IOException("Only JPG, PNG, and GIF images are allowed");
            }

            //Clean filename to prevent path traversal attacks
            String cleanFileName = StringUtils.cleanPath(file.getOriginalFilename());

            String filename = System.currentTimeMillis() + "_" + cleanFileName;

            //Handle case where file might already exist
            Files.copy(file.getInputStream(),
                    this.root.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);

            product.setImagePath(filename);
        }

        return productRepository.save(product);
    }

    // Get all products
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    // Get a single product by ID
    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    // Get the root path for image serving
    public Path getRoot() {
        return root;
    }

    // Update product
    public Product updateProduct(Long id, Product productDetails, MultipartFile file) throws IOException {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();

        // Validate price if provided
        if (productDetails.getPrice() != null && productDetails.getPrice() <= 0) {
            throw new IllegalArgumentException("Product price must be greater than 0");
        }

        // Update fields if they are provided in productDetails
        if (productDetails.getName() != null) {
            product.setName(productDetails.getName());
        }
        if (productDetails.getDescription() != null) {
            product.setDescription(productDetails.getDescription());
        }
        if (productDetails.getPrice() != null) {
            product.setPrice(productDetails.getPrice());
        }

        // Handle image update
        if (file != null && !file.isEmpty()) {
            // Security check: Validate file type
            String fileType = file.getContentType();
            if (fileType == null || !allowedImageTypes.contains(fileType)) {
                throw new IOException("Only JPG, PNG, and GIF images are allowed");
            }

            // Clean filename to prevent path traversal attacks
            String cleanFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String filename = System.currentTimeMillis() + "_" + cleanFileName;

            // Delete old image if exists
            if (product.getImagePath() != null) {
                Path oldImagePath = root.resolve(product.getImagePath());
                if (Files.exists(oldImagePath)) {
                    Files.delete(oldImagePath);
                }
            }

            // Save new image
            Files.copy(file.getInputStream(),
                    root.resolve(filename),
                    StandardCopyOption.REPLACE_EXISTING);

            product.setImagePath(filename);
        }

        return productRepository.save(product);
    }

    // Delete product
    public void deleteProduct(Long id) throws IOException {
        Optional<Product> optionalProduct = productRepository.findById(id);

        if (optionalProduct.isEmpty()) {
            throw new IllegalArgumentException("Product not found with id: " + id);
        }

        Product product = optionalProduct.get();

        // Delete associated image file if exists
        if (product.getImagePath() != null) {
            Path imagePath = root.resolve(product.getImagePath());
            if (Files.exists(imagePath)) {
                Files.delete(imagePath);
            }
        }

        productRepository.deleteById(id);
    }
}


