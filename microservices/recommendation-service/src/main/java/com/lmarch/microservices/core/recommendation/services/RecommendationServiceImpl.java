package com.lmarch.microservices.core.recommendation.services;

import com.lmarch.api.core.recommendation.Recommendation;
import com.lmarch.api.core.recommendation.RecommendationService;
import com.lmarch.api.exceptions.InvalidInputException;
import com.lmarch.microservices.core.recommendation.persistence.RecommendationEntity;
import com.lmarch.microservices.core.recommendation.persistence.RecommendationRepository;
import com.lmarch.util.http.ServiceUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class RecommendationServiceImpl implements RecommendationService {
    private static final Logger LOGGER = LoggerFactory.getLogger(RecommendationServiceImpl.class);

    private final RecommendationRepository repository;
    private final RecommendationMapper mapper;
    private final ServiceUtil serviceUtil;

    @Autowired
    public RecommendationServiceImpl(RecommendationRepository repository, RecommendationMapper mapper, ServiceUtil serviceUtil) {
        this.repository = repository;
        this.mapper = mapper;
        this.serviceUtil = serviceUtil;
    }

    @Override
    public Recommendation createRecommendation(Recommendation body) {
        try {
            RecommendationEntity entity = mapper.apiToEntity(body);
            RecommendationEntity newEntity = repository.save(entity);

            LOGGER.debug(
                "createRecommendation: created a body entity: {}/{}",
                body.getProductId(),
                body.getRecommendationId());

            return mapper.entityToApi(newEntity);

        } catch (DuplicateKeyException dke) {
            throw new InvalidInputException(
                "Duplicate key, Product Id: " + body.getProductId() + ", Recommendation Id:" + body.getRecommendationId());
        }
    }

    @Override
    public List<Recommendation> getRecommendations(int productId) {
        if (productId < 1) {
            throw new InvalidInputException("Invalid productId: " + productId);
        }

        List<RecommendationEntity> entityList = repository.findByProductId(productId);
        List<Recommendation> list = mapper.entityListToApiList(entityList);
        list.forEach(e -> e.setServiceAddress(serviceUtil.getServiceAddress()));

        LOGGER.debug("getRecommendations: response size: {}", list.size());

        return list;
    }

    @Override
    public void deleteRecommendations(int productId) {
        LOGGER.debug("deleteRecommendations: tries to delete recommendations for the product with productId: {}", productId);

        repository.deleteAll(repository.findByProductId(productId));
    }
}
