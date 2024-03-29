package com.lmarch.microservices.core.recommendation;

import com.lmarch.api.core.recommendation.Recommendation;

import com.lmarch.microservices.core.recommendation.persistence.RecommendationRepository;
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
class RecommendationServiceApplicationTests extends MongoDbTestBase {
	private static final int PRODUCT_ID = 1;
	private static final int RECOMMENDATION_ID = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 113;
	private static final String PRODUCT_ID_NOT_INTEGER = "not_integer";
	private static final int PRODUCT_ID_NEGATIVE = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private RecommendationRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getRecommendationsByProductId() {
		postAndVerifyRecommendation(PRODUCT_ID, 1, OK);
		postAndVerifyRecommendation(PRODUCT_ID, 2, OK);
		postAndVerifyRecommendation(PRODUCT_ID, 3, OK);

		assertEquals(3, repository.findByProductId(PRODUCT_ID).size());

		getAndVerifyRecommendationsByProductId(PRODUCT_ID, OK)
			.jsonPath("$.length()").isEqualTo(3)
			.jsonPath("$[2].productId").isEqualTo(PRODUCT_ID)
			.jsonPath("$[2].recommendationId").isEqualTo(3);
	}

	@Test
	void duplicateError() {
		postAndVerifyRecommendation(PRODUCT_ID, RECOMMENDATION_ID, OK)
			.jsonPath("$.productId").isEqualTo(PRODUCT_ID)
			.jsonPath("$.recommendationId").isEqualTo(RECOMMENDATION_ID);

		assertEquals(1, repository.count());

		postAndVerifyRecommendation(PRODUCT_ID, RECOMMENDATION_ID, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: 1, Recommendation Id:1");

		assertEquals(1, repository.count());
	}

	@Test
	void deleteRecommendations() {
		postAndVerifyRecommendation(PRODUCT_ID, RECOMMENDATION_ID, OK);
		assertEquals(1, repository.findByProductId(PRODUCT_ID).size());

		deleteAndVerifyRecommendationsByProductId(PRODUCT_ID, OK);
		assertEquals(0, repository.findByProductId(PRODUCT_ID).size());

		deleteAndVerifyRecommendationsByProductId(PRODUCT_ID, OK);
	}

	@Test
	void getRecommendationsMissingParameter() {
		getAndVerifyRecommendationsByProductId("", BAD_REQUEST)	// 400
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Required query parameter 'productId' is not present.");
	}

	@Test
	void getRecommendationsInvalidParameter() {
		getAndVerifyRecommendationsByProductId("?productId=" + PRODUCT_ID_NOT_INTEGER, BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getRecommendationsNotFound() {
		getAndVerifyRecommendationsByProductId("?productId=" + PRODUCT_ID_NOT_FOUND, OK)
			.jsonPath("$.length()").isEqualTo(0);
	}

	@Test
	void getRecommendationsInvalidParameterNegativeValue() {
		getAndVerifyRecommendationsByProductId("?productId=" + PRODUCT_ID_NEGATIVE, UNPROCESSABLE_ENTITY)
			.jsonPath("$.path").isEqualTo("/recommendation")
			.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_NEGATIVE);
	}

	private WebTestClient.BodyContentSpec postAndVerifyRecommendation(int productId, int recommendationId, HttpStatus expectedStatus) {
		Recommendation recommendation = new Recommendation(
			productId, recommendationId, "Author " + recommendationId, 5, "Content " + recommendationId, "SA");

		return client.post()
			.uri("/recommendation")
			.body(just(recommendation), Recommendation.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(int productId, HttpStatus status) {
		return getAndVerifyRecommendationsByProductId("?productId=" + productId, status);
	}

	private WebTestClient.BodyContentSpec getAndVerifyRecommendationsByProductId(String productId, HttpStatus status) {
		return client.get()
			.uri("/recommendation" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(status)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyRecommendationsByProductId(int productId, HttpStatus status) {
		return client.delete()
			.uri("/recommendation?productId=" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(status)
			.expectBody();
	}
}
