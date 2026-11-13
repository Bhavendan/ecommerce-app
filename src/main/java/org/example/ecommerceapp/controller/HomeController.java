package org.example.ecommerceapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/home")
public class HomeController {

    @GetMapping
    public String homePage() {
        return "Welcome to Home Page..... ";
    }
}