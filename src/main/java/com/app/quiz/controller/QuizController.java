package com.app.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.app.quiz.model.User;

@Controller
public class QuizController {

    @GetMapping("/login")
    public String login() {
        return "login";     // Return the login.html template
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User()); // Add a new User object to the model
        return "register";  // Return the register.html template
    }

}
