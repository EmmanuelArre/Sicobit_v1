package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.stage.Stage;

public class CreditosController implements Initializable {

    @FXML
    private Button btnVolver;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // sin inicialización especial por ahora
    }

    @FXML
    private void volver(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource("Inicio.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("SICOBIT - Inicio");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
