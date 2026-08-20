package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Incidencia;

public class IncidenciasController implements Initializable {
    @FXML
    private Button btnResuelta;

    @FXML
    private Button btnReabrir;

    @FXML
    private Button btnLimpiar;


    @FXML
    private ComboBox<String> cmbFiltroEstado;

    @FXML
    private TableView<Incidencia> tablaIncidencias;
    @FXML
    private TableColumn<Incidencia, Integer> colId;
    @FXML
    private TableColumn<Incidencia, String> colMatricula;
    @FXML
    private TableColumn<Incidencia, String> colNombreAlumno;
    @FXML
    private TableColumn<Incidencia, String> colLaboratorio;
    @FXML
    private TableColumn<Incidencia, String> colTipo;
    @FXML
    private TableColumn<Incidencia, String> colDescripcion;
    @FXML
    private TableColumn<Incidencia, String> colFecha;
    @FXML
    private TableColumn<Incidencia, String> colHora;
    @FXML
    private TableColumn<Incidencia, String> colEstado;

    @FXML
    private TextArea txtObservaciones;

    private int idSeleccionada = -1;
    private ObservableList<Incidencia> listaCompleta;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbFiltroEstado.getItems().addAll("Todas", "Pendiente", "Resuelta");
        cmbFiltroEstado.getSelectionModel().selectFirst();
        cmbFiltroEstado.valueProperty().addListener((obs, anterior, nuevo) -> aplicarFiltro());

        colId.setCellValueFactory(new PropertyValueFactory<>("idIncidencia"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombreAlumno.setCellValueFactory(new PropertyValueFactory<>("nombreAlumno"));
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colDescripcion.setCellValueFactory(new PropertyValueFactory<>("descripcion"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHora.setCellValueFactory(new PropertyValueFactory<>("hora"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaIncidencias.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionada) -> {

            if (seleccionada != null) {
                idSeleccionada = seleccionada.getIdIncidencia();
                txtObservaciones.setText(
                        seleccionada.getObservacionesAdmin() != null ? seleccionada.getObservacionesAdmin() : ""
                );
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        listaCompleta = FXCollections.observableArrayList(new Incidencia().obtenerTodas());
        aplicarFiltro();
    }

    private void aplicarFiltro() {

        if (listaCompleta == null) {
            return;
        }

        String filtro = cmbFiltroEstado.getValue();

        if (filtro == null || filtro.equals("Todas")) {
            tablaIncidencias.setItems(listaCompleta);
            return;
        }

        ObservableList<Incidencia> filtrada = FXCollections.observableArrayList();

        for (Incidencia inc : listaCompleta) {
            if (inc.getEstado().equalsIgnoreCase(filtro)) {
                filtrada.add(inc);
            }
        }

        tablaIncidencias.setItems(filtrada);
    }

    @FXML
    private void marcarResuelta() {

        if (idSeleccionada == -1) {
            mostrarAlerta("Error", "Selecciona una incidencia de la tabla primero.", AlertType.ERROR);
            return;
        }

        boolean ok = new Incidencia().marcarResuelta(idSeleccionada, txtObservaciones.getText().trim());

        if (ok) {
            mostrarAlerta("Éxito", "Incidencia marcada como resuelta.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la incidencia.", AlertType.ERROR);
        }
    }

    @FXML
    private void reabrir() {

        if (idSeleccionada == -1) {
            mostrarAlerta("Error", "Selecciona una incidencia de la tabla primero.", AlertType.ERROR);
            return;
        }

        boolean ok = new Incidencia().reabrir(idSeleccionada);

        if (ok) {
            mostrarAlerta("Éxito", "Incidencia reabierta como Pendiente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo reabrir la incidencia.", AlertType.ERROR);
        }
    }

    @FXML
    private void limpiar() {

        idSeleccionada = -1;
        txtObservaciones.clear();
        tablaIncidencias.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
