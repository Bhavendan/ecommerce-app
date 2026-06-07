package org.example.ecommerceapp.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController("/api/public/home")
public class HomeController {
    @GetMapping
    public String homePage(HttpServletRequest request){
        return "Welcome to Home Page..... "+request.getSession().getId();
    }
}
