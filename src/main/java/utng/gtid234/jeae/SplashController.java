package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import javafx.util.Duration;

public class SplashController implements Initializable {
    @FXML
    private Label lblInstitucion;


    @FXML
    private ProgressBar barCarga;

    private Stage stage;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, e -> barCarga.setProgress(0)),
                new KeyFrame(Duration.seconds(1.6), e -> barCarga.setProgress(1))
        );

        timeline.setOnFinished(e -> irAInicio());
        timeline.play();
    }

    private void irAInicio() {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("Inicio.fxml"));

            Stage s = obtenerStage();

            s.getScene().setRoot(root);
            s.setTitle("SICOBIT - Inicio");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private Stage obtenerStage() {

        if (stage == null) {
            stage = (Stage) barCarga.getScene().getWindow();
        }

        return stage;
    }
}
