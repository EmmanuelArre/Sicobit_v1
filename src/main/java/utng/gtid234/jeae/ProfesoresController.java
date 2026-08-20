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

import utng.gtid234.jeae.modelo.Profesor;

public class ProfesoresController implements Initializable {

    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<String> cmbEstatus;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnCambiarEstatus;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Profesor> tablaProfesores;
    @FXML
    private TableColumn<Profesor, Integer> colId;
    @FXML
    private TableColumn<Profesor, String> colNombre;
    @FXML
    private TableColumn<Profesor, String> colEstatus;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbEstatus.getItems().addAll("Activo", "Inactivo");
        cmbEstatus.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("idProfesor"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));

        tablaProfesores.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Profesor> lista =
                FXCollections.observableArrayList(new Profesor().obtenerTodos());

        tablaProfesores.setItems(lista);
    }

    private void cargarEnFormulario(Profesor p) {

        idSeleccionado = p.getIdProfesor();
        txtNombre.setText(p.getNombre());
        cmbEstatus.setValue(p.getEstatus());

    }

    @FXML
    private void agregar() {

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre del profesor es obligatorio.", AlertType.ERROR);
            return;
        }

        Profesor modelo = new Profesor();
        boolean ok = modelo.agregar(nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Profesor agregado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el profesor.", AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarEstatus() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un profesor de la tabla primero.", AlertType.ERROR);
            return;
        }

        String nuevoEstatus = cmbEstatus.getValue();

        boolean ok = new Profesor().cambiarEstatus(idSeleccionado, nuevoEstatus);

        if (ok) {
            mostrarAlerta("Éxito", "Estatus actualizado a: " + nuevoEstatus, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el estatus.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un profesor de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText(
                "¿Eliminar este profesor? También se eliminarán los horarios regulares "
                        + "en los que esté asignado. Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                Profesor modelo = new Profesor();
                boolean ok = modelo.eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Profesor eliminado.", AlertType.INFORMATION);
                    limpiar();
                    cargarTabla();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el profesor. "
                            + (modelo.getUltimoError() != null ? modelo.getUltimoError() : ""),
                            AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        txtNombre.clear();
        cmbEstatus.getSelectionModel().selectFirst();
        tablaProfesores.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
