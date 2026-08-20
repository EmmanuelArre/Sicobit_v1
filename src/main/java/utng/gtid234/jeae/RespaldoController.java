package utng.gtid234.jeae;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import utng.gtid234.jeae.conexiones.Conexion;
import utng.gtid234.jeae.modelo.Respaldo;

public class RespaldoController implements Initializable {
    @FXML
    private Button btnCarpeta;

    @FXML
    private Button btnGenerar;

    @FXML
    private Button btnImportar;


    @FXML
    private TextField txtCarpeta;
    @FXML
    private Label lblEstado;

    @FXML
    private TableView<Respaldo> tablaRespaldos;
    @FXML
    private TableColumn<Respaldo, Integer> colId;
    @FXML
    private TableColumn<Respaldo, String> colArchivo;
    @FXML
    private TableColumn<Respaldo, String> colFecha;
    @FXML
    private TableColumn<Respaldo, String> colHora;
    @FXML
    private TableColumn<Respaldo, String> colTamano;
    @FXML
    private TableColumn<Respaldo, String> colEstado;
    @FXML
    private TableColumn<Respaldo, String> colTipo;

    // Mismos datos de conexión que utng.gtid234.jeae.conexiones.Conexion,
    // tomados de ahí en vez de repetirlos fijos, para que respaldo/
    // restauración usen siempre el mismo host/usuario/contraseña/base de
    // datos que el resto de la app (incluyendo lo configurado por las
    // variables de entorno DB_URL, DB_USER y DB_PASS en cada equipo).
    private static final String HOST = Conexion.getHost();
    private static final String USUARIO = Conexion.getUsuario();
    private static final String PASSWORD = Conexion.getPassword();
    private static final String BASE_DATOS = Conexion.getBaseDatos();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        txtCarpeta.setText(System.getProperty("user.home") + File.separator + "respaldos_sicobit");

        colId.setCellValueFactory(new PropertyValueFactory<>("idRespaldo"));
        colArchivo.setCellValueFactory(new PropertyValueFactory<>("nombreArchivo"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colTamano.setCellValueFactory(new PropertyValueFactory<>("tamanoLegible"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Respaldo> lista = FXCollections.observableArrayList(new Respaldo().obtenerHistorial());
        tablaRespaldos.setItems(lista);
    }

    @FXML
    private void elegirCarpeta() {

        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Selecciona carpeta de destino para los respaldos");

        Stage stage = (Stage) txtCarpeta.getScene().getWindow();
        File carpeta = chooser.showDialog(stage);

        if (carpeta != null) {
            txtCarpeta.setText(carpeta.getAbsolutePath());
        }
    }

    @FXML
    private void generarRespaldo() {

        String carpeta = txtCarpeta.getText().trim();

        if (carpeta.isEmpty()) {
            mostrarAlerta("Error", "Selecciona una carpeta de destino.", AlertType.ERROR);
            return;
        }

        lblEstado.setText("Generando respaldo, espera un momento...");
        lblEstado.setStyle("-fx-text-fill:#F39C12;");

        Respaldo resultado = new Respaldo().generar(carpeta, HOST, USUARIO, PASSWORD, BASE_DATOS);

        if ("Exitoso".equals(resultado.getEstado())) {

            lblEstado.setText("Respaldo generado: " + resultado.getNombreArchivo() + " (" + resultado.getTamanoLegible() + ")");
            lblEstado.setStyle("-fx-text-fill:#2EAA4A;");
            mostrarAlerta("Éxito", "Respaldo generado correctamente en:\n" + resultado.getRutaCompleta(), AlertType.INFORMATION);

        } else {

            lblEstado.setText("No se pudo generar el respaldo.");
            lblEstado.setStyle("-fx-text-fill:#E74C3C;");
            mostrarAlerta("Error",
                    "No se pudo generar el respaldo. Verifica que 'mysqldump' esté instalado y "
                    + "disponible en el PATH del sistema."
                    + (resultado.getDetalleError() != null ? "\n\nDetalle:\n" + resultado.getDetalleError() : ""),
                    AlertType.ERROR);

        }

        cargarTabla();
    }

    @FXML
    private void importarRespaldo() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona el archivo .sql a importar");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Archivos SQL", "*.sql"));

        Stage stage = (Stage) txtCarpeta.getScene().getWindow();
        File archivo = chooser.showOpenDialog(stage);

        if (archivo == null) {
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar importación");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "Vas a importar \"" + archivo.getName() + "\". Esto puede sobrescribir información "
                + "actual de la base de datos. ¿Deseas continuar?");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (!respuesta.getText().equalsIgnoreCase("Aceptar")) {
                return;
            }

            lblEstado.setText("Importando respaldo, espera un momento...");
            lblEstado.setStyle("-fx-text-fill:#F39C12;");

            Respaldo resultado = new Respaldo().importar(archivo, HOST, USUARIO, PASSWORD, BASE_DATOS);

            if ("Exitoso".equals(resultado.getEstado())) {

                lblEstado.setText("Respaldo importado correctamente: " + resultado.getNombreArchivo());
                lblEstado.setStyle("-fx-text-fill:#2EAA4A;");
                mostrarAlerta("Éxito", "El respaldo se importó correctamente. Es posible que debas reiniciar sesión.", AlertType.INFORMATION);

            } else {

                lblEstado.setText("No se pudo importar el respaldo.");
                lblEstado.setStyle("-fx-text-fill:#E74C3C;");
                mostrarAlerta("Error",
                        "No se pudo importar el respaldo. Verifica que 'mysql' esté instalado y "
                        + "disponible en el PATH del sistema, y que el archivo sea un .sql válido."
                        + (resultado.getDetalleError() != null ? "\n\nDetalle:\n" + resultado.getDetalleError() : ""),
                        AlertType.ERROR);

            }

            cargarTabla();
        });
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
