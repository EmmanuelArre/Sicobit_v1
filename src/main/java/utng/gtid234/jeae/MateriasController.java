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

import utng.gtid234.jeae.modelo.Materia;

public class MateriasController implements Initializable {

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
    private TableView<Materia> tablaMaterias;
    @FXML
    private TableColumn<Materia, Integer> colId;
    @FXML
    private TableColumn<Materia, String> colNombre;
    @FXML
    private TableColumn<Materia, String> colEstatus;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbEstatus.getItems().addAll("Activa", "Inactiva");
        cmbEstatus.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("idMateria"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colEstatus.setCellValueFactory(new PropertyValueFactory<>("estatus"));

        tablaMaterias.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Materia> lista =
                FXCollections.observableArrayList(new Materia().obtenerTodas());

        tablaMaterias.setItems(lista);
    }

    private void cargarEnFormulario(Materia m) {

        idSeleccionado = m.getIdMateria();
        txtNombre.setText(m.getNombre());
        cmbEstatus.setValue(m.getEstatus());

    }

    @FXML
    private void agregar() {

        String nombre = txtNombre.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "El nombre de la materia es obligatorio.", AlertType.ERROR);
            return;
        }

        Materia modelo = new Materia();
        boolean ok = modelo.agregar(nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Materia agregada correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar la materia.", AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarEstatus() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona una materia de la tabla primero.", AlertType.ERROR);
            return;
        }

        String nuevoEstatus = cmbEstatus.getValue();

        boolean ok = new Materia().cambiarEstatus(idSeleccionado, nuevoEstatus);

        if (ok) {
            mostrarAlerta("Éxito", "Estatus actualizado a: " + nuevoEstatus, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el estatus.", AlertType.ERROR);
        }
    }

    // Solo las materias "Activas" aparecen en los combobox del resto del
    // sistema, así que si una materia ya está en uso no conviene borrarla
    // (se perdería la referencia): se recomienda marcarla como "Inactiva"
    // en vez de eliminarla.
    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona una materia de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar definitivamente la materia \"" + txtNombre.getText() + "\"?");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (!respuesta.getText().equalsIgnoreCase("Aceptar")) {
                return;
            }

            Materia modelo = new Materia();
            boolean ok = modelo.eliminar(idSeleccionado);

            if (ok) {
                mostrarAlerta("Éxito", "La materia fue eliminada.", AlertType.INFORMATION);
                limpiar();
                cargarTabla();
            } else {
                mostrarAlerta("Error",
                        "No se pudo eliminar (probablemente ya está en uso). "
                        + "Usa \"Cambiar estatus\" a Inactiva para dejar de mostrarla sin perder la información asociada.",
                        AlertType.ERROR);
            }
        });
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        txtNombre.clear();
        cmbEstatus.getSelectionModel().selectFirst();
        tablaMaterias.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
