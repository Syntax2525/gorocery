# PicknCart - E-commerce Application

A modern e-commerce platform built with Spring Boot and Thymeleaf for the Dar es Salaam market.

## Features

### Frontend Features
- **Responsive Design**: Mobile-first approach with modern UI/UX
- **Product Catalog**: Browse products by categories
- **Shopping Cart**: Add/remove items with real-time updates
- **User Authentication**: Secure login and registration
- **Order Management**: View order history and reorder
- **Profile Management**: Update personal information and addresses
- **Search Functionality**: Find products quickly
- **Checkout Process**: Multiple payment options including mobile money

### Backend Features
- **Spring Boot**: Modern Java backend framework
- **Spring Security**: Authentication and authorization
- **Spring Data JPA**: Database operations with MySQL
- **RESTful APIs**: Clean API design for frontend integration
- **JWT Authentication**: Secure token-based authentication
- **Thymeleaf Templates**: Server-side rendering for better SEO

## Project Structure

```
src/
├── main/
│   ├── java/com/pickncart/
│   │   ├── PickncartApplication.java     # Main application class
│   │   ├── config/
│   │   │   └── SecurityConfig.java       # Security configuration
│   │   ├── controller/
│   │   │   ├── WebController.java         # Frontend page controllers
│   │   │   ├── AuthController.java        # Authentication API
│   │   │   ├── ItemController.java        # Product management
│   │   │   ├── CartController.java       # Shopping cart
│   │   │   ├── OrderController.java      # Order management
│   │   │   ├── UserController.java       # User management
│   │   │   ├── CategoryController.java   # Category management
│   │   │   └── SuggestionController.java # Product suggestions
│   │   ├── model/                        # Entity classes
│   │   ├── repository/                   # Data access layer
│   │   └── service/                      # Business logic
│   └── resources/
│       ├── static/
│       │   ├── style.css                 # Main stylesheet
│       │   ├── script.js                 # Frontend JavaScript
│       │   └── images/                   # Product and category images
│       └── templates/
│           ├── layout.html               # Base template
│           ├── home.html                 # Homepage
│           ├── login.html                # Login page
│           ├── register.html             # Registration page
│           ├── cart.html                # Shopping cart
│           ├── checkout.html             # Checkout process
│           ├── profile.html              # User profile
│           ├── orders.html               # Order history
│           ├── categories.html           # Product categories
│           └── category-items.html       # Category products
```

## Technology Stack

### Backend
- **Java 25** - Programming language
- **Spring Boot 4.0.3** - Application framework
- **Spring Security** - Authentication and authorization
- **Spring Data JPA** - Database operations
- **MySQL** - Database
- **JWT** - Token-based authentication
- **Lombok** - Code generation

### Frontend
- **Thymeleaf** - Template engine
- **HTML5/CSS3** - Markup and styling
- **JavaScript (ES6+)** - Client-side interactions
- **Responsive Design** - Mobile-first approach

## Getting Started

### Prerequisites
- Java 25 or higher
- MySQL 8.0 or higher
- Maven 3.6 or higher

### Database Setup
1. Create a MySQL database named `mayenze`
2. Update database credentials in `application.properties`:
   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3307/mayenze?allowPublicKeyRetrieval=true&useSSL=false&serverTimezone=UTC
   spring.datasource.username=root
   spring.datasource.password=your_password
   ```

### Running the Application
1. Clone the repository
2. Navigate to the project directory
3. Run the application:
   ```bash
   mvn spring-boot:run
   ```
4. Access the application at `http://localhost:8080`

## API Endpoints

### Authentication
- `POST /api/auth/login` - User login
- `POST /api/auth/register` - User registration

### Products
- `GET /api/items` - Get all products
- `GET /api/items/search?name={query}` - Search products
- `POST /api/items` - Create product (admin)

### Cart
- `POST /api/cart/add/{id}` - Add item to cart
- `DELETE /api/cart/remove/{id}` - Remove item from cart
- `GET /api/cart/count` - Get cart item count

### Orders
- `POST /api/orders/create` - Create new order
- `GET /api/orders/user` - Get user orders
- `POST /api/orders/reorder/{id}` - Reorder previous order

## Frontend Pages

- `/` - Homepage with product listings
- `/login` - User login
- `/register` - User registration
- `/cart` - Shopping cart
- `/checkout` - Checkout process
- `/profile` - User profile management
- `/orders` - Order history
- `/categories` - Product categories
- `/category/{id}` - Products in specific category

## Security Features

- **Password Encryption**: BCrypt hashing
- **JWT Tokens**: Secure authentication
- **CSRF Protection**: Cross-site request forgery prevention
- **Session Management**: Secure session handling
- **Input Validation**: Form validation and sanitization

## Payment Integration

The application supports:
- **Cash on Delivery** - Traditional payment method
- **Mobile Money** - Tigo Pesa and M-Pesa integration ready

## Deployment

### Production Deployment
1. Build the application:
   ```bash
   mvn clean package
   ```
2. Deploy the JAR file to your server
3. Configure production database settings
4. Set up SSL certificate for HTTPS

### Docker Deployment
```dockerfile
FROM openjdk:25-jdk-slim
COPY target/pickncart-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests for new functionality
5. Submit a pull request

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Support

For support and questions:
- Email: support@pickncart.co.tz
- Phone: +255 123 456 789
- Location: Dar es Salaam, Tanzania

---

© 2026 PicknCart - Dar es Salaam, Tanzania
