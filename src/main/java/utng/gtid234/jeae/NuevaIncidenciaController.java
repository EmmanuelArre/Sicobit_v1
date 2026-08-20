package utng.gtid234.jeae;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Alumno;
import utng.gtid234.jeae.modelo.EquipoComputo;
import utng.gtid234.jeae.modelo.Incidencia;
import utng.gtid234.jeae.modelo.Laboratorio;

public class NuevaIncidenciaController implements Initializable {
    @FXML
    private Button btnGuardar;

    @FXML
    private Button btnLimpiar;

    @FXML
    private Button btnCancelar;


    @FXML
    private TextField txtMatricula;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbLaboratorio;
    @FXML
    private ComboBox<EquipoComputo> cmbEquipo;
    @FXML
    private ComboBox<String> cmbTipo;
    @FXML
    private TextArea txtDescripcion;

    private List<Laboratorio> laboratorios;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbTipo.getItems().addAll(
                "Equipo dañado",
                "Software",
                "Internet",
                "Mobiliario",
                "Electricidad",
                "Limpieza",
                "Otro"
        );

        laboratorios = new Laboratorio().obtenerListaCompleta();

        for (Laboratorio l : laboratorios) {
            cmbLaboratorio.getItems().add(l.getNombre());
        }

        // Al elegir el laboratorio se cargan sus equipos (por si la
        // incidencia es de un equipo de cómputo en particular)
        cmbLaboratorio.valueProperty().addListener((obs, anterior, nuevo) -> cargarEquiposDelLaboratorio());

        // Al escribir la matrícula se busca el alumno y se muestra su nombre
        txtMatricula.textProperty().addListener((obs, anterior, nuevo) -> {

            if (nuevo.length() >= 10) {

                Alumno alumno = new Alumno().buscarAlumno(nuevo);

                if (alumno != null) {
                    txtNombre.setText(alumno.getNombre());
                } else {
                    txtNombre.clear();
                }

            } else {
                txtNombre.clear();
            }

        });

        txtMatricula.textProperty().addListener((obs, viejo, nuevo) -> {
            if (!nuevo.matches("\\d{0,20}")) {
                txtMatricula.setText(viejo);
            }
        });
    }

    // Carga los equipos del laboratorio elegido para poder ligar la
    // incidencia a un equipo de cómputo específico (opcional)
    private void cargarEquiposDelLaboratorio() {

        cmbEquipo.getItems().clear();
        cmbEquipo.getSelectionModel().clearSelection();

        String nombreLab = cmbLaboratorio.getValue();

        if (nombreLab == null) {
            return;
        }

        int idLaboratorio = laboratorios.stream()
                .filter(l -> l.getNombre().equals(nombreLab))
                .map(Laboratorio::getIdLaboratorio)
                .findFirst()
                .orElse(-1);

        if (idLaboratorio == -1) {
            return;
        }

        cmbEquipo.getItems().setAll(new EquipoComputo().obtenerPorLaboratorio(idLaboratorio));
    }

    @FXML
    private void guardar() {

        if (txtMatricula.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()
                || cmbLaboratorio.getValue() == null
                || cmbTipo.getValue() == null
                || txtDescripcion.getText().trim().isEmpty()) {

            mostrarAlerta("Error", "Completa matrícula, laboratorio, tipo y descripción.", AlertType.ERROR);
            return;

        }

        Incidencia inc = new Incidencia();
        inc.setMatricula(txtMatricula.getText().trim());
        inc.setLaboratorio(cmbLaboratorio.getValue());
        inc.setIdEquipo(cmbEquipo.getValue() != null ? cmbEquipo.getValue().getIdEquipo() : null);
        inc.setTipo(cmbTipo.getValue());
        inc.setDescripcion(txtDescripcion.getText().trim());
        // La fecha y hora las registra el propio sistema al guardar (CURDATE/CURTIME)

        boolean guardado = inc.insertarIncidencia(inc);

        if (guardado) {

            mostrarAlerta("Éxito", "Incidencia registrada correctamente.", AlertType.INFORMATION);
            limpiar();

        } else {

            mostrarAlerta("Error", "No se pudo registrar la incidencia.", AlertType.ERROR);

        }
    }

    @FXML
    private void limpiar() {

        txtMatricula.clear();
        txtNombre.clear();
        cmbLaboratorio.getSelectionModel().clearSelection();
        cmbEquipo.getItems().clear();
        cmbEquipo.getSelectionModel().clearSelection();
        cmbTipo.getSelectionModel().clearSelection();
        txtDescripcion.clear();
        txtMatricula.requestFocus();

    }

    @FXML
    private void cancelar(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource("MenuAlumno.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("SICOBIT - Alumno");

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
