

# Build and start the microservice product
`cd <field of project>` <br>
`./gradlew build` <br>
`java -jar microservices/product-service/build/libs/*.jar &`

### Make a test call to the product service:
`curl http://localhost:7001/product/123`

# Docker
Start the microservice landscape.
`cd skeleton`<br>
`./gradlew clean build && docker-compose build && docker-compose up -d`

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

`cd skeleton`

`./gradlew clean build && docker-compose build && docker-compose up -d`

http://localhost:8080/openapi/swagger-ui.html

<a href="http://localhost:8080/openapi/swagger-ui.html">Open API</a>

### Run tests
if the microservice landscape is not starting

`./test-em-all.bash start stop`

else

`./test-em-all.bash`

## Start the system landscape (all microservices)
### The MongoDB and MySQL CLI tools
To start the MongoDB CLI tool and mongo, inside the mongodb container, run the following command:

`
docker-compose exec mongodb mongosh ––quiet
`
Введите exit, чтобы покинуть интерфейс mongo CLI.
To start the MySQL CLI tool, mysql, inside the mysql container and log in to review-db using the user
created at startup, run the following command:

`
docker-compose exec mysql mysql -uuser -p review-db
`

Enter exit to leave the mysql CLI.

Build and start the system landscape with the following command:

`
./gradlew build && docker-compose build && docker-compose up
`
