package com.lmarch.microservices.core.recommendation.services;

import com.lmarch.api.core.recommendation.Recommendation;
import com.lmarch.microservices.core.recommendation.persistence.RecommendationEntity;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.springframework.stereotype.Component;

import java.util.List;

@Mapper(componentModel = "spring")
@Component
public interface RecommendationMapper {

    @Mappings({
        @Mapping(target = "rate", source = "entity.rating"),
        @Mapping(target = "serviceAddress", ignore = true)
    })
    Recommendation entityToApi(RecommendationEntity entity);

    @Mappings({
        @Mapping(target = "rating", source = "api.rate"),
        @Mapping(target = "id", ignore = true),
        @Mapping(target = "version", ignore = true)
    })
    RecommendationEntity apiToEntity(Recommendation api);

    List<Recommendation> entityListToApiList(List<RecommendationEntity> entityList);

    List<RecommendationEntity> apiListToEntityList(List<Recommendation> apiList);
}
