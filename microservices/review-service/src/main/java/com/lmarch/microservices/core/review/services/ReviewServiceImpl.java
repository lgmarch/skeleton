package com.lmarch.microservices.core.review.services;

import com.lmarch.api.core.review.Review;
import com.lmarch.api.core.review.ReviewService;
import com.lmarch.api.exceptions.InvalidInputException;
import com.lmarch.microservices.core.review.persistence.ReviewEntity;
import com.lmarch.microservices.core.review.persistence.ReviewRepository;
import com.lmarch.util.http.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class ReviewServiceImpl implements ReviewService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewServiceImpl.class);

    private final ReviewRepository repository;
    private final ReviewMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public ReviewServiceImpl(ReviewRepository repository, ReviewMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Review createReview(Review review) {
        try {
            ReviewEntity entity = mapper.apiToEntity(review);
            ReviewEntity newEntity = repository.save(entity);

            LOGGER.debug("createReview: created a review entity: {}/{}", review.getProductId(), review.getReviewId());

            return mapper.entityToApi(newEntity);

        } catch (DataIntegrityViolationException dive) {
            throw new InvalidInputException(
                "Duplicate key, Product Id: " + review.getProductId() + ", Review Id:" + review.getReviewId());
        }

    }

    @Override
    public List<Review> getReviews(int productId) {

        if (productId <1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<ReviewEntity> entityList = repository.findByProductId(productId);
        List<Review> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOGGER.debug("getReviews: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteReviews(int productId) {
        LOGGER.debug("deleteReviews: tries to delete reviews for the product with productId: {}", productId);

        repository.deleteAll(repository.findByProductId(productId));
    }
}
