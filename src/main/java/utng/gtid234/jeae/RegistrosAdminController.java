package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

import utng.gtid234.jeae.modelo.Cuatrimestre;
import utng.gtid234.jeae.modelo.Registro;
import javafx.scene.control.Button;
/**
 * Vista de solo lectura para el administrador: muestra la tabla de
 * registros de extraclase, filtrable por cuatrimestre (el admin puede
 * ver cualquier cuatrimestre registrado, a diferencia del alumno que
 * solo ve el actual).
 */
public class RegistrosAdminController implements Initializable {
    @FXML
    private Button btnRefrescar;


    @FXML
    private ComboBox<Cuatrimestre> cmbCuatrimestre;

    @FXML
    private TableView<Registro> tablaRegistros;
    @FXML
    private TableColumn<Registro, Integer> colId;
    @FXML
    private TableColumn<Registro, String> colMatricula;
    @FXML
    private TableColumn<Registro, String> colNombre;
    @FXML
    private TableColumn<Registro, String> colGrupo;
    @FXML
    private TableColumn<Registro, String> colLaboratorio;
    @FXML
    private TableColumn<Registro, String> colActividad;
    @FXML
    private TableColumn<Registro, String> colFecha;
    @FXML
    private TableColumn<Registro, String> colDia;
    @FXML
    private TableColumn<Registro, String> colHoraEntrada;
    @FXML
    private TableColumn<Registro, String> colHoraSalida;
    @FXML
    private TableColumn<Registro, String> colTipo;
    @FXML
    private TableColumn<Registro, String> colEstado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colId.setCellValueFactory(new PropertyValueFactory<>("idRegistro"));
        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colDia.setCellValueFactory(new PropertyValueFactory<>("dia"));
        colHoraEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        colHoraSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        colTipo.setCellValueFactory(new PropertyValueFactory<>("tipo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        ObservableList<Cuatrimestre> cuatrimestres =
                FXCollections.observableArrayList(new Cuatrimestre().obtenerTodos());
        cmbCuatrimestre.setItems(cuatrimestres);

        for (Cuatrimestre c : cuatrimestres) {
            if (c.isActivo()) {
                cmbCuatrimestre.setValue(c);
                break;
            }
        }
        if (cmbCuatrimestre.getValue() == null && !cuatrimestres.isEmpty()) {
            cmbCuatrimestre.getSelectionModel().selectFirst();
        }

        cmbCuatrimestre.valueProperty().addListener((obs, anterior, nuevo) -> cargarTabla());

        cargarTabla();
    }

    private void cargarTabla() {

        if (cmbCuatrimestre.getValue() == null) {
            tablaRegistros.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<Registro> lista =
                new Registro().obtenerRegistros(cmbCuatrimestre.getValue().getIdCuatrimestre());

        tablaRegistros.setItems(lista);
    }

    @FXML
    private void refrescar() {
        cargarTabla();
    }
}
