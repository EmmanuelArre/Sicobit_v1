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
public class MenuAlumnoController implements Initializable {
    @FXML
    private Button btnRegistro;

    @FXML
    private Button btnIncidencia;

    @FXML
    private Button btnRegresar;


    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // sin inicialización especial
    }

    @FXML
    private void irRegistro(ActionEvent event) {
        navegarA(event, "formularioAlumno.fxml", "Registro de Laboratorios");
    }

    @FXML
    private void irIncidencia(ActionEvent event) {
        navegarA(event, "NuevaIncidencia.fxml", "SICOBIT - Registro de Incidencias");
    }

    @FXML
    private void regresar(ActionEvent event) {
        navegarA(event, "Inicio.fxml", "SICOBIT - Inicio");
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
