package com.lmarch.api.core.recommendation;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface RecommendationService {

    /**
     * Sample usage.
     *
       curl -X POST $HOST:$PORT/recommendation -H "Content-Type: application/json" --data \
       '{"productId":123,"recommendationId":456,"author":"jon","rate":5,"content":"yga, yga, yga"}'
     *
     * @param recommendation A JSON representation of the new recommendation
     * @return A JSON representation of the newly created recommendation
     */
    @PostMapping(
        value = "/recommendation",
        consumes = "application/json",
        produces = "application/json")
    Recommendation createRecommendation(@RequestBody Recommendation recommendation);

    /**
     * Sample usage:
     *
       curl $HOST:$PORT/recommendation?productId=1
     *
     * @param productId ID of the product
     * @return the recommendations of the product
     */
    @GetMapping(value = "/recommendation", produces = "application/json")
    List<Recommendation> getRecommendations(
      @RequestParam(value = "productId") int productId
    );

    /**
     * Sample usage:
     *
       curl -X DELETE $HOST:$PORT/recommendation?productId=1
     *
     * @param productId ID of the product
     */
    @DeleteMapping(value = "/recommendation")
    void deleteRecommendations(@RequestParam(value = "productId") int productId);
}
