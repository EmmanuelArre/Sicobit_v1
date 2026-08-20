package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import utng.gtid234.jeae.modelo.Actividad;

public class ActividadesController implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbEstatus;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnActualizar;
    @FXML
    private Button btnCambiarEstatus;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Actividad> tablaActividades;
    @FXML
    private TableColumn<Actividad, Integer> colId;
    @FXML
    private TableColumn<Actividad, String> colNombre;
    @FXML
    private TableColumn<Actividad, String> colEstatus;

    private int idSeleccionada = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbEstatus.getItems().addAll("Activa", "Inactiva");
        cmbEstatus.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("idActividad"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));

        tablaActividades.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {

            if (seleccionada != null) {
                cargarEnFormulario(seleccionada);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Actividad> lista =
                FXCollections.observableArrayList(new Actividad().obtenerTodas());

        tablaActividades.setItems(lista);
    }

    private void cargarEnFormulario(Actividad a) {

        idSeleccionada = a.getIdActividad();
        txtNombre.setText(a.getNombre());
        cmbEstatus.setValue(a.getEstatus());

    }

    @FXML
    private void agregar() {

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre de la actividad es obligatorio.", AlertType.ERROR);
            return;
        }

        Actividad modelo = new Actividad();
        boolean ok = modelo.agregar(nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Actividad agregada correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar la actividad (verifica que el nombre no esté repetido).", AlertType.ERROR);
        }
    }

    @FXML
    private void actualizar() {

        if (idSeleccionada == -1) {
            mostrarAlerta("Error", "Selecciona una actividad de la tabla primero.", AlertType.ERROR);
            return;
        }

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre de la actividad es obligatorio.", AlertType.ERROR);
            return;
        }

        boolean ok = new Actividad().actualizar(idSeleccionada, nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Actividad actualizada correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la actividad (verifica que el nombre no esté repetido).", AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarEstatus() {

        if (idSeleccionada == -1) {
            mostrarAlerta("Error", "Selecciona una actividad de la tabla primero.", AlertType.ERROR);
            return;
        }

        String nuevoEstatus = cmbEstatus.getValue();

        boolean ok = new Actividad().cambiarEstatus(idSeleccionada, nuevoEstatus);

        if (ok) {
            mostrarAlerta("Éxito", "Estatus actualizado a: " + nuevoEstatus, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el estatus.", AlertType.ERROR);
        }
    }

    // Solo las actividades "Activas" aparecen en el combobox del formulario
    // de registro de alumnos, así que si una actividad ya está en uso no
    // conviene borrarla (se perdería el historial): se recomienda marcarla
    // como "Inactiva" en vez de eliminarla.
    @FXML
    private void eliminar() {

        if (idSeleccionada == -1) {
            mostrarAlerta("Error", "Selecciona una actividad de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar definitivamente la actividad \"" + txtNombre.getText() + "\"?");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (!respuesta.getText().equalsIgnoreCase("Aceptar")) {
                return;
            }

            Actividad modelo = new Actividad();
            boolean ok = modelo.eliminar(idSeleccionada);

            if (ok) {
                mostrarAlerta("Éxito", "La actividad fue eliminada.", AlertType.INFORMATION);
                limpiar();
                cargarTabla();
            } else {
                mostrarAlerta("Error",
                        "No se pudo eliminar (probablemente ya tiene registros asociados). "
                        + "Usa \"Cambiar estatus\" a Inactiva para dejar de mostrarla sin perder el historial.",
                        AlertType.ERROR);
            }
        });
    }

    @FXML
    private void limpiar() {

        idSeleccionada = -1;
        txtNombre.clear();
        cmbEstatus.getSelectionModel().selectFirst();
        tablaActividades.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
