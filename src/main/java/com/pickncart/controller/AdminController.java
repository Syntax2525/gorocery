package com.pickncart.controller;

import com.pickncart.model.Category;
import com.pickncart.model.Item;
import com.pickncart.model.Order;
import com.pickncart.model.User;
import com.pickncart.repository.CartRepository;
import com.pickncart.repository.OrderItemRepository;
import com.pickncart.service.CategoryService;
import com.pickncart.service.ItemService;
import com.pickncart.service.OrderService;
import com.pickncart.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");
    private static final Path CATEGORY_UPLOAD_DIR = UPLOAD_DIR.resolve("categories");
    private static final long MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024;
    private static final int MAX_IMAGE_URL_LENGTH = 2048;
    private static final int MAX_IMAGE_DATA_URL_LENGTH = 5 * 1024 * 1024;

    private final CategoryService categoryService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final UserService userService;
    private final OrderItemRepository orderItemRepository;
    private final CartRepository cartRepository;

    public AdminController(CategoryService categoryService,
                           ItemService itemService,
                           OrderService orderService,
                           UserService userService,
                           OrderItemRepository orderItemRepository,
                           CartRepository cartRepository) {
        this.categoryService = categoryService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.userService = userService;
        this.orderItemRepository = orderItemRepository;
        this.cartRepository = cartRepository;
    }

    @GetMapping({"/dashboard", "/admin-dashboard"})
    public String adminDashboard(Model model) {
        List<User> users = userService.getAllUsers();
        List<Item> products = itemService.getAll();
        List<Order> orders = orderService.getAllOrders();

        BigDecimal deliveredRevenue = orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(nullToEmpty(order.getStatus())))
                .map(Order::getTotalAmount)
                .filter(amount -> amount != null)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("totalUsers", users.size());
        model.addAttribute("totalProducts", products.size());
        model.addAttribute("totalOrders", orders.size());
        model.addAttribute("deliveredRevenue", deliveredRevenue);
        model.addAttribute("users", users);
        model.addAttribute("orders", orders.stream().limit(10).collect(Collectors.toList()));
        return "admin-dashboard";
    }

    @GetMapping({"", "/"})
    public String adminRoot() {
        return "redirect:/admin/dashboard";
    }

    @GetMapping({"/roles", "/reports", "/settings", "/notifications", "/analytics"})
    public String adminSectionFallback(@RequestHeader(value = "Referer", required = false) String referer,
                                       RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute(
                "success",
                "That admin section is available through the dashboard, users, products, orders, and categories screens."
        );
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("newCategory", new Category());
        return "admin-categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String imageUrl,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);

        try {
            category.setImageUrl(resolveCategoryImageUrl(imageUrl, imageFile, null));
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categories";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category created successfully");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 @RequestParam(required = false) String imageUrl,
                                 @RequestParam(required = false) MultipartFile imageFile,
                                 RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryService.getById(id);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/categories";
        }

        Category category = categoryOpt.get();
        category.setName(name);
        category.setDescription(description);

        try {
            category.setImageUrl(resolveCategoryImageUrl(imageUrl, imageFile, category.getImageUrl()));
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/categories";
        }

        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            if (!itemService.getItemsByCategory(id).isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "Category cannot be deleted because it has products");
                return "redirect:/admin/categories";
            }
            categoryService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Category deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Category cannot be deleted because it is in use by products");
        }
        return "redirect:/admin/categories";
    }

    @GetMapping("/products")
    public String productsPage(Model model) {
        model.addAttribute("products", itemService.getAll());
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("newItem", new Item());
        return "admin-products";
    }

    @PostMapping("/products")
    public String createProduct(@RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) MultipartFile imageFile,
                                @RequestParam(required = false) MultipartFile cameraImage,
                                @RequestParam(defaultValue = "0") int stock,
                                @RequestParam Long categoryId,
                                RedirectAttributes redirectAttributes) {
        Category category = itemService.getCategoryById(categoryId);
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/products";
        }

        Item item = new Item();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setStock(stock);
        item.setCategory(category);

        try {
            String finalImageUrl = resolveImageUrl(imageUrl, imageFile, cameraImage, null);
            item.setImageUrl(finalImageUrl);
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/products";
        }

        itemService.save(item);
        redirectAttributes.addFlashAttribute("success", "Product created successfully");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/update")
    public String updateProduct(@PathVariable Long id,
                                @RequestParam String name,
                                @RequestParam(required = false) String description,
                                @RequestParam BigDecimal price,
                                @RequestParam(required = false) String imageUrl,
                                @RequestParam(required = false) MultipartFile imageFile,
                                @RequestParam(required = false) MultipartFile cameraImage,
                                @RequestParam(defaultValue = "0") int stock,
                                @RequestParam Long categoryId,
                                RedirectAttributes redirectAttributes) {
        Optional<Item> itemOpt = itemService.getById(id);
        if (itemOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Product not found");
            return "redirect:/admin/products";
        }

        Category category = itemService.getCategoryById(categoryId);
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/products";
        }

        Item item = itemOpt.get();
        item.setName(name);
        item.setDescription(description);
        item.setPrice(price);
        item.setStock(stock);
        item.setCategory(category);

        try {
            String finalImageUrl = resolveImageUrl(imageUrl, imageFile, cameraImage, item.getImageUrl());
            item.setImageUrl(finalImageUrl);
        } catch (IllegalArgumentException | IOException ex) {
            redirectAttributes.addFlashAttribute("error", ex.getMessage());
            return "redirect:/admin/products";
        }

        itemService.save(item);
        redirectAttributes.addFlashAttribute("success", "Product updated successfully");
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            boolean inOrders = orderItemRepository.existsByItemId(id);
            boolean inCarts = cartRepository.existsByItemId(id);
            if (inOrders || inCarts) {
                redirectAttributes.addFlashAttribute(
                        "error",
                        "Product cannot be deleted because it is referenced by existing orders or carts"
                );
                return "redirect:/admin/products";
            }
            itemService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Product deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "Product cannot be deleted right now");
        }
        return "redirect:/admin/products";
    }

    @GetMapping("/orders")
    public String ordersPage(Model model) {
        model.addAttribute("orders", orderService.getAllOrders());
        return "admin-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateOrderStatus(@PathVariable Long id,
                                    @RequestParam String status,
                                    RedirectAttributes redirectAttributes) {
        if (!isAllowedOrderStatus(status)) {
            redirectAttributes.addFlashAttribute("error", "Invalid order status");
            return "redirect:/admin/orders";
        }

        Optional<Order> orderOpt = orderService.getById(id);
        if (orderOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }

        Order order = orderOpt.get();
        order.setStatus(status);
        orderService.save(order);
        redirectAttributes.addFlashAttribute("success", "Order status updated");
        return "redirect:/admin/orders";
    }

    @PostMapping("/orders/{id}/cancel-approve")
    public String approveCancel(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        boolean ok = orderService.approveCancel(id);
        if (!ok) {
            redirectAttributes.addFlashAttribute("error", "Order not found");
            return "redirect:/admin/orders";
        }
        redirectAttributes.addFlashAttribute("success", "Order cancelled and removed");
        return "redirect:/admin/orders";
    }

    @GetMapping("/users")
    public String usersPage(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-users";
    }

    @PostMapping("/users/{id}/role")
    public String updateUserRole(@PathVariable Long id,
                                 @RequestParam String role,
                                 RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "User not found");
            return "redirect:/admin/users";
        }

        if (!"ADMIN".equals(role) && !"CUSTOMER".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "Invalid role");
            return "redirect:/admin/users";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentEmail = authentication.getName();
        User targetUser = userOpt.get();

        if (targetUser.getEmail() != null
                && targetUser.getEmail().equalsIgnoreCase(currentEmail)
                && "CUSTOMER".equals(role)) {
            redirectAttributes.addFlashAttribute("error", "You cannot remove your own admin role");
            return "redirect:/admin/users";
        }

        targetUser.setRole(role);
        userService.save(targetUser);
        redirectAttributes.addFlashAttribute("success", "User role updated");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Optional<User> userOpt = userService.getById(id);
            if (userOpt.isPresent()
                    && userOpt.get().getEmail() != null
                    && userOpt.get().getEmail().equalsIgnoreCase(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "You cannot delete your own admin account");
                return "redirect:/admin/users";
            }
            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("success", "User deleted successfully");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("error", "User cannot be deleted");
        }
        return "redirect:/admin/users";
    }

    private boolean isAllowedOrderStatus(String status) {
        return "PLACED".equals(status)
                || "SHIPPED".equals(status)
                || "DELIVERED".equals(status)
                || "CANCEL_REQUESTED".equals(status)
                || "CANCELLED".equals(status);
    }

    private String nullToEmpty(String value) {
        return value == null ? "" : value;
    }

    private String resolveImageUrl(String imageUrl,
                                   MultipartFile imageFile,
                                   MultipartFile cameraImage,
                                   String currentImageUrl) throws IOException {
        MultipartFile selectedFile = (imageFile != null && !imageFile.isEmpty()) ? imageFile : cameraImage;

        if (selectedFile != null && !selectedFile.isEmpty()) {
            return storeUploadedImage(selectedFile);
        }

        if (StringUtils.hasText(imageUrl)) {
            return normalizeImageUrl(imageUrl);
        }

        return currentImageUrl;
    }

    private String storeUploadedImage(MultipartFile file) throws IOException {
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Only image files are allowed");
        }

        Files.createDirectories(UPLOAD_DIR);

        String originalFilename = file.getOriginalFilename();
        String extension = StringUtils.getFilenameExtension(originalFilename);
        String safeExtension = StringUtils.hasText(extension) ? "." + extension.toLowerCase() : ".jpg";

        String fileName = UUID.randomUUID() + safeExtension;
        Path targetPath = UPLOAD_DIR.resolve(fileName).normalize();

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/" + fileName;
    }

    private String resolveCategoryImageUrl(String imageUrl,
                                           MultipartFile imageFile,
                                           String currentImageUrl) throws IOException {
        if (imageFile != null && !imageFile.isEmpty()) {
            return storeCategoryImage(imageFile);
        }

        if (StringUtils.hasText(imageUrl)) {
            return normalizeImageUrl(imageUrl);
        }

        return currentImageUrl;
    }

    private String normalizeImageUrl(String imageUrl) {
        String normalizedUrl = imageUrl.trim();
        if (normalizedUrl.startsWith("data:image/")) {
            validateImageDataUrl(normalizedUrl);
            return normalizedUrl;
        }
        if (normalizedUrl.length() > MAX_IMAGE_URL_LENGTH) {
            throw new IllegalArgumentException("Image URL must be 2048 characters or fewer");
        }
        return normalizedUrl;
    }

    private void validateImageDataUrl(String imageUrl) {
        boolean supportedImageData = imageUrl.startsWith("data:image/jpeg;base64,")
                || imageUrl.startsWith("data:image/png;base64,")
                || imageUrl.startsWith("data:image/webp;base64,");
        if (!supportedImageData) {
            throw new IllegalArgumentException("Image data URL must be JPG, PNG, or WEBP");
        }
        if (imageUrl.length() > MAX_IMAGE_DATA_URL_LENGTH) {
            throw new IllegalArgumentException("Image data URL must be 5 MB or smaller");
        }
    }

    private String storeCategoryImage(MultipartFile file) throws IOException {
        validateImageFile(file);
        Files.createDirectories(CATEGORY_UPLOAD_DIR);

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String safeExtension = "." + extension.toLowerCase();
        String fileName = "category_" + UUID.randomUUID() + safeExtension;
        Path targetPath = CATEGORY_UPLOAD_DIR.resolve(fileName).normalize();

        if (!targetPath.startsWith(CATEGORY_UPLOAD_DIR)) {
            throw new IllegalArgumentException("Invalid upload path");
        }

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
        return "/uploads/categories/" + fileName;
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Please select an image file");
        }

        if (file.getSize() > MAX_IMAGE_SIZE_BYTES) {
            throw new IllegalArgumentException("Category image must be 5 MB or smaller");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        if (!StringUtils.hasText(extension)) {
            throw new IllegalArgumentException("Image file must be JPG, JPEG, PNG, or WEBP");
        }

        String normalizedExtension = extension.toLowerCase();
        boolean supportedExtension = normalizedExtension.equals("jpg")
                || normalizedExtension.equals("jpeg")
                || normalizedExtension.equals("png")
                || normalizedExtension.equals("webp");
        if (!supportedExtension) {
            throw new IllegalArgumentException("Supported category image formats are JPG, JPEG, PNG, and WEBP");
        }

        String contentType = file.getContentType();
        boolean supportedContentType = contentType != null && (
                contentType.equals("image/jpeg")
                        || contentType.equals("image/png")
                        || contentType.equals("image/webp")
        );
        if (!supportedContentType) {
            throw new IllegalArgumentException("Uploaded file is not a supported image type");
        }
    }
}
