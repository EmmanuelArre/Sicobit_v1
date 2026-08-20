package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import utng.gtid234.jeae.modelo.EquipoComputo;
import utng.gtid234.jeae.modelo.Laboratorio;

public class LaboratoriosController implements Initializable {
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnEditar;
    @FXML
    private Button btnEliminar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnCambiarEstado;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtCapacidad;
    @FXML
    private TextField txtUbicacion;
    @FXML
    private ComboBox<String> cmbEstado;

    @FXML
    private TableView<Laboratorio> tablaLaboratorios;
    @FXML
    private TableColumn<Laboratorio, Integer> colId;
    @FXML
    private TableColumn<Laboratorio, String> colNombre;
    @FXML
    private TableColumn<Laboratorio, Integer> colCapacidad;
    @FXML
    private TableColumn<Laboratorio, String> colUbicacion;
    @FXML
    private TableColumn<Laboratorio, String> colEstado;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbEstado.getItems().addAll("Disponible", "Ocupado", "Fuera de servicio");
        cmbEstado.getSelectionModel().selectFirst();

        colId.setCellValueFactory(new PropertyValueFactory<>("idLaboratorio"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colCapacidad.setCellValueFactory(new PropertyValueFactory<>("capacidad"));
        colUbicacion.setCellValueFactory(new PropertyValueFactory<>("ubicacion"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        tablaLaboratorios.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarEnFormulario(seleccionado);
            }

        });

        cargarTabla();
    }

    private void cargarTabla() {

        ObservableList<Laboratorio> lista =
                FXCollections.observableArrayList(new Laboratorio().obtenerTodosDetalle());

        tablaLaboratorios.setItems(lista);
    }

    private void cargarEnFormulario(Laboratorio lab) {

        idSeleccionado = lab.getIdLaboratorio();
        txtNombre.setText(lab.getNombre());
        txtCapacidad.setText(String.valueOf(lab.getCapacidad()));
        txtUbicacion.setText(lab.getUbicacion());
        cmbEstado.setValue(lab.getEstado());

    }

    @FXML
    private void agregar() {

        Laboratorio lab = leerFormulario();

        if (lab == null) {
            return;
        }

        boolean ok = new Laboratorio().agregar(lab);

        if (ok) {

            // El laboratorio se creó: ahora se generan sus PC-01..PC-N
            // automáticamente según la capacidad capturada
            int idNuevo = new Laboratorio().obtenerIdLaboratorio(lab.getNombre());

            String mensajeEquipos = "";

            if (idNuevo > 0) {
                EquipoComputo.ResultadoSincronizacion sync =
                        new EquipoComputo().sincronizarConCapacidad(idNuevo, lab.getCapacidad());

                if (sync.agregados > 0) {
                    mensajeEquipos = "\n\nSe generaron " + sync.agregados + " equipos de cómputo (PC-01 a PC-"
                            + String.format("%02d", lab.getCapacidad()) + ").";
                }
            }

            mostrarAlerta("Éxito", "Laboratorio agregado correctamente." + mensajeEquipos, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el laboratorio.", AlertType.ERROR);
        }
    }

    @FXML
    private void editar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un laboratorio de la tabla primero.", AlertType.ERROR);
            return;
        }

        Laboratorio lab = leerFormulario();

        if (lab == null) {
            return;
        }

        lab.setIdLaboratorio(idSeleccionado);

        boolean ok = new Laboratorio().editar(lab);

        if (ok) {

            // Sincroniza los equipos de cómputo con la nueva capacidad:
            // agrega PC-XX si subió, o intenta quitar las últimas si bajó
            EquipoComputo.ResultadoSincronizacion sync =
                    new EquipoComputo().sincronizarConCapacidad(idSeleccionado, lab.getCapacidad());

            StringBuilder mensajeEquipos = new StringBuilder();

            if (sync.agregados > 0) {
                mensajeEquipos.append("\n\nSe agregaron ").append(sync.agregados).append(" equipos nuevos.");
            }
            if (sync.eliminados > 0) {
                mensajeEquipos.append("\n\nSe quitaron ").append(sync.eliminados).append(" equipos.");
            }
            if (sync.noSePudieronEliminar > 0) {
                mensajeEquipos.append("\n\nAviso: ").append(sync.noSePudieronEliminar)
                        .append(" equipo(s) no se pudieron quitar porque ya tienen historial (registros, préstamos o incidencias). ")
                        .append("La capacidad quedó guardada, pero esos equipos siguen existiendo.");
            }

            mostrarAlerta("Éxito", "Laboratorio actualizado." + mensajeEquipos, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el laboratorio.", AlertType.ERROR);
        }
    }

    @FXML
    private void cambiarEstado() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un laboratorio de la tabla primero.", AlertType.ERROR);
            return;
        }

        String nuevoEstado = cmbEstado.getValue();

        boolean ok = new Laboratorio().cambiarEstado(idSeleccionado, nuevoEstado);

        if (ok) {
            mostrarAlerta("Éxito", "Estado actualizado a: " + nuevoEstado, AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo cambiar el estado.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un laboratorio de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este laboratorio? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new Laboratorio().eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Laboratorio eliminado.", AlertType.INFORMATION);
                    limpiar();
                    cargarTabla();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar (puede tener registros asociados).", AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        txtNombre.clear();
        txtCapacidad.clear();
        txtUbicacion.clear();
        cmbEstado.getSelectionModel().selectFirst();
        tablaLaboratorios.getSelectionModel().clearSelection();

    }

    private Laboratorio leerFormulario() {

        if (txtNombre.getText().trim().isEmpty() || txtCapacidad.getText().trim().isEmpty()) {

            mostrarAlerta("Error", "Nombre y capacidad son obligatorios.", AlertType.ERROR);
            return null;

        }

        int capacidad;

        try {

            capacidad = Integer.parseInt(txtCapacidad.getText().trim());

        } catch (NumberFormatException e) {

            mostrarAlerta("Error", "La capacidad debe ser un número.", AlertType.ERROR);
            return null;

        }

        Laboratorio lab = new Laboratorio();
        lab.setNombre(txtNombre.getText().trim());
        lab.setCapacidad(capacidad);
        lab.setUbicacion(txtUbicacion.getText().trim());
        lab.setEstado(cmbEstado.getValue());

        return lab;
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
