package com.app.quiz.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;

import java.util.ArrayList;
import java.util.Map;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.app.quiz.model.User;
import com.app.quiz.model.Question;
import com.app.quiz.service.QuestionsService;
import com.app.quiz.service.QuizUserDetailsService;

import jakarta.validation.Valid;

@Controller
public class QuizController {

    private final QuizUserDetailsService userDetailsService;
    private final QuestionsService questionsService;
    private final AuthenticationManager authenticationManager;

    public QuizController(QuizUserDetailsService userDetailsService, QuestionsService questionsService,
            AuthenticationManager authenticationManager) {
        this.userDetailsService = userDetailsService;
        this.questionsService = questionsService;
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

        // Get the list of quizzes and add it to the model
        ArrayList<Question> quizzes = questionsService.getAllQuizzes();
        model.addAttribute("quizzes", quizzes); // Add the list of quizzes to the model

        // Redirect to the appropriate page based on the role
        if ("ROLE_ADMIN".equals(role)) {
            return "quiz-list"; // Return the quiz-list.html template for admin users

        } else {
            return "quiz"; // Return the quiz.html template for regular users
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
    public String registerUser(@Valid @ModelAttribute User user, BindingResult bindingResult, Model model) {

        // Check for validation errors
        if (bindingResult.hasErrors()) {
			return "register"; // Return to the form with error messages
        }

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
    public String saveQuiz(@Valid @ModelAttribute Question quiz, BindingResult bindingResult, Model model, Authentication authentication) {

        // Check for validation errors
        // if (bindingResult.hasErrors()) {
		// 	return "add-quiz"; // Return to the form with error messages
        // }

        // Get the auhtenticated user's role
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("ROLE_USER"); // Default to ROLE_USER if no role is found

        if (!"ROLE_ADMIN".equals(role)) {
            model.addAttribute("error", "You do not have permission to add a quiz.");
            return "redirect:/add-quiz?error"; // Redirect to home with an error message if the user is not an admin
        }

        // Logic to save the quiz to the database (e.g., quizService.save(quiz))
        quiz.setId(questionsService.getNextId());

        // Save the quiz using the appropriate service
        boolean success = questionsService.addQuiz(quiz);

        if (!success) {
            model.addAttribute("error", "An error occurred while saving the quiz.");
            return "redirect:/add-quiz?error"; // Return to the add-quiz page if there was an error
        }

        model.addAttribute("success", "Quiz added successfully!"); // Add a success message to the model
        return "redirect:/home?success"; // Redirect to the home page after saving the quiz
    }

    @GetMapping("/edit-quiz/{id}")
    public String editQuiz(@PathVariable int id, Model model) {

        // Find the quiz by ID and add it to the model
        Question quiz = questionsService.getQuizById(id);

        if (quiz == null) {
            model.addAttribute("error", "Quiz not found.");
            return "redirect:/home?error"; // Redirect to home if the quiz is not found
        }

        model.addAttribute("quiz", quiz);
        return "edit-quiz"; // Return the edit-quiz.html template

    }

    @PutMapping("/edit-quiz")
    public String updateQuiz(@ModelAttribute Question quiz, Model model, Authentication authentication) {

        // Get the authenticated user's role
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("ROLE_USER"); // Default to ROLE_USER if no role is found

        if (!"ROLE_ADMIN".equals(role)) {
            model.addAttribute("error", "You do not have permission to edit a quiz.");
            return "redirect:/home?error"; // Redirect to home with an error message if the user is not an admin
        }

        // Logic to update the quiz in the database (e.g., quizService.update(quiz))
        boolean success = questionsService.editQuiz(quiz);

        if (!success) {
            model.addAttribute("error", "An error occurred while updating the quiz.");
            return "redirect:/home?error"; // Redirect to home with an error message if there was an error
        }

        return "redirect:/home?success"; // Redirect to the home page after updating the quiz

    }

    @DeleteMapping("/delete-quiz/{id}")
    public String deleteQuiz(@PathVariable int id, Model model, Authentication authentication) {

        // Get the authenticated user's role
        String role = authentication.getAuthorities().stream()
                .map(auth -> auth.getAuthority())
                .findFirst()
                .orElse("ROLE_USER"); // Default to ROLE_USER if no role is found

        if (!"ROLE_ADMIN".equals(role)) {
            model.addAttribute("error", "You do not have permission to delete a quiz.");
            return "redirect:/home?error"; // Redirect to home with an error message if the user is not an admin
        }

        // Logic to delete the quiz from the database (e.g., quizService.delete(id))
        boolean success = questionsService.deleteQuiz(id);

        if (!success) {
            model.addAttribute("error", "An error occurred while deleting the quiz.");
            return "redirect:/home?error"; // Redirect to home with an error message if there was an error
        }

        return "redirect:/home?success"; // Redirect to home with a success message after deleting the quiz

    }

    @PostMapping("/submit-quiz")
    public String submitQuiz(@RequestParam Map<String, String> allParams, Model model) {

        int correctAnswers = 0;

        ArrayList<String> userAnswers = new ArrayList<>();
        ArrayList<Question> quizzes = questionsService.getAllQuizzes();

        // Iterate through the quizzes and compare answers
        for (int i = 0; i < quizzes.size(); i++) {

            String userAnswer = allParams.get("answer" + i); // Get the answer for question i
            userAnswers.add(userAnswer); // Store user's answer

            if (quizzes.get(i).getCorrectAnswer().equals(userAnswer)) {
                correctAnswers++;
            }

        }

        // Add data to the model
        model.addAttribute("quizzes", quizzes);
        model.addAttribute("userAnswers", userAnswers);
        model.addAttribute("correctAnswers", correctAnswers);
        model.addAttribute("totalQuestions", quizzes.size());

        // Return the result template
        return "result";

    }

}
