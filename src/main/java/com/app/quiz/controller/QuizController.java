package com.app.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.app.quiz.model.User;
import com.app.quiz.service.QuizUserDetailsService;

@Controller
public class QuizController {

    private final QuizUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public QuizController(QuizUserDetailsService userDetailsService, AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.authenticationManager = authenticationManager;
    }

    @GetMapping("/home")
    public String homepage(Model model) {

        // Get the authenticated user's details
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        // Check if the user is authenticated
        if (authentication != null && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal())) {

            String username = authentication.getName(); // Get the username of the authenticated user
            model.addAttribute("username", username); // Add the username to the model

        } else {
            // Redirect to the login page if the user is not authenticated
            return "redirect:/login";
        }

        // Get the user's role and add it to the model
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("ROLE_USER"); // Default to ROLE_USER if no role is found

        model.addAttribute("role", role); // Add the user's role to the model

        // Redirect to the appropriate page based on the role
        if ("ROLE_ADMIN".equals(role)) {
            return "admin-home"; // Return the admin-home.html template for admin users
        } else {
            return "user-home"; // Return the user-home.html template for regular users
        }

    }

    @GetMapping("/login")
    public String login() {
        return "login"; // Return the login.html template
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User()); // Add a new User object to the model
        return "register"; // Return the register.html template
    }

    @PostMapping("/register")
    public String registerUser(@ModelAttribute User user, Model model) {

        // Logic to save the user to the database (e.g., userService.save(user))
        // Save the user using the QuizUserDetailsService

        try {

            userDetailsService.registerUser(
                    user.getUsername(),
                    user.getPassword(),
                    user.getEmail(),
                    user.getRole());

        } catch (Exception e) {
            model.addAttribute("error", "An error occurred during registration: " + e.getMessage());
            return "redirect:/register?error";
        }

        // Authenticate the user programmatically
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(user.getUsername(),
                        user.getPassword()));

        // Set the authentication in the SecurityContext
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Redirect to the /login endpoint
        return "redirect:/login?success";

    }

}
