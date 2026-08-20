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
import javafx.util.StringConverter;

import utng.gtid234.jeae.modelo.BloqueHorario;
import utng.gtid234.jeae.modelo.Cuatrimestre;
import utng.gtid234.jeae.modelo.HorarioClase;
import utng.gtid234.jeae.modelo.Laboratorio;
import utng.gtid234.jeae.modelo.Materia;
import utng.gtid234.jeae.modelo.Profesor;

public class HorarioClaseController implements Initializable {

    @FXML
    private ComboBox<Cuatrimestre> cmbCuatrimestre;
    @FXML
    private ComboBox<Laboratorio> cmbLaboratorio;
    @FXML
    private ComboBox<String> cmbDia;
    @FXML
    private ComboBox<BloqueHorario> cmbBloque;
    @FXML
    private ComboBox<Materia> cmbMateria;
    @FXML
    private ComboBox<Profesor> cmbProfesor;

    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<HorarioClase> tablaHorario;
    @FXML
    private TableColumn<HorarioClase, Integer> colId;
    @FXML
    private TableColumn<HorarioClase, String> colDia;
    @FXML
    private TableColumn<HorarioClase, String> colBloque;
    @FXML
    private TableColumn<HorarioClase, String> colLaboratorio;
    @FXML
    private TableColumn<HorarioClase, String> colMateria;
    @FXML
    private TableColumn<HorarioClase, String> colProfesor;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        // Cuatrimestres (el admin puede ver/elegir cualquiera)
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

        // Laboratorios
        ObservableList<Laboratorio> laboratorios =
                FXCollections.observableArrayList(new Laboratorio().obtenerListaCompleta());
        cmbLaboratorio.setItems(laboratorios);
        cmbLaboratorio.setConverter(new StringConverter<Laboratorio>() {
            @Override
            public String toString(Laboratorio lab) {
                return lab == null ? "" : lab.getNombre();
            }

            @Override
            public Laboratorio fromString(String s) {
                return null;
            }
        });

        // Días
        cmbDia.getItems().addAll("Lunes", "Martes", "Miércoles", "Jueves", "Viernes", "Sábado");

        // Bloques
        ObservableList<BloqueHorario> bloques =
                FXCollections.observableArrayList(new BloqueHorario().obtenerTodos());
        cmbBloque.setItems(bloques);

        // Materias y profesores (combobox en vez de texto libre)
        cmbMateria.setItems(FXCollections.observableArrayList(new Materia().obtenerActivas()));
        cmbProfesor.setItems(FXCollections.observableArrayList(new Profesor().obtenerActivos()));

        // Tabla
        colId.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("idHorario"));
        colDia.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("dia"));
        colBloque.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("bloqueTexto"));
        colLaboratorio.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("laboratorio"));
        colMateria.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("materia"));
        colProfesor.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("profesor"));

        tablaHorario.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        if (cmbCuatrimestre.getValue() == null) {
            tablaHorario.setItems(FXCollections.observableArrayList());
            return;
        }

        ObservableList<HorarioClase> lista = FXCollections.observableArrayList(
                new HorarioClase().obtenerPorCuatrimestre(cmbCuatrimestre.getValue().getIdCuatrimestre()));

        tablaHorario.setItems(lista);
    }

    private void cargarEnFormulario(HorarioClase h) {

        idSeleccionado = h.getIdHorario();

        for (Cuatrimestre c : cmbCuatrimestre.getItems()) {
            if (c.getIdCuatrimestre() == h.getIdCuatrimestre()) {
                cmbCuatrimestre.setValue(c);
                break;
            }
        }

        for (Laboratorio lab : cmbLaboratorio.getItems()) {
            if (lab.getIdLaboratorio() == h.getIdLaboratorio()) {
                cmbLaboratorio.setValue(lab);
                break;
            }
        }

        cmbDia.setValue(h.getDia());

        for (BloqueHorario b : cmbBloque.getItems()) {
            if (b.getIdBloque() == h.getIdBloque()) {
                cmbBloque.setValue(b);
                break;
            }
        }

        for (Materia m : cmbMateria.getItems()) {
            if (m.getIdMateria() == h.getIdMateria()) {
                cmbMateria.setValue(m);
                break;
            }
        }

        for (Profesor p : cmbProfesor.getItems()) {
            if (p.getIdProfesor() == h.getIdProfesor()) {
                cmbProfesor.setValue(p);
                break;
            }
        }

    }

    @FXML
    private void agregar() {

        HorarioClase h = leerFormulario();

        if (h == null) {
            return;
        }

        // Verificación explícita para mostrar el motivo del choque
        String ocupado = new HorarioClase().verificarOcupado(
                h.getIdLaboratorio(), h.getDia(), h.getIdBloque(), h.getIdCuatrimestre(), -1);

        if (ocupado != null) {
            mostrarAlerta("Horario ocupado", ocupado, AlertType.WARNING);
            return;
        }

        boolean ok = new HorarioClase().agregar(h);

        if (ok) {
            mostrarAlerta("Éxito", "Clase agregada al horario regular.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar la clase.", AlertType.ERROR);
        }
    }

    @FXML
    private void editar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona una clase de la tabla primero.", AlertType.ERROR);
            return;
        }

        HorarioClase h = leerFormulario();

        if (h == null) {
            return;
        }

        h.setIdHorario(idSeleccionado);

        String ocupado = new HorarioClase().verificarOcupado(
                h.getIdLaboratorio(), h.getDia(), h.getIdBloque(), h.getIdCuatrimestre(), idSeleccionado);

        if (ocupado != null) {
            mostrarAlerta("Horario ocupado", ocupado, AlertType.WARNING);
            return;
        }

        boolean ok = new HorarioClase().editar(h);

        if (ok) {
            mostrarAlerta("Éxito", "Clase actualizada.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar la clase.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona una clase de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar esta clase del horario? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new HorarioClase().eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Clase eliminada.", AlertType.INFORMATION);
                    limpiar();
                    cargarTabla();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar la clase.", AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        cmbLaboratorio.getSelectionModel().clearSelection();
        cmbDia.getSelectionModel().clearSelection();
        cmbBloque.getSelectionModel().clearSelection();
        cmbMateria.getSelectionModel().clearSelection();
        cmbProfesor.getSelectionModel().clearSelection();
        tablaHorario.getSelectionModel().clearSelection();

    }

    private HorarioClase leerFormulario() {

        if (cmbCuatrimestre.getValue() == null || cmbLaboratorio.getValue() == null || cmbDia.getValue() == null
                || cmbBloque.getValue() == null || cmbMateria.getValue() == null || cmbProfesor.getValue() == null) {

            mostrarAlerta("Error", "Cuatrimestre, laboratorio, día, bloque, materia y profesor son obligatorios.", AlertType.ERROR);
            return null;

        }

        HorarioClase h = new HorarioClase();
        h.setIdCuatrimestre(cmbCuatrimestre.getValue().getIdCuatrimestre());
        h.setIdLaboratorio(cmbLaboratorio.getValue().getIdLaboratorio());
        h.setDia(cmbDia.getValue());
        h.setIdBloque(cmbBloque.getValue().getIdBloque());
        h.setIdMateria(cmbMateria.getValue().getIdMateria());
        h.setIdProfesor(cmbProfesor.getValue().getIdProfesor());

        return h;
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
