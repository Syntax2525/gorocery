package com.pickncart.controller;

import com.pickncart.model.Category;
import com.pickncart.model.Item;
import com.pickncart.model.Order;
import com.pickncart.model.User;
import com.pickncart.service.CategoryService;
import com.pickncart.service.ItemService;
import com.pickncart.service.OrderService;
import com.pickncart.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.ui.Model;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;
import java.util.UUID;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private static final Path UPLOAD_DIR = Paths.get("uploads");

    private final CategoryService categoryService;
    private final ItemService itemService;
    private final OrderService orderService;
    private final UserService userService;

    public AdminController(CategoryService categoryService,
                           ItemService itemService,
                           OrderService orderService,
                           UserService userService) {
        this.categoryService = categoryService;
        this.itemService = itemService;
        this.orderService = orderService;
        this.userService = userService;
    }

    @GetMapping("/categories")
    public String categoriesPage(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "admin-categories";
    }

    @PostMapping("/categories")
    public String createCategory(@RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        Category category = new Category();
        category.setName(name);
        category.setDescription(description);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category created successfully");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/update")
    public String updateCategory(@PathVariable Long id,
                                 @RequestParam String name,
                                 @RequestParam(required = false) String description,
                                 RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryService.getById(id);
        if (categoryOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "Category not found");
            return "redirect:/admin/categories";
        }

        Category category = categoryOpt.get();
        category.setName(name);
        category.setDescription(description);
        categoryService.save(category);
        redirectAttributes.addFlashAttribute("success", "Category updated successfully");
        return "redirect:/admin/categories";
    }

    @PostMapping("/categories/{id}/delete")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
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

    private String resolveImageUrl(String imageUrl,
                                   MultipartFile imageFile,
                                   MultipartFile cameraImage,
                                   String currentImageUrl) throws IOException {
        MultipartFile selectedFile = (imageFile != null && !imageFile.isEmpty()) ? imageFile : cameraImage;

        if (selectedFile != null && !selectedFile.isEmpty()) {
            return storeUploadedImage(selectedFile);
        }

        if (StringUtils.hasText(imageUrl)) {
            return imageUrl.trim();
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
}
