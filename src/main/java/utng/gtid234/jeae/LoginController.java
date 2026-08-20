package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Administrador;

public class LoginController implements Initializable {
    @FXML
    private Button btnIngresar;

    @FXML
    private Button btnRegresar;


    @FXML
    private TextField txtUsuario;

    @FXML
    private PasswordField txtPassword;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // sin inicialización especial
    }

    @FXML
    private void ingresar(ActionEvent event) {

        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {

            mostrarAlerta("Error", "Ingresa usuario y contraseña.", AlertType.ERROR);
            return;

        }

        Administrador admin = new Administrador().validarLogin(usuario, password);

        if (admin == null) {

            mostrarAlerta("Acceso denegado", "Usuario o contraseña incorrectos.", AlertType.ERROR);
            return;

        }

        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource("Dashboard.fxml"));
            Parent root = loader.load();

            DashboardController controller = loader.getController();
            controller.setAdministrador(admin);

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("SICOBIT - Panel Administrador");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    @FXML
    private void regresar(ActionEvent event) {

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

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
