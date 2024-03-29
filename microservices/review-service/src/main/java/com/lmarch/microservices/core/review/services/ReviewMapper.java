package com.lmarch.microservices.core.review.services;

import com.lmarch.api.core.review.Review;
import com.lmarch.microservices.core.review.persistence.ReviewEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface ReviewMapper {

    @Mappings({
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Review entityToApi(ReviewEntity entity);

    @Mappings({
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    ReviewEntity apiToEntity(Review api);

    List<Review> entityListToApiList(List<ReviewEntity> entityList);

    List<ReviewEntity> apiListToEntityList(List<Review> apiList);
}
