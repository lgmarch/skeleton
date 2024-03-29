package com.lmarch.microservices.core.review;

import com.lmarch.api.core.review.Review;
import com.lmarch.microservices.core.review.persistence.ReviewRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.test.web.reactive.server.WebTestClient;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ReviewServiceApplicationTests extends MySqlTestBase {
	private static final int PRODUCT_ID = 1;
	private static final int REVIEW_ID = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 213;
	private static final String PRODUCT_ID_NOT_INTEGER = "not_integer";
	private static final int PRODUCT_ID_NEGATIVE = -1;

	@Autowired
	private WebTestClient client;
	@Autowired
	private ReviewRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getReviewsByProductId() {
		assertEquals(0, repository.findByProductId(PRODUCT_ID).size());

		postAndVerifyReview(PRODUCT_ID, 1, OK);
		postAndVerifyReview(PRODUCT_ID, 2, OK);
		postAndVerifyReview(PRODUCT_ID, 3, OK);

		assertEquals(3, repository.findByProductId(PRODUCT_ID).size());

		getAndVerifyReviewsByProductId(PRODUCT_ID, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].productId").isEqualTo(PRODUCT_ID)
			.jsonPath("$[2].reviewId").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		assertEquals(0, repository.count());

		postAndVerifyReview(PRODUCT_ID, REVIEW_ID, OK)
			.jsonPath("$.productId").isEqualTo(PRODUCT_ID)
			.jsonPath("$.reviewId").isEqualTo(REVIEW_ID);

		assertEquals(1, repository.count());

		postAndVerifyReview(PRODUCT_ID, REVIEW_ID, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Review Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	void deleteReviews() {
		postAndVerifyReview(PRODUCT_ID, REVIEW_ID, OK);
		assertEquals(1, repository.count());

		deleteAndVerifyReviewsByProductId(PRODUCT_ID, OK);
		assertEquals(0, repository.findByProductId(PRODUCT_ID).size());

		deleteAndVerifyReviewsByProductId(PRODUCT_ID, OK);
	}

	@Test
	void getReviewsMissingParameter() {
		getAndVerifyReviewsByProductId("", BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getReviewsInvalidParameter() {
		getAndVerifyReviewsByProductId("?productId=" + PRODUCT_ID_NOT_INTEGER, BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getReviewsNotFound() {
		getAndVerifyReviewsByProductId("?productId=" + PRODUCT_ID_NOT_FOUND, OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getReviewsInvalidParameterNegativeValue() {
		getAndVerifyReviewsByProductId("?productId=" + PRODUCT_ID_NEGATIVE, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/review")
			.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_NEGATIVE);
	}

	private WebTestClient.BodyContentSpec postAndVerifyReview(int productId, int reviewId, HttpStatus expectedStatus) {
		Review review = new Review(productId, reviewId, "A " + reviewId, "S " + reviewId, "C " + reviewId, "SA");

		return client.post()
			.uri("/review")
			.body(just(review), Review.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return getAndVerifyReviewsByProductId("?productId=" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyReviewsByProductId(String productId, HttpStatus expectedStatus) {
		return client.get()
			.uri("/review" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyReviewsByProductId(int productId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/review?productId=" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
