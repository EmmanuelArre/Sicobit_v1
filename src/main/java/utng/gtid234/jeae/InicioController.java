package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.stage.Stage;
import javafx.scene.control.Button;
public class InicioController implements Initializable {
    @FXML
    private Button btnAlumno;

    @FXML
    private Button btnAdministrador;

    @FXML
    private Button btnCreditos;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // sin inicialización especial por ahora
    }

    @FXML
    private void irAlumno(ActionEvent event) {

        navegarA(event, "MenuAlumno.fxml", "SICOBIT - Alumno");
    }

    @FXML
    private void irAdministrador(ActionEvent event) {

        navegarA(event, "Login.fxml", "SICOBIT - Acceso Administrador");
    }

    @FXML
    private void irCreditos(ActionEvent event) {

        navegarA(event, "Creditos.fxml", "SICOBIT - Créditos");
    }

    private void navegarA(ActionEvent event, String fxml, String titulo) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource(fxml));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle(titulo);

        } catch (Exception e) {

            e.printStackTrace();

        }
    }
}
