package utng.gtid234.jeae;

import java.net.URL;
import java.time.Year;
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

import utng.gtid234.jeae.modelo.Cuatrimestre;


public class CuatrimestresController implements Initializable {

    @FXML
    private ComboBox<String> cbPeriodo;
    @FXML
    private TextField txtAnio;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnGuardarCambios;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnMarcarActivo;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<Cuatrimestre> tablaCuatrimestres;
    @FXML
    private TableColumn<Cuatrimestre, Integer> colId;
    @FXML
    private TableColumn<Cuatrimestre, String> colPeriodo;
    @FXML
    private TableColumn<Cuatrimestre, Integer> colAnio;
    @FXML
    private TableColumn<Cuatrimestre, String> colActivo;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cbPeriodo.setItems(FXCollections.observableArrayList(Cuatrimestre.PERIODOS));

        colId.setCellValueFactory(new PropertyValueFactory<>("idCuatrimestre"));
        colPeriodo.setCellValueFactory(new PropertyValueFactory<>("periodo"));
        colAnio.setCellValueFactory(new PropertyValueFactory<>("anio"));
        colActivo.setCellValueFactory(dato ->
                new javafx.beans.property.SimpleStringProperty(dato.getValue().isActivo() ? "Sí" : "No"));

        tablaCuatrimestres.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Cuatrimestre> lista =
                FXCollections.observableArrayList(new Cuatrimestre().obtenerTodos());

        tablaCuatrimestres.setItems(lista);
    }

    private void cargarEnFormulario(Cuatrimestre c) {

        idSeleccionado = c.getIdCuatrimestre();
        cbPeriodo.setValue(c.getPeriodo());
        txtAnio.setText(String.valueOf(c.getAnio()));

    }

    @FXML
    private void agregar() {

        Object[] datos = validarDatos();

        if (datos == null) {
            return;
        }

        boolean ok = new Cuatrimestre().agregar((String) datos[0], (int) datos[1]);

        if (ok) {
            mostrarAlerta("Éxito", "Cuatrimestre agregado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el cuatrimestre (puede que ya exista uno igual).", AlertType.ERROR);
        }
    }

    @FXML
    private void guardarCambios() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un cuatrimestre de la tabla primero.", AlertType.ERROR);
            return;
        }

        Object[] datos = validarDatos();

        if (datos == null) {
            return;
        }

        boolean ok = new Cuatrimestre().editar(idSeleccionado, (String) datos[0], (int) datos[1]);

        if (ok) {
            mostrarAlerta("Éxito", "Cuatrimestre actualizado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el cuatrimestre.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un cuatrimestre de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este cuatrimestre? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new Cuatrimestre().eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Cuatrimestre eliminado.", AlertType.INFORMATION);
                    limpiar();
                    cargarTabla();
                } else {
                    mostrarAlerta("Error",
                            "No se pudo eliminar (puede que ya tenga horarios o registros asociados).",
                            AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void marcarActivo() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un cuatrimestre de la tabla primero.", AlertType.ERROR);
            return;
        }

        boolean ok = new Cuatrimestre().marcarActivo(idSeleccionado);

        if (ok) {
            mostrarAlerta("Éxito", "Ese cuatrimestre ahora es el activo (el que ven los alumnos).", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo marcar como activo.", AlertType.ERROR);
        }
    }

    // Valida que haya un periodo elegido y un año numérico razonable;
    // regresa {periodo, anio} o null si algo no es válido.
    private Object[] validarDatos() {

        String periodo = cbPeriodo.getValue();
        String textoAnio = txtAnio.getText().trim();

        if (periodo == null || periodo.isEmpty()) {
            mostrarAlerta("Error", "Selecciona el periodo (Enero-Abril, Mayo-Agosto o Septiembre-Diciembre).", AlertType.ERROR);
            return null;
        }

        if (textoAnio.isEmpty()) {
            mostrarAlerta("Error", "Escribe el año, por ejemplo " + Year.now().getValue() + ".", AlertType.ERROR);
            return null;
        }

        try {

            int anio = Integer.parseInt(textoAnio);

            if (anio < 2000 || anio > 2100) {
                mostrarAlerta("Error", "Escribe un año válido (entre 2000 y 2100).", AlertType.ERROR);
                return null;
            }

            return new Object[]{periodo, anio};

        } catch (NumberFormatException e) {
            mostrarAlerta("Error", "El año debe ser un número, por ejemplo " + Year.now().getValue() + ".", AlertType.ERROR);
            return null;
        }
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        cbPeriodo.setValue(null);
        txtAnio.clear();
        tablaCuatrimestres.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
