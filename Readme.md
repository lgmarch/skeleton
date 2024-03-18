

# Build and start the microservice product
`cd <field of project>` <br>
`./gradlew build` <br>
`java -jar microservices/product-service/build/libs/*.jar &`

### Make a test call to the product service:
`curl http://localhost:7001/product/123`

# Docker
`./gradlew clean build`
`docker-compose build`
`docker-compose up -d`
`docker ps`
`docker-compose restart product`
Stop the microservice landscape.
`docker-compose down`

### Logging
`docker-compose logs -f`
`docker-compose logs product review`

### Running tests
`./gradlew clean build && docker-compose build && ./test-em-all.bash start stop`

### Make a test call to the product-composite service:
`curl localhost:8080/product-composite/123 -s | jq .`

# OpenAPI documentation
Before need to build and start the microservice landscape.
This can be done with the following commands:

`cd skeleton`<br>
`./gradlew clean build && docker-compose build && docker-compose up -d`

### 
<a href="http://localhost:8080/openapi/swagger-ui.html">Open API</a>

### Run tests
if the microservice landscape is not starting

`./test-em-all.bash start stop`

else

`./test-em-all.bash`
