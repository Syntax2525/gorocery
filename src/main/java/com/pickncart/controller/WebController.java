package com.pickncart.controller;

import com.pickncart.model.Cart;
import com.pickncart.model.Item;
import com.pickncart.model.Order;
import com.pickncart.model.OrderItem;
import com.pickncart.model.User;
import com.pickncart.service.CartService;
import com.pickncart.service.DeliveryFeeService;
import com.pickncart.service.ItemService;
import com.pickncart.service.OrderService;
import com.pickncart.service.UserService;
import com.pickncart.util.RoleUtils;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
public class WebController {

    private static final Path PROFILE_UPLOAD_DIR = Paths.get("uploads", "profiles");

    private final ItemService itemService;
    private final CartService cartService;
    private final DeliveryFeeService deliveryFeeService;
    private final OrderService orderService;
    private final UserService userService;

    public WebController(ItemService itemService,
                         CartService cartService,
                         DeliveryFeeService deliveryFeeService,
                         OrderService orderService,
                         UserService userService) {
        this.itemService = itemService;
        this.cartService = cartService;
        this.deliveryFeeService = deliveryFeeService;
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
        List<Item> products = itemService.getInStockItems();
        model.addAttribute("products", products);
        model.addAttribute("categories", itemService.getAllCategories());
        model.addAttribute("currentUser", getCurrentUser());
        return "home";
    }

    @GetMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {
        if (error != null) {
            model.addAttribute("error", "Invalid email or password");
        }
        if (logout != null) {
            model.addAttribute("success", "You have been logged out");
        }
        return "login";
    }

    @GetMapping("/cart")
    public String cart(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            List<Cart> cartItems = cartService.getCartItems(currentUser);
            BigDecimal subtotal = cartSubtotal(cartItems);
            BigDecimal deliveryFee = deliveryFeeService.calculateForCart(cartItems, currentUser.getDistrict());
            model.addAttribute("cartItems", cartItems);
            model.addAttribute("total", subtotal);
            model.addAttribute("deliveryFee", deliveryFee);
            model.addAttribute("grandTotal", subtotal.add(deliveryFee));
        } else {
            model.addAttribute("cartItems", List.of());
            model.addAttribute("total", BigDecimal.ZERO);
            model.addAttribute("deliveryFee", BigDecimal.ZERO);
            model.addAttribute("grandTotal", BigDecimal.ZERO);
        }
        model.addAttribute("currentUser", getCurrentUser());
        return "cart";
    }

    @GetMapping("/profile")
    public String profile(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            model.addAttribute("user", currentUser);
            List<Order> userOrders = orderService.getUserOrders(currentUser);
            if (!userOrders.isEmpty()) {
                model.addAttribute("lastOrder", userOrders.get(0));
            }
        }
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
        model.addAttribute("currentUser", getCurrentUser());
        return "orders";
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        if (RoleUtils.isAdmin(currentUser)) {
            return "redirect:/admin/dashboard";
        }
        List<Cart> cartItems = cartService.getCartItems(currentUser);
        BigDecimal subtotal = cartSubtotal(cartItems);
        BigDecimal deliveryFee = deliveryFeeService.calculateForCart(cartItems, currentUser.getDistrict());
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", subtotal);
        model.addAttribute("deliveryFee", deliveryFee);
        model.addAttribute("grandTotal", subtotal.add(deliveryFee));
        model.addAttribute("deliveryFeeOptions", deliveryFeeService.calculateDistrictOptionsForCart(cartItems));
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("user", currentUser);
        return "checkout";
    }

    @GetMapping("/categories")
    public String categories(Model model) {
        model.addAttribute("categories", itemService.getAllCategories());
        model.addAttribute("currentUser", getCurrentUser());
        return "categories";
    }

    @GetMapping("/category/{id}")
    public String categoryItems(@PathVariable Long id, Model model) {
        var category = itemService.getCategoryById(id);
        if (category == null) {
            return "redirect:/categories";
        }
        model.addAttribute("category", category);
        model.addAttribute("items", itemService.getItemsByCategory(id));
        model.addAttribute("currentUser", getCurrentUser());
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
        if (RoleUtils.isAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Admins manage the store from the dashboard and cannot shop as customers");
            return "redirect:/admin/dashboard";
        }

        Optional<Item> itemOpt = itemService.getById(itemId);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Item not found");
            return "redirect:/";
        }

        Item item = itemOpt.get();
        if (item.getStock() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Insufficient stock");
            return "redirect:/";
        }

        cartService.addOrUpdateCart(currentUser, item, quantity);
        redirectAttributes.addFlashAttribute("success", "Item added to cart");
        return "redirect:/cart";
    }

    @PostMapping("/cart/update/{id}")
    public String updateCartQuantity(@PathVariable Long id,
                                     @RequestParam int quantity,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }
        cartService.updateQuantity(id, quantity);
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
    public String processCheckout(@RequestParam String fullName,
                                  @RequestParam String phone,
                                  @RequestParam String address,
                                  @RequestParam String district,
                                  @RequestParam String paymentMethod,
                                  RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to checkout");
            return "redirect:/login";
        }
        if (RoleUtils.isAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Admins cannot place customer orders");
            return "redirect:/admin/dashboard";
        }

        List<Cart> cartItems = cartService.getCartItems(currentUser);
        if (cartItems == null || cartItems.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Your cart is empty");
            return "redirect:/cart";
        }

        Order order = new Order();
        order.setUser(currentUser);
        order.setStatus("PLACED");
        order.setAddress(address);
        order.setPhone(phone);
        order.setDeliveryDistrict(district);
        order.setPaymentMethod(paymentMethod);

        List<OrderItem> orderItems = cartItems.stream().map(cart -> {
            OrderItem orderItem = new OrderItem();
            orderItem.setItem(cart.getItem());
            orderItem.setQuantity(cart.getQuantity());
            orderItem.setPrice(cart.getItem().getPrice());
            orderItem.setOrder(order);
            return orderItem;
        }).collect(Collectors.toList());

        order.setOrderItems(orderItems);

        BigDecimal subtotal = orderItems.stream()
                .map(oi -> oi.getPrice().multiply(BigDecimal.valueOf(oi.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal deliveryFee = deliveryFeeService.calculateForCart(cartItems, district);
        order.setSubtotalAmount(subtotal);
        order.setDeliveryFee(deliveryFee);
        order.setTotalAmount(subtotal.add(deliveryFee));

        Order savedOrder = orderService.placeOrder(order, currentUser);

        cartItems.forEach(cart -> cartService.removeFromCart(cart.getId()));

        redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order ID: " + savedOrder.getId());
        return "redirect:/orders";
    }

    @PostMapping("/orders/reorder/{id}")
    public String reorder(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to reorder");
            return "redirect:/login";
        }
        if (RoleUtils.isAdmin(currentUser)) {
            redirectAttributes.addFlashAttribute("error", "Admins cannot place customer orders");
            return "redirect:/admin/dashboard";
        }

        Order order = orderService.reorder(id, currentUser);
        if (order == null) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/orders";
        }

        redirectAttributes.addFlashAttribute("success", "Order placed successfully! Order ID: " + order.getId());
        return "redirect:/orders";
    }

    @PostMapping("/orders/cancel-request")
    public String requestCancel(@RequestParam Long id, RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to request cancellation");
            return "redirect:/login";
        }

        boolean ok = orderService.requestCancel(id, currentUser);
        if (!ok) {
            redirectAttributes.addFlashAttribute("error", "Unable to request cancellation for this order");
            return "redirect:/orders";
        }

        redirectAttributes.addFlashAttribute("success", "Cancellation request sent. Waiting for admin approval.");
        return "redirect:/orders";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String firstName,
                                @RequestParam String lastName,
                                @RequestParam(required = false) String phoneNumber,
                                @RequestParam(required = false) String gender,
                                @RequestParam(required = false) String street,
                                @RequestParam(required = false) String district,
                                RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        currentUser.setFirstName(firstName);
        currentUser.setLastName(lastName);
        currentUser.setPhoneNumber(phoneNumber);
        currentUser.setGender(gender);
        currentUser.setStreet(street);
        currentUser.setDistrict(district);

        try {
            userService.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/password")
    public String updatePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "New passwords do not match");
            return "redirect:/profile";
        }

        try {
            userService.updatePassword(currentUser.getId(), currentPassword, newPassword);
            redirectAttributes.addFlashAttribute("success", "Password updated successfully");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/profile";
    }

    @PostMapping("/profile/photo")
    public String updateProfilePhoto(@RequestParam("profileImage") MultipartFile profileImage,
                                     RedirectAttributes redirectAttributes) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("error", "Please login to update your profile photo");
            return "redirect:/login";
        }

        if (profileImage == null || profileImage.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Please choose an image to upload");
            return "redirect:/profile";
        }

        try {
            String imageUrl = storeProfileImage(profileImage);
            currentUser.setProfileImageUrl(imageUrl);
            userService.save(currentUser);
            redirectAttributes.addFlashAttribute("success", "Profile photo updated");
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
        }

        return "redirect:/profile";
    }

    private String storeProfileImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Files.createDirectories(PROFILE_UPLOAD_DIR);

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String safeExtension = StringUtils.hasText(extension) ? "." + extension.toLowerCase() : ".jpg";

        String fileName = "profile_" + UUID.randomUUID() + safeExtension;
        Path targetPath = PROFILE_UPLOAD_DIR.resolve(fileName).normalize();

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/profiles/" + fileName;
    }

    private BigDecimal cartSubtotal(List<Cart> cartItems) {
        if (cartItems == null) {
            return BigDecimal.ZERO;
        }
        return cartItems.stream()
                .filter(cart -> cart.getItem() != null && cart.getItem().getPrice() != null)
                .map(cart -> cart.getItem().getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
