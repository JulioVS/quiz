package com.app.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.ui.Model;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.app.quiz.model.User;
import com.app.quiz.model.Question;
import com.app.quiz.service.QuestionsService;
import com.app.quiz.service.QuizUserDetailsService;

@Controller
public class QuizController {

    private final QuestionsService questionsService;
    private final QuizUserDetailsService userDetailsService;
    private final AuthenticationManager authenticationManager;

    public QuizController(QuestionsService questionsService, QuizUserDetailsService userDetailsService,
            AuthenticationManager authenticationManager) {
        this.questionsService = questionsService;
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

    @GetMapping("/add-quiz")
    public String addQuiz(Model model) {
        model.addAttribute("quiz", new Question()); // Add a new Quiz object to the model
        return "add-quiz"; // Return the add-quiz.html template
    }

    @PostMapping("/add-quiz")
    public String saveQuiz(@ModelAttribute Question quiz, Model model) {
        // Logic to save the quiz to the database (e.g., quizService.save(quiz))
        // Save the quiz using the appropriate service
        boolean success = questionsService.addQuiz(quiz);

        if (!success) {
            model.addAttribute("error", "An error occurred while saving the quiz.");
            return "add-quiz"; // Return to the add-quiz page if there was an error
        }

        return "redirect:/home"; // Redirect to the home page after saving the quiz
    }

    @GetMapping("/edit-quiz/{id}")
    public String editQuiz(@PathVariable int id, Model model) {
        Question quiz = questionsService.getQuizById(id);
        if (quiz == null) {
            return "redirect:/home"; // Redirect to home if the quiz is not found
        }
        model.addAttribute("quiz", quiz);
        return "edit-quiz"; // Return the edit-quiz.html template
    }

    @PutMapping("/edit-quiz")
    public String updateQuiz(@ModelAttribute Question quiz, Model model) {
        // Logic to update the quiz in the database (e.g., quizService.update(quiz))
        boolean success = questionsService.editQuiz(quiz);
        if (!success) {
            model.addAttribute("error", "An error occurred while updating the quiz.");
            return "edit-quiz"; // Return to the edit-quiz page if there was an error
        }
        return "redirect:/home"; // Redirect to the home page after updating the quiz
    }

    @DeleteMapping("/delete-quiz/{id}")
    public String deleteQuiz(@PathVariable int id, Model model) {
        // Logic to delete the quiz from the database (e.g., quizService.delete(id))
        boolean success = questionsService.deleteQuiz(id);
        if (!success) {
            model.addAttribute("error", "An error occurred while deleting the quiz.");
            return "redirect:/home?error"; // Redirect to home with an error message if there was an error
        }
        return "redirect:/home?success"; // Redirect to home with a success message after deleting the quiz
    }

}
