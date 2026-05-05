package com.pickncart;

import com.pickncart.model.Category;
import com.pickncart.model.Item;
import com.pickncart.model.Order;
import com.pickncart.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockServletContext;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.web.servlet.JakartaServletWebApplication;
import org.thymeleaf.context.WebContext;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class AdminTemplateRenderingTest {

    private final SpringTemplateEngine templateEngine = templateEngine();

    @Test
    void adminTemplatesRenderWithTypicalModelData() {
        Map<String, Object> model = new HashMap<>();
        model.put("_csrf", new CsrfView("token", "X-CSRF-TOKEN"));

        User admin = new User();
        admin.setId(1L);
        admin.setFirstName("Admin");
        admin.setLastName("User");
        admin.setEmail("admin@example.com");
        admin.setUsername("admin@example.com");
        admin.setRole("ADMIN");

        Category category = new Category();
        category.setId(1L);
        category.setName("Groceries");
        category.setDescription("Daily shopping");

        Item product = new Item();
        product.setId(1L);
        product.setName("Milk");
        product.setDescription("Fresh local milk");
        product.setPrice(BigDecimal.valueOf(2500));
        product.setStock(12);
        product.setCategory(category);

        Order order = new Order();
        order.setId(1L);
        order.setUser(admin);
        order.setStatus("DELIVERED");
        order.setTotalAmount(BigDecimal.valueOf(2500));
        order.setCreatedAt(LocalDateTime.now());

        model.put("totalUsers", 1);
        model.put("totalProducts", 1);
        model.put("totalOrders", 1);
        model.put("deliveredRevenue", BigDecimal.valueOf(2500));
        model.put("users", List.of(admin));
        model.put("orders", List.of(order));
        model.put("products", List.of(product));
        model.put("categories", List.of(category));

        WebContext context = webContext(model);
        assertThat(templateEngine.process("admin-dashboard", context)).contains("Dashboard Overview");
        assertThat(templateEngine.process("admin-orders", context)).contains("Order Management");
        assertThat(templateEngine.process("admin-products", context)).contains("Manage Products");
        assertThat(templateEngine.process("admin-users", context)).contains("User Management");
        assertThat(templateEngine.process("admin-categories", context)).contains("Manage Categories");
    }

    private SpringTemplateEngine templateEngine() {
        ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
        resolver.setPrefix("templates/");
        resolver.setSuffix(".html");
        resolver.setTemplateMode("HTML");
        resolver.setCharacterEncoding("UTF-8");
        resolver.setCacheable(false);

        SpringTemplateEngine engine = new SpringTemplateEngine();
        engine.setTemplateResolver(resolver);
        return engine;
    }

    private WebContext webContext(Map<String, Object> model) {
        MockServletContext servletContext = new MockServletContext();
        MockHttpServletRequest request = new MockHttpServletRequest(servletContext);
        MockHttpServletResponse response = new MockHttpServletResponse();
        JakartaServletWebApplication application = JakartaServletWebApplication.buildApplication(servletContext);
        return new WebContext(application.buildExchange(request, response), request.getLocale(), model);
    }

    static class CsrfView {
        private final String token;
        private final String headerName;

        CsrfView(String token, String headerName) {
            this.token = token;
            this.headerName = headerName;
        }

        public String getToken() {
            return token;
        }

        public String getHeaderName() {
            return headerName;
        }
    }
}
