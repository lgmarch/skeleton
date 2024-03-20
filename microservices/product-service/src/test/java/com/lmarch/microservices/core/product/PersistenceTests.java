package com.lmarch.microservices.core.product;

import com.lmarch.microservices.core.product.persistence.ProductEntity;
import com.lmarch.microservices.core.product.persistence.ProductRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static java.util.stream.IntStream.rangeClosed;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.data.domain.Sort.Direction.ASC;

@DataMongoTest
public class PersistenceTests extends MongoDbTestBase{

    @Autowired
    private ProductRepository repository;
    private ProductEntity savedEntity;

    @BeforeEach
    void setupDb() {
        repository.deleteAll();

        ProductEntity entity = new ProductEntity(1, "n", 1);
        savedEntity = repository.save(entity);

        assertEqualsProduct(entity, savedEntity);
    }

    @Test
    void create() {
        ProductEntity newEntity = new ProductEntity(2, "n", 2);
        repository.save(newEntity);

        ProductEntity foundEntity = repository.findById(newEntity.getId()).get();
        assertEqualsProduct(newEntity, foundEntity);

        assertEquals(2, repository.count());
    }

    @Test
    void getByProductId() {
        Optional<ProductEntity> entity = repository.findByProductId(savedEntity.getProductId());
        assertTrue(entity.isPresent());
        assertEqualsProduct(savedEntity, entity.get());
    }

    @Test
    void update() {
        savedEntity.setName("n2");
        repository.save(savedEntity);

        ProductEntity foundEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (long)foundEntity.getVersion());
        assertEquals("n2", foundEntity.getName());
    }

    @Test
    void delete() {
        repository.delete(savedEntity);
        assertFalse(repository.existsById(savedEntity.getId()));
    }

    @Test
    void duplicateError() {
        assertThrows(DuplicateKeyException.class, () -> {
            ProductEntity entity = new ProductEntity(savedEntity.getProductId(), "n", 1);
            repository.save(entity);
        });
    }

    @Test
    void optimisticLockError() {

        // Берем из базы продукт и созраняем его в разных Entity
        ProductEntity entity1 = repository.findById(savedEntity.getId()).get();
        ProductEntity entity2 = repository.findById(savedEntity.getId()).get();

        // Изменяем первый Entity и пишем его в базу
        entity1.setName("n1");
        repository.save(entity1);

        // Изменяем втрой Entity и пишем в базу.
        // Это должно завершиться ошибкой, поскольку второй объект теперь содержит старый номер версии,
        // т.е. получим Optimistic Lock Error
        assertThrows(OptimisticLockingFailureException.class, () -> {
            entity2.setName("n2");
            repository.save(entity2);
        });

        // Получаем обновленный Entity из базы и проверяем его на новое состояние
        ProductEntity updatedEntity = repository.findById(savedEntity.getId()).get();
        assertEquals(1, (int)updatedEntity.getVersion());
        assertEquals("n1", updatedEntity.getName());
    }

    @Test
    void paging() {

        repository.deleteAll();

        List<ProductEntity> newProducts = rangeClosed(1001, 1010)
                .mapToObj(i -> new ProductEntity(i, "name " + i, i))
                .collect(Collectors.toList());
        repository.saveAll(newProducts);

        Pageable nextPage = PageRequest.of(0, 4, ASC, "productId");
        nextPage = testNextPage(nextPage, "[1001, 1002, 1003, 1004]", true);
        nextPage = testNextPage(nextPage, "[1005, 1006, 1007, 1008]", true);
        nextPage = testNextPage(nextPage, "[1009, 1010]", false);
    }

    private Pageable testNextPage(Pageable nextPage, String expectedProductIds, boolean expectsNextPage) {
        Page<ProductEntity> productPage = repository.findAll(nextPage);
        assertEquals(expectedProductIds, productPage.getContent().stream().map(p -> p.getProductId()).toList().toString());
        assertEquals(expectsNextPage, productPage.hasNext());

        return productPage.nextPageable();
    }

    private void assertEqualsProduct(ProductEntity expectedEntity, ProductEntity actualEntity) {
        assertEquals(expectedEntity.getId(),               actualEntity.getId());
        assertEquals(expectedEntity.getVersion(),          actualEntity.getVersion());
        assertEquals(expectedEntity.getProductId(),        actualEntity.getProductId());
        assertEquals(expectedEntity.getName(),           actualEntity.getName());
        assertEquals(expectedEntity.getWeight(),           actualEntity.getWeight());
    }
}
