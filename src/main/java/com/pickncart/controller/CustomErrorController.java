package com.pickncart.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    private static final Logger log = LoggerFactory.getLogger(CustomErrorController.class);

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);
        Object path = request.getAttribute(RequestDispatcher.ERROR_REQUEST_URI);

        Integer statusCode = null;
        String errorMessage = "An unexpected error occurred";

        if (status != null) {
            statusCode = Integer.valueOf(status.toString());
        }

        if (message != null && !message.toString().isEmpty()) {
            errorMessage = message.toString();
        }

        model.addAttribute("statusCode", statusCode != null ? statusCode : "Unknown");
        model.addAttribute("errorMessage", errorMessage);
        model.addAttribute("path", path != null ? path : request.getRequestURI());

        if (exception instanceof Throwable throwable) {
            log.error("Request failed with status {} on {}", statusCode, path, throwable);
        } else if (statusCode != null && statusCode >= HttpStatus.INTERNAL_SERVER_ERROR.value()) {
            log.error("Request failed with status {} on {}: {}", statusCode, path, errorMessage);
        } else {
            log.warn("Request ended with status {} on {}: {}", statusCode, path, errorMessage);
        }

        if (statusCode != null) {
            return switch (statusCode) {
                case 400 -> "error/400";
                case 403 -> "error/403";
                case 404 -> "error/404";
                case 500 -> "error/500";
                default -> "error/generic";
            };
        }

        return "error/generic";
    }
}
