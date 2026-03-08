package com.pickncart.controller;

import com.pickncart.model.Cart;
import com.pickncart.model.Item;
import com.pickncart.model.Order;
import com.pickncart.model.OrderItem;
import com.pickncart.model.User;
import com.pickncart.service.CartService;
import com.pickncart.service.ItemService;
import com.pickncart.service.OrderService;
import com.pickncart.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private final ItemService itemService;
    private final CartService cartService;
    private final OrderService orderService;
    private final UserService userService;

    public WebController(ItemService itemService, CartService cartService, OrderService orderService, UserService userService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.userService = userService;
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !authentication.getName().equals("anonymousUser")) {
            Optional<User> user = userService.findByEmail(authentication.getName());
            return user.orElse(null);
        }
        return null;
    }

    @GetMapping("/")
    public String home(Model model) {
        List<Item> products = itemService.getAll();
        model.addAttribute("products", products);
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminDashboard(Model model) {
        model.addAttribute("totalUsers", userService.getAllUsers().size());
        model.addAttribute("totalProducts", itemService.getAll().size());
        model.addAttribute("totalOrders", orderService.getAllOrders().size());
        return "admin-dashboard";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("cartItems", cartService.getCartItems(currentUser));
            model.addAttribute("total", cartService.getTotal(currentUser));
        } else {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("total", 0.0);
        }
        return "cart";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("orders", orderService.getUserOrders(currentUser));
        } else {
            model.addAttribute("orders", List.of());
        }
        return "orders";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("cartItems", cartService.getCartItems(currentUser));
            model.addAttribute("total", cartService.getTotal(currentUser));
        } else {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("total", 0.0);
        }
        return "checkout";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", itemService.getAllCategories());
        return "categories";
    }

    @GetMapping("/category/{id}")
    public String categoryItems(@PathVariable Long id, Model model) {
        model.addAttribute("category", itemService.getCategoryById(id));
        model.addAttribute("items", itemService.getItemsByCategory(id));
        return "category-items";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long itemId,
                            @RequestParam(defaultValue = "1") int quantity,
                            RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to add items to cart");
            return "redirect:/login";
        }

        Optional<Item> itemOpt = itemService.getById(itemId);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Item not found");
            return "redirect:/";
        }

        Cart cart = new Cart();
        cart.setUser(currentUser);
        cart.setItem(itemOpt.get());
        cart.setQuantity(quantity);

        cartService.addToCart(cart);
        redirectAttributes.addFlashAttribute("success", "Item added to cart");
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String removeFromCart(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login");
            return "redirect:/login";
        }

        cartService.removeFromCart(id);
        redirectAttributes.addFlashAttribute("success", "Item removed from cart");
        return "redirect:/cart";
    }

    @PostMapping("/checkout")
    public String processCheckout(@RequestParam String address,
                                  @RequestParam String phone,
                                  @RequestParam String paymentMethod,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to checkout");
            return "redirect:/login";
        }

        List<Cart> cartItems = cartService.getCartItems(currentUser);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus("PLACED");

        List<OrderItem> orderItems = cartItems.stream().map(cart -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cart.getItem());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setPrice(cart.getItem().getPrice());
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal total = orderItems.stream()
                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);

        Order savedOrder = orderService.placeOrder(order, currentUser);

        cartItems.forEach(cart -> cartService.removeFromCart(cart.getId()));

        redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order ID: " + savedOrder.getId());
        return "redirect:/orders";
    }
}
