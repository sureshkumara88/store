# Enhancements

1. Extended the order endpoint to find a specific order, by ID
    * Updated order responses to include the list of associated products

2. Extended the customer endpoint to support case-insensitive substring search on customer names
    * Added `/api/v1/customers/search` endpoint
    * Optimized query performance using Postgres trigram indexes (`pg_trgm`) and functional indexes on `customer.name`

3. Improved performance of GET endpoints
    * Added indexes on `order_product` and `order.customer_id` for faster joins
    * Implemented sequence restarts to avoid primary key collisions with seeded data
    * Tuned Hibernate/JPA fetch strategies:
        * Used `@EntityGraph` in `CustomerRepository`, `OrderRepository` and `ProductRepository` files for eager loading of `customers`, `orders` and `products`
          * `@EntityGraph` is defense against N+1 queries, ensuring fewer, more efficient queries while fulfilling the performance optimization
        * Configured batching and fetch sizes in `application.yaml` (`hibernate.jdbc.fetch_size`, `hibernate.jdbc.batch_size`)

4. Added a new `/api/v1/products` endpoint to model products which appear in an order
    * A single order to contain 1 or more products
    * A product has an ID and a description
    * Added a POST endpoint to create a product
    * Added a GET endpoint to return all products, and a specific product by ID
        * In both cases, return a list of the order IDs which contain those products
    * Updated the orders endpoint to return a list of products contained in the order

5. Enhanced error handling
    * Introduced a centralized `GlobalExceptionHandler` for validation and persistence errors
    * Standardized error response format across endpoints

6. Expanded test coverage
    * Added/updated tests for:
        * `CustomerControllerTests`
        * `OrderControllerTests`
        * `ProductControllerTests`

7. API Documentation Improvements
    * Refactored the `OpenAPI.yaml` file to match the latest implemented changes:
        * Order by ID retrieval
        * Customer search endpoint
        * Product endpoints (create, list, get by ID, include order IDs)
        * Updated error schema for consistent responses
    * Moved the OpenAPI spec into the static resources location (`src/main/resources/static`)
    * Integrated Swagger UI via springdoc so the API documentation is now accessible at:
        * [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)
