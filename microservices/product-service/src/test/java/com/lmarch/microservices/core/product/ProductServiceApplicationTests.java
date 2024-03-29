package com.lmarch.microservices.core.product;

import com.lmarch.api.core.product.Product;
import com.lmarch.microservices.core.product.persistence.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.http.HttpStatus;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;
import static org.springframework.http.HttpStatus.*;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static reactor.core.publisher.Mono.just;

@SpringBootTest(webEnvironment = RANDOM_PORT)
class ProductServiceApplicationTests extends MongoDbTestBase{
	private static final int PRODUCT_ID_OK = 1;
	private static final int PRODUCT_ID_NOT_FOUND = 13;
	private static final String PRODUCT_ID_NOT_INTEGER = "not_integer";
	private static final int PRODUCT_ID_NEGATIVE = -1;

	@Autowired
	private WebTestClient client;

	@Autowired
	private ProductRepository repository;

	@BeforeEach
	void setupDb() {
		repository.deleteAll();
	}

	@Test
	void getProductById() {
		postAndVerifyProduct(PRODUCT_ID_OK, OK);

		assertTrue(repository.findByProductId(PRODUCT_ID_OK).isPresent());

		getAndVerifyProduct(PRODUCT_ID_OK, OK).jsonPath("$.productId").isEqualTo(PRODUCT_ID_OK);
	}

	@Test
	void duplicateError() {
		postAndVerifyProduct(PRODUCT_ID_OK, OK);

		assertTrue(repository.findByProductId(PRODUCT_ID_OK).isPresent());

		postAndVerifyProduct(PRODUCT_ID_OK, UNPROCESSABLE_ENTITY)	// 422
			.jsonPath("$.path").isEqualTo("/product")
			.jsonPath("$.message").isEqualTo("Duplicate key, Product Id: " + PRODUCT_ID_OK);
	}

	@Test
	void deleteProduct() {
		postAndVerifyProduct(PRODUCT_ID_OK, OK);
		assertTrue(repository.findByProductId(PRODUCT_ID_OK).isPresent());

		deleteAndVerifyProduct(PRODUCT_ID_OK, OK);
		assertFalse(repository.findByProductId(PRODUCT_ID_OK).isPresent());

		deleteAndVerifyProduct(PRODUCT_ID_OK, OK);
	}

	@Test
	void getProductInvalidParameterString() {
		getAndVerifyProduct(PRODUCT_ID_NOT_INTEGER, BAD_REQUEST)
			.jsonPath("$.path").isEqualTo("/product/" + PRODUCT_ID_NOT_INTEGER)
			.jsonPath("$.message").isEqualTo("Type mismatch.");
	}

	@Test
	void getProductNotFound() {
		getAndVerifyProduct(PRODUCT_ID_NOT_FOUND, NOT_FOUND)
			.jsonPath("$.path").isEqualTo("/product/" + PRODUCT_ID_NOT_FOUND)
			.jsonPath("$.message").isEqualTo("No product found for productId: " + PRODUCT_ID_NOT_FOUND);
	}

	@Test
	void getProductInvalidParameterNegativeValue() {
		getAndVerifyProduct(PRODUCT_ID_NEGATIVE, UNPROCESSABLE_ENTITY) // 422
			.jsonPath("$.path").isEqualTo("/product/" + PRODUCT_ID_NEGATIVE)
			.jsonPath("$.message").isEqualTo("Invalid productId: " + PRODUCT_ID_NEGATIVE);
	}

	private WebTestClient.BodyContentSpec postAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		Product product = new Product(productId, "Name " + productId, 2, "SA");

		return client.post()
			.uri("/product")
			.body(just(product), Product.class)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return getAndVerifyProduct("/" + productId, expectedStatus);
	}

	private WebTestClient.BodyContentSpec getAndVerifyProduct(String productIdPath, HttpStatus expectedStatus) {
		return client.get()
			.uri("/product/" + productIdPath)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectHeader().contentType(APPLICATION_JSON)
			.expectBody();
	}

	private WebTestClient.BodyContentSpec deleteAndVerifyProduct(int productId, HttpStatus expectedStatus) {
		return client.delete()
			.uri("/product/" + productId)
			.accept(APPLICATION_JSON)
			.exchange()
			.expectStatus().isEqualTo(expectedStatus)
			.expectBody();
	}
}
