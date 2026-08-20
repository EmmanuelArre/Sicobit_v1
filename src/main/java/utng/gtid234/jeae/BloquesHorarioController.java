package utng.gtid234.jeae;

import java.net.URL;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import utng.gtid234.jeae.modelo.BloqueHorario;

/**
 * Administra el catálogo de bloques de horario (bloque_horario), para que
 * el administrador pueda agregar un turno nuevo o corregir las horas de
 * uno existente sin tocar la base de datos directamente. Estos bloques
 * son los que se usan tanto en "Horarios regulares" como en el registro
 * de extraclase del alumno y en la vista de horario.
 */
public class BloquesHorarioController implements Initializable {

    @FXML
    private TextField txtHoraInicio;
    @FXML
    private TextField txtHoraFin;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnGuardarCambios;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;

    @FXML
    private TableView<BloqueHorario> tablaBloques;
    @FXML
    private TableColumn<BloqueHorario, Integer> colId;
    @FXML
    private TableColumn<BloqueHorario, String> colHoraInicio;
    @FXML
    private TableColumn<BloqueHorario, String> colHoraFin;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colId.setCellValueFactory(new PropertyValueFactory<>("idBloque"));
        colHoraInicio.setCellValueFactory(new PropertyValueFactory<>("horaInicio"));
        colHoraFin.setCellValueFactory(new PropertyValueFactory<>("horaFin"));

        tablaBloques.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<BloqueHorario> lista =
                FXCollections.observableArrayList(new BloqueHorario().obtenerTodos());

        tablaBloques.setItems(lista);
    }

    private void cargarEnFormulario(BloqueHorario b) {

        idSeleccionado = b.getIdBloque();
        txtHoraInicio.setText(b.getHoraInicio());
        txtHoraFin.setText(b.getHoraFin());

    }

    @FXML
    private void agregar() {

        LocalTime[] horas = validarHoras();

        if (horas == null) {
            return;
        }

        boolean ok = new BloqueHorario().agregar(horas[0].toString(), horas[1].toString());

        if (ok) {
            mostrarAlerta("Éxito", "Bloque de horario agregado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el bloque.", AlertType.ERROR);
        }
    }

    @FXML
    private void guardarCambios() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un bloque de la tabla primero.", AlertType.ERROR);
            return;
        }

        LocalTime[] horas = validarHoras();

        if (horas == null) {
            return;
        }

        boolean ok = new BloqueHorario().editar(idSeleccionado, horas[0].toString(), horas[1].toString());

        if (ok) {
            mostrarAlerta("Éxito", "Bloque actualizado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el bloque.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un bloque de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este bloque de horario? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new BloqueHorario().eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Bloque eliminado.", AlertType.INFORMATION);
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

    // Valida el formato HH:mm de ambos campos y que la hora de fin sea
    // posterior a la de inicio; regresa null si algo no es válido.
    private LocalTime[] validarHoras() {

        String textoInicio = txtHoraInicio.getText().trim();
        String textoFin = txtHoraFin.getText().trim();

        if (textoInicio.isEmpty() || textoFin.isEmpty()) {
            mostrarAlerta("Error", "Escribe la hora de inicio y la hora de fin (formato HH:mm).", AlertType.ERROR);
            return null;
        }

        try {

            LocalTime inicio = LocalTime.parse(textoInicio);
            LocalTime fin = LocalTime.parse(textoFin);

            if (!fin.isAfter(inicio)) {
                mostrarAlerta("Error", "La hora de fin debe ser posterior a la hora de inicio.", AlertType.ERROR);
                return null;
            }

            return new LocalTime[]{inicio, fin};

        } catch (Exception e) {
            mostrarAlerta("Error", "Formato de hora inválido. Usa HH:mm, por ejemplo 08:00.", AlertType.ERROR);
            return null;
        }
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        txtHoraInicio.clear();
        txtHoraFin.clear();
        tablaBloques.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
