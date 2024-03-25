package com.lmarch.api.core.review;

import org.springframework.web.bind.annotation.*;

import java.util.List;

public interface ReviewService {

    /**
     * Sample usage:
     *
       curl -X POST $HOST:$PORT/review -H "Content-Type: application/json" --data \
       '{"productId":123,"reviewId":456,"author":"me","subject":"yga, yga, yga","content":"yga, yga, yga"}'
     *
     * @param review A JSON representation of the new review
     * @return A JSON representation of the newly created review
     */
    @PostMapping(
        value = "/review",
        consumes = "application/json",
        produces = "application/json")
    Review createReview(@RequestBody Review review);

    /**
     * Sample usage:
     *
       curl $HOST:$PORT/review?productId=1
     *
     * @param productId ID of the product
     * @return the reviews of the product
     */
    @GetMapping(value = "/review", produces = "application/json")
    List<Review> getReviews(@RequestParam(value = "productId") int productId);

    /**
     * Sample usage:
     *
       curl -X DELETE $HOST:$POST/review?productId=1
     *
     * @param productId ID of the product
     */
    @DeleteMapping(value = "/review")
    void deleteReviews(@RequestParam(value = "productId") int productId);
}
