package dev.skypaolo.service;

import dev.skypaolo.model.Question; // le modèle Question
import java.util.ArrayList; // liste modifiable
import java.util.Collections; // outils pour mélanger ou rendre non modifiable une liste.
import java.util.List; // type d'une liste

// Ce service permet de créer des questions afin de pouvoir
//  les charger via le contrôleur depuis la base de données.
public class QuestionService {

    // Liste des questions :
    private final List<Question> questions = new ArrayList<>();

    // le "final" permet de dire que la référence de la liste ne change pas même si contenu change.

    // C'est le constructeur qui load les questions automatiquement, et aussi pour tester sans base données.
    public QuestionService() {
        loadDefaultQuestions();
    }

    // Le loader des questions de base (hard codé pour le moment).
    // le formatage est corrigé par zed automatiquement.
    public void loadDefaultQuestions() {
        questions.add(new Question(1, "Le ciel est bleu.", true, "nature"));
        questions.add(
            new Question(
                2,
                "Paris est la capitale de l'Italie.",
                false,
                "géographie"
            )
        );
        questions.add(
            new Question(3, "Un chat est un mammifère.", true, "animaux")
        );
        questions.add(new Question(4, "2 + 2 = 5.", false, "mathématiques"));
        questions.add(
            new Question(
                5,
                "La Terre tourne autour du Soleil.",
                true,
                "astronomie"
            )
        );
        questions.add(new Question(6, "Le feu est froid.", false, "physique"));
        questions.add(
            new Question(
                7,
                "Java est un langage de programmation.",
                true,
                "informatique"
            )
        );
        questions.add(
            new Question(
                8,
                "Un éléphant peut voler naturellement.",
                false,
                "animaux"
            )
        );
        questions.add(
            new Question(
                9,
                "L'eau bout à 100°C au niveau de la mer.",
                true,
                "science"
            )
        );
        questions.add(
            new Question(10, "Le Japon est en Europe.", false, "géographie")
        );
    }

    // Ca retourne les questions.
    public List<Question> getAllQuestions() {
        return Collections.unmodifiableList(questions); // Empêche de modifier les questions depuis l'exterieur.
    }

    // Ca copie la liste, mélange la copie, renvoie la copie mélangée, permet d'avoir un ordre aléatoire à chaque partie.
    public List<Question> getShuffledQuestions() {
        List<Question> shuffled = new ArrayList<>(questions);
        Collections.shuffle(shuffled);
        return shuffled;
    }

    // Ajoute une nouvelle question à la liste. Utile pour que le joueur ajoute lui même des questions dans le formulaire.
    public void addQuestion(Question question) {
        questions.add(question);
    }

    // Renvoie le total de questions, utile pour le score.
    public int getQuestionCount() {
        return questions.size();
    }
}
