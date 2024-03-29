package com.lmarch.microservices.core.product.services;

import com.lmarch.api.core.product.Product;
import com.lmarch.api.core.product.ProductService;
import com.lmarch.api.exceptions.InvalidInputException;
import com.lmarch.api.exceptions.NotFoundException;
import com.lmarch.microservices.core.product.persistence.ProductEntity;
import com.lmarch.microservices.core.product.persistence.ProductRepository;
import com.lmarch.util.http.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ProductServiceImpl implements ProductService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProductServiceImpl.class);

    private final ProductRepository repository;
    private final ProductMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ProductServiceImpl(ProductRepository repository, ProductMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Product createProduct(Product product) {
        try {
            ProductEntity entity = mapper.apiToEntity(product);
            ProductEntity newEntity = repository.save(entity);

            LOGGER.debug("createProduct: entity created for productId: {}", product.getProductId());

            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException("Duplicate key, Product Id: " + product.getProductId());
        }
    }

    @Override
    public Product getProduct(int productId) {

        if (productId <1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        ProductEntity entity = repository.findByProductId(productId)
            .orElseThrow(() -> new NotFoundException("No product found for productId: " + productId));

        Product response = mapper.entityToApi(entity);
        response.setServiceAddress(serviceUtil.getServiceAddress());

        LOGGER.debug("getProduct: found productId: {}", response.getProductId());

        return response;
    }

    @Override
    public void deleteProduct(int productId) {
        LOGGER.debug("deleteProduct: tries to delete an entity with productId: {}", productId);

        repository.findByProductId(productId).ifPresent(repository::delete);
    }
}
