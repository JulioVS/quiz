package com.app.quiz.service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import com.app.quiz.model.Question;

@Service
public class QuestionsService {

    private final Map<Integer, Question> questions = new HashMap<>();
    private int nextId = 0; // To keep track of the next available ID for new quizzes

    public QuestionsService() {
        loadQuizzes(); // Load initial quizzes when the service is instantiated
    }

    public int getNextId() {
        return nextId++; // Return the next available ID and increment the counter
    }

    public Question getQuizById(int id) {
        return questions.get(id); // Retrieve a quiz by its ID
    }

    public ArrayList<Question> getAllQuizzes() {
        return new ArrayList<>(questions.values()); // Retrieve all quizzes
    }

    public ArrayList<Question> loadQuizzes() {

        // Logic to load quizzes from a data source (e.g., database, file, etc.)
        Question q1 = new Question(getNextId(), "What is the capital of France?", new ArrayList<String>() {
            {
                add("Berlin");
                add("Madrid");
                add("Paris");
                add("Rome");
            }
        }, "Paris");
        questions.put(q1.getId(), q1);

        Question q2 = new Question(getNextId(), "What is the largest planet in our solar system?",
                new ArrayList<String>() {
                    {
                        add("Earth");
                        add("Jupiter");
                        add("Mars");
                        add("Saturn");
                    }
                }, "Jupiter");
        questions.put(q2.getId(), q2);

        Question q3 = new Question(getNextId(), "What is the chemical symbol for water?", new ArrayList<String>() {
            {
                add("H2O");
                add("O2");
                add("CO2");
                add("NaCl");
            }
        }, "H2O");
        questions.put(q3.getId(), q3);

        return new ArrayList<>(questions.values());

    }

    public boolean addQuiz(Question question) {

        // Logic to add a new quiz to the data source
        // For demonstration, we will just return true to indicate success
        if (questions.containsKey(question.getId())) {
            return false; // Quiz with the same ID already exists
        }

        questions.put(question.getId(), question);
        return true;

    }

    public boolean editQuiz(Question updatedQuestion) {

        // Logic to update an existing quiz in the data source
        // For demonstration, we will just return true to indicate success
        if (!questions.containsKey(updatedQuestion.getId())) {
            return false; // Quiz with the given ID does not exist
        }

        questions.put(updatedQuestion.getId(), updatedQuestion);
        return true;

    }

    public boolean deleteQuiz(int id) {

        // Logic to delete a quiz from the data source
        // For demonstration, we will just return true to indicate success
        if (!questions.containsKey(id)) {
            return false; // Quiz with the given ID does not exist
        }

        questions.remove(id);
        return true;

    }

}
