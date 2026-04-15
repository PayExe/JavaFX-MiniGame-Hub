package dev.skypaolo.service;

import dev.skypaolo.database.DatabaseManager;
import dev.skypaolo.model.Question;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class QuestionService {

    private final DatabaseManager databaseManager;
    private final List<Question> questions = new ArrayList<>();
    private final Random random = new Random();

    public QuestionService() {
        this.databaseManager = DatabaseManager.getInstance();
        initializeQuestions();
    }

    private void initializeQuestions() {
        loadQuestionsFromDatabase();

        if (questions.isEmpty()) {
            insertDefaultQuestionsIfNeeded();
            loadQuestionsFromDatabase();
        }
    }

    private void loadQuestionsFromDatabase() {
        questions.clear();

        String sql =
            "SELECT id, text, answer, category FROM questions ORDER BY id";

        try (
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(sql);
            ResultSet resultSet = statement.executeQuery()
        ) {
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String text = resultSet.getString("text");
                boolean answer = resultSet.getInt("answer") == 1;
                String category = resultSet.getString("category");

                questions.add(new Question(id, text, answer, category));
            }
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS ERROR] Failed to load questions from database!"
            );
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void insertDefaultQuestionsIfNeeded() {
        List<Question> defaultQuestions = getDefaultQuestions();

        String sql =
            "INSERT INTO questions (text, answer, category) VALUES (?, ?, ?)";

        try (Connection connection = databaseManager.getConnection()) {
            connection.setAutoCommit(false);

            try (
                PreparedStatement statement = connection.prepareStatement(sql)
            ) {
                for (Question question : defaultQuestions) {
                    statement.setString(1, question.getText());
                    statement.setInt(2, question.isAnswer() ? 1 : 0);
                    statement.setString(3, question.getCategory());
                    statement.addBatch();
                }

                statement.executeBatch();
                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS ERROR] Failed to insert default questions!"
            );
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
        }
    }

    private List<Question> getDefaultQuestions() {
        List<Question> defaultQuestions = new ArrayList<>();

        defaultQuestions.add(
            new Question(0, "Le ciel est bleu.", true, "nature")
        );
        defaultQuestions.add(
            new Question(
                0,
                "Paris est la capitale de l'Italie.",
                false,
                "géographie"
            )
        );
        defaultQuestions.add(
            new Question(0, "Un chat est un mammifère.", true, "animaux")
        );
        defaultQuestions.add(
            new Question(0, "2 + 2 = 5.", false, "mathématiques")
        );
        defaultQuestions.add(
            new Question(
                0,
                "La Terre tourne autour du Soleil.",
                true,
                "astronomie"
            )
        );
        defaultQuestions.add(
            new Question(0, "Le feu est froid.", false, "physique")
        );
        defaultQuestions.add(
            new Question(
                0,
                "Java est un langage de programmation.",
                true,
                "informatique"
            )
        );
        defaultQuestions.add(
            new Question(
                0,
                "Un éléphant peut voler naturellement.",
                false,
                "animaux"
            )
        );
        defaultQuestions.add(
            new Question(
                0,
                "L'eau bout à 100°C au niveau de la mer.",
                true,
                "science"
            )
        );
        defaultQuestions.add(
            new Question(0, "Le Japon est en Europe.", false, "géographie")
        );

        return defaultQuestions;
    }

    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(questions);
    }

    public List<Question> getShuffledQuestions() {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled, random);
        return shuffled;
    }

    public void addQuestion(Question question) {
        String sql =
            "INSERT INTO questions (text, answer, category) VALUES (?, ?, ?)";

        try (
            Connection connection = databaseManager.getConnection();
            PreparedStatement statement = connection.prepareStatement(
                sql,
                Statement.RETURN_GENERATED_KEYS
            )
        ) {
            statement.setString(1, question.getText());
            statement.setInt(2, question.isAnswer() ? 1 : 0);
            statement.setString(3, question.getCategory());
            statement.executeUpdate();

            try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    int generatedId = generatedKeys.getInt(1);
                    questions.add(
                        new Question(
                            generatedId,
                            question.getText(),
                            question.isAnswer(),
                            question.getCategory()
                        )
                    );
                } else {
                    questions.add(question);
                }
            }
        } catch (SQLException e) {
            System.err.println(
                "[VT-OS ERROR] Failed to save question to database!"
            );
            System.err.println("[VT-OS ERROR] " + e.getMessage());
            e.printStackTrace();
            questions.add(question);
        }
    }

    public int getQuestionCount() {
        return questions.size();
    }

    public void refresh() {
        loadQuestionsFromDatabase();
    }
}
