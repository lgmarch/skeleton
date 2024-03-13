# Adding REST full APIs

# Implementing API
### Add the api and util projects as dependencies to our build.gradle file

`dependencies {
  implementation project(':api')
  implementation project(':util')
`
### Detect Spring Beans in the api and util projects
To enable Spring Boot’s autoconfiguration feature to detect Spring Beans in the api and util
projects, we also need to add a @ComponentScan annotation to the main application class, which
includes the packages of the api and util projects

`@SpringBootApplication
@ComponentScan("se.magnus")
public class ProductServiceApplication {
`

### Create our service implementation file
Create our service implementation file, ProductServiceImpl.java, in order to implement
the Java interface, ProductService, from the api project and annotate the class with
@RestController so that Spring will call the methods in this class according to the mappings
specified in the Interface class

### Add to the property file application.yml
Need to set up some runtime properties – what port to use and the desired
level of logging.

`
server.port: 7003
server.error.include-message: always
logging:
level:
root: INFO
com.lmarch: DEBUG
`
### Build and start the microservice
`
cd <field of project>
./gradlew build
java -jar microservices/product-service/build/libs/*.jar &
`
Make a test call to the product service:

`curl http://localhost:7001/product/123`
