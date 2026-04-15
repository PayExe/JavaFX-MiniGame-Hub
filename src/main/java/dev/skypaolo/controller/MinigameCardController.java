package dev.skypaolo.controller;

import dev.skypaolo.model.Minigame;
import java.io.IOException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MinigameCardController {

    @FXML
    private VBox root;

    @FXML
    private ImageView cardImage;

    @FXML
    private Label cardTitle;

    @FXML
    private Label cardDescription;

    @FXML
    private Button playButton;

    private Minigame minigame;

    public void setMinigame(Minigame minigame) {
        this.minigame = minigame;
        cardTitle.setText(minigame.getTitle());
        cardDescription.setText(minigame.getDescription());
        loadImage(minigame.getImagePath());
    }

    private void loadImage(String imagePath) {
        try {
            Image image = new Image(getClass().getResourceAsStream(imagePath));
            if (image.isError()) {
                image = new Image(
                    getClass().getResourceAsStream(
                        "/images/placeholders/default-placeholder.png"
                    )
                );
            }
            cardImage.setImage(image);
        } catch (Exception e) {
            System.err.println(
                "[Vault-Tec Warning] Could not load image: " + imagePath
            );
        }
    }

    @FXML
    public void handlePlay(ActionEvent event) {
        if (minigame == null) {
            System.err.println("[Vault-Tec Error] No minigame data available!");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/fxml/MinigameContainerView.fxml")
            );
            Parent containerView = loader.load();

            MinigameContainerController controller = loader.getController();
            controller.setMinigame(minigame);

            Stage currentStage = (Stage) (
                (Node) event.getSource()
            ).getScene().getWindow();
            double currentWidth = currentStage.getWidth();
            double currentHeight = currentStage.getHeight();

            Scene scene = new Scene(containerView);
            scene
                .getStylesheets()
                .add(
                    getClass()
                        .getResource("/css/vault-tec-theme.css")
                        .toExternalForm()
                );

            currentStage.setScene(scene);
            currentStage.setTitle(
                "Vault-Tec Mini Game Hub - " + minigame.getTitle()
            );
            currentStage.setWidth(currentWidth);
            currentStage.setHeight(currentHeight);
        } catch (IOException e) {
            System.err.println(
                "[Vault-Tec Critical] Failed to load minigame container: " +
                    e.getMessage()
            );
            e.printStackTrace();
        }
    }

    public VBox getRoot() {
        return root;
    }
}
