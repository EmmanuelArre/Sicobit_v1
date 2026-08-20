package utng.gtid234.jeae;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import utng.gtid234.jeae.modelo.Actividad;
import utng.gtid234.jeae.modelo.Alumno;
import utng.gtid234.jeae.modelo.BloqueHorario;
import utng.gtid234.jeae.modelo.Cuatrimestre;
import utng.gtid234.jeae.modelo.EquipoComputo;
import utng.gtid234.jeae.modelo.Registro;
import utng.gtid234.jeae.modelo.RegistroHorario;

public class FormRegAlumController implements Initializable {

    @FXML
    private ComboBox<BloqueHorario> cmbHorario;

    @FXML
    private Label lblDia;

    @FXML
    private Button btnHorarios;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnRegresar;
    @FXML
    private Button btnRegistrar;
    @FXML
    private TextField txtNombre;
    @FXML
    private TextField txtMatricula;
    @FXML
    private TextField txtGrupo;
    @FXML
    private ComboBox<String> cmbLab;

    @FXML
    private ComboBox<Actividad> cmbActividad;
    @FXML
    private ComboBox<EquipoComputo> cmbEquipo;
    @FXML
    private Label lblDisponibilidad;

    @FXML
    private TableView<Registro> tblRegistros;

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
    private TableColumn<Registro, ?> colFecha;
    @FXML
    private TableColumn<Registro, ?> colHoraEntrada;
    @FXML
    private TableColumn<Registro, ?> colHoraSalida;
    @FXML
    private TableColumn<Registro, String> colEstado;
    @FXML
    private Button btnCancelarRegistro;

    private int idCuatrimestreActivo = -1;

    private String diaFijo;

  
    private String diaDeHoy() {

        return switch (LocalDate.now().getDayOfWeek()) {
            case MONDAY -> "Lunes";
            case TUESDAY -> "Martes";
            case WEDNESDAY -> "Miércoles";
            case THURSDAY -> "Jueves";
            case FRIDAY -> "Viernes";
            case SATURDAY -> "Sábado";
            case SUNDAY -> null;
        };
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colLaboratorio.setCellValueFactory(new PropertyValueFactory<>("laboratorio"));
        colActividad.setCellValueFactory(new PropertyValueFactory<>("actividad"));
        colFecha.setCellValueFactory(new PropertyValueFactory<>("fecha"));
        colHoraEntrada.setCellValueFactory(new PropertyValueFactory<>("horaEntrada"));
        colHoraSalida.setCellValueFactory(new PropertyValueFactory<>("horaSalida"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        fijarDiaDeHoy();

        Cuatrimestre activo = new Cuatrimestre().obtenerActivo();
        idCuatrimestreActivo = (activo != null) ? activo.getIdCuatrimestre() : -1;

        cargarTabla();
        cargarLaboratorios();
        cargarHorarios();
        cargarActividades();

        // El laboratorio y el día determinan qué bloques de horario se pueden
        // elegir: los bloques que ya tienen una clase regular asignada ni
        // siquiera aparecen en el combobox. Además, los equipos libres
        // dependen del laboratorio, el día y el bloque elegidos (un equipo
        // ya usado en ese día/bloque, o con una incidencia pendiente, no
        // debe aparecer como disponible).
        cmbLab.valueProperty().addListener((obs, anterior, nuevo) -> {
            actualizarBloquesDisponibles();
            cargarEquiposDisponibles();
        });
        cmbHorario.valueProperty().addListener((obs, anterior, nuevo) -> cargarEquiposDisponibles());

        txtMatricula.textProperty().addListener((observable, anterior, nuevo) -> {
            if (nuevo.length() >= 10) {
                Registro reg = new Registro();
                Alumno alumno = reg.buscarAlumno(nuevo);
                if (alumno != null) {
                    txtNombre.setText(alumno.getNombre());
                    txtGrupo.setText(alumno.getGrupo());
                } else {
                    txtNombre.clear();
                    txtGrupo.clear();
                }
            }
        });

        txtMatricula.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!valorNuevo.matches("\\d{0,10}")) {
                txtMatricula.setText(valorAnterior);
            }
        });
    }

    // En la vista de alumno solo se muestran los últimos 20 registros
    // (ya vienen ordenados del más reciente al más antiguo); en la
    // vista de administrador (RegistrosAdminController) sí se listan todos.
    private static final int MAX_REGISTROS_ALUMNO = 20;

    private void cargarTabla() {

        Registro reg = new Registro();

        ObservableList<Registro> lista = idCuatrimestreActivo != -1
                ? reg.obtenerRegistros(idCuatrimestreActivo)
                : FXCollections.observableArrayList();

        if (lista.size() > MAX_REGISTROS_ALUMNO) {
            lista = FXCollections.observableArrayList(lista.subList(0, MAX_REGISTROS_ALUMNO));
        }

        tblRegistros.setItems(lista);
    }

    // El día ya no es elegible: se muestra como una etiqueta fija con el
    // día de hoy, para que no se puedan hacer registros de días anteriores
    // ni posteriores. Si hoy es domingo (no hay actividad ese día), se
    // deshabilita todo el formulario.
    private void fijarDiaDeHoy() {

        diaFijo = diaDeHoy();

        if (diaFijo == null) {

            lblDia.setText("Sin servicio (domingo)");

            cmbLab.setDisable(true);
            cmbHorario.setDisable(true);
            cmbActividad.setDisable(true);
            cmbEquipo.setDisable(true);
            btnRegistrar.setDisable(true);

            lblDisponibilidad.setText("Hoy es domingo, no hay servicio de laboratorios extraclase.");

            return;
        }

        lblDia.setText(diaFijo);
    }

    private void cargarHorarios() {

        RegistroHorario rh = new RegistroHorario();

        cmbHorario.getItems().setAll(rh.obtenerBloques());

    }

    // Vuelve a llenar el combobox de horarios quitando los bloques que ya
    // tienen una clase regular en el laboratorio/día elegidos, para que no
    // se puedan seleccionar horarios ocupados por una materia.
    private void actualizarBloquesDisponibles() {

        String lab = cmbLab.getValue();
        String dia = diaFijo;

        if (lab == null || dia == null || idCuatrimestreActivo == -1) {
            cargarHorarios();
            return;
        }

        Registro regAux = new Registro();
        int idLaboratorio = regAux.obtenerIdLaboratorio(lab);

        BloqueHorario seleccionado = cmbHorario.getValue();

        RegistroHorario rh = new RegistroHorario();
        java.util.List<BloqueHorario> disponibles = rh.obtenerBloquesDisponibles(idLaboratorio, dia, idCuatrimestreActivo);

        // Si el día elegido es hoy, quita también los bloques cuya hora de
        // inicio ya pasó, para no permitir registrar un horario anterior
        // al momento actual.
        if (dia.equals(diaDeHoy())) {

            LocalTime ahora = LocalTime.now();

            disponibles = disponibles.stream()
                    .filter(b -> {
                        try {
                            return !LocalTime.parse(b.getHoraInicio()).isBefore(ahora);
                        } catch (Exception e) {
                            return true;
                        }
                    })
                    .toList();
        }

        cmbHorario.getItems().setAll(disponibles);

        if (seleccionado != null && cmbHorario.getItems().contains(seleccionado)) {
            cmbHorario.setValue(seleccionado);
        } else {
            cmbHorario.getSelectionModel().clearSelection();
        }
    }

    private void cargarLaboratorios() {

        Registro reg = new Registro();

        cmbLab.setItems(reg.obtenerLaboratorios());
    }

    private void cargarActividades() {

        cmbActividad.getItems().setAll(new Actividad().obtenerActivas());

    }

    // Solo muestra en el combobox los equipos LIBRES del laboratorio, día
    // y bloque elegidos. Si aún no se ha elegido día/bloque, se muestran
    // los libres "en este momento" como vista previa.
    private void cargarEquiposDisponibles() {

        cmbEquipo.getItems().clear();
        lblDisponibilidad.setText("");

        if (cmbLab.getValue() == null) {
            return;
        }

        Registro regAux = new Registro();
        int idLaboratorio = regAux.obtenerIdLaboratorio(cmbLab.getValue());

        EquipoComputo modeloEquipo = new EquipoComputo();
        int total = modeloEquipo.contarTotal(idLaboratorio);

        String dia = diaFijo;
        BloqueHorario bloque = cmbHorario.getValue();

        if (dia != null && bloque != null && idCuatrimestreActivo != -1) {

            // Resguardo: aunque el combobox de horario ya no debería ofrecer
            // bloques con clase regular, se valida de nuevo por si el
            // horario cambió de laboratorio/día entre selecciones.
            RegistroHorario rhValidacion = new RegistroHorario();
            if (rhValidacion.tieneClaseRegular(idLaboratorio, dia, bloque.getIdBloque(), idCuatrimestreActivo)) {
                lblDisponibilidad.setText("Laboratorio no disponible, en " + dia + " en ese Horario");
                return;
            }

            // Ya se eligió día y horario: filtra por ocupación real de ese bloque
            var disponibles = modeloEquipo.obtenerDisponiblesPorLaboratorioYHorario(
                    idLaboratorio, dia, bloque.getIdBloque(), idCuatrimestreActivo);

            cmbEquipo.getItems().setAll(disponibles);

            lblDisponibilidad.setText(disponibles.size() + " de " + total
                    + " equipos disponibles para " + dia + " en ese horario");

        } else {

            // Aún no se elige día/horario: muestra disponibilidad general como referencia
            var disponibles = modeloEquipo.obtenerDisponiblesPorLaboratorio(idLaboratorio);

            cmbEquipo.getItems().setAll(disponibles);

            lblDisponibilidad.setText(disponibles.size() + " de " + total
                    + " equipos disponibles en este laboratorio ahora mismo (elige día y horario para confirmar)");
        }
    }

    @FXML
    public void registrarEntrada() {

        Registro reg = new Registro();

        if (idCuatrimestreActivo == -1) {
            mostrarAlerta("Error", "No hay un cuatrimestre activo configurado. Avisa al administrador.", AlertType.ERROR);
            return;
        }

        if (txtMatricula.getText().trim().isEmpty()
                || txtNombre.getText().trim().isEmpty()
                || txtGrupo.getText().trim().isEmpty()
                || cmbLab.getValue() == null
                || diaFijo == null
                || cmbActividad.getValue() == null
                || cmbEquipo.getValue() == null) {

            mostrarAlerta("Error", "Completa todos los campos, incluyendo actividad y equipo.", AlertType.ERROR);
            return;
        }

        if (txtMatricula.getText().trim().length() != 10) {
            mostrarAlerta("Error", "La matrícula debe tener exactamente 10 dígitos.", AlertType.ERROR);
            return;
        }

        BloqueHorario bloque = cmbHorario.getValue();

        if (bloque == null) {
            mostrarAlerta("Error", "Selecciona un horario.", AlertType.ERROR);
            return;
        }

        // Resguardo final: el combobox de día siempre queda fijo en el día
        // de hoy (no se puede elegir otro), así que solo queda validar que
        // el bloque de horario elegido no haya quedado en el pasado (p. ej.
        // la pantalla llevaba tiempo abierta y ya avanzó la hora).
        String diaHoy = diaDeHoy();

        if (diaHoy == null || !diaHoy.equals(diaFijo)) {
            mostrarAlerta("Error", "El día de registro ya no coincide con el día de hoy. Vuelve a abrir la pantalla.", AlertType.ERROR);
            fijarDiaDeHoy();
            return;
        }

        try {
            if (LocalTime.parse(bloque.getHoraInicio()).isBefore(LocalTime.now())) {
                mostrarAlerta("Error", "Ese horario ya pasó. Selecciona uno posterior a la hora actual.", AlertType.ERROR);
                actualizarBloquesDisponibles();
                return;
            }
        } catch (Exception ignorado) {
            // Si no se puede interpretar la hora, se deja pasar la validación de horario.
        }

        int idLaboratorio = reg.obtenerIdLaboratorio(cmbLab.getValue());

        // Valida que el bloque no esté ocupado por clase regular ni sin equipos libres
        RegistroHorario rhValidacion = new RegistroHorario();

        String mensajeOcupado = rhValidacion.verificarOcupado(
                idLaboratorio, diaFijo, bloque.getIdBloque(), idCuatrimestreActivo);

        if (mensajeOcupado != null) {
            mostrarAlerta("Horario no disponible", mensajeOcupado, AlertType.WARNING);
            cargarEquiposDisponibles();
            return;
        }

        reg.setMatricula(txtMatricula.getText());
        reg.setIdLaboratorio(idLaboratorio);
        reg.setLaboratorio(cmbLab.getValue());
        reg.setDia(diaFijo);
        reg.setIdBloque(bloque.getIdBloque());
        reg.setIdCuatrimestre(idCuatrimestreActivo);
        reg.setIdEquipo(cmbEquipo.getValue().getIdEquipo());
        reg.setIdActividad(cmbActividad.getValue().getIdActividad());

        reg.setHoraEntrada(LocalTime.parse(bloque.getHoraInicio()));
        reg.setHoraSalida(LocalTime.parse(bloque.getHoraFin()));

        boolean respuesta = reg.insertarRegistro(reg);

        if (respuesta) {

            mostrarAlerta("Registro correcto", "Entrada registrada correctamente.", AlertType.INFORMATION);

            cargarTabla();
            limpiarCampos();

        } else {

            mostrarAlerta("Error", "No se pudo registrar la entrada.", AlertType.ERROR);
        }
    }

    @FXML
    private void salir(ActionEvent event) {

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

    // Cancela (sin borrar) el registro seleccionado en la tabla. Solo se
    // permite cancelar un registro de HOY cuya hora de entrada todavía no
    // se haya cumplido; la restricción real se aplica en Registro.cancelar
    // (a nivel SQL), aquí solo se filtra la selección para dar un mensaje
    // más claro antes de intentarlo.
    @FXML
    private void cancelarRegistro() {

        Registro seleccionado = tblRegistros.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Error", "Selecciona de la tabla el registro que quieres cancelar.", AlertType.ERROR);
            return;
        }

        if (seleccionado.getEstado() != null && seleccionado.getEstado().equalsIgnoreCase("Cancelado")) {
            mostrarAlerta("Error", "Ese registro ya está cancelado.", AlertType.ERROR);
            return;
        }

        boolean esDeHoy = seleccionado.getFecha() != null
                && seleccionado.getFecha().isEqual(java.time.LocalDate.now());

        boolean esPosteriorAHora = seleccionado.getHoraEntrada() != null
                && seleccionado.getHoraEntrada().isAfter(LocalTime.now());

        if (!esDeHoy || !esPosteriorAHora) {
            mostrarAlerta("Error",
                    "Solo se pueden cancelar registros de hoy cuya hora todavía no se haya cumplido.",
                    AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Cancelar la reservación de \"" + seleccionado.getNombre()
                + "\" en " + seleccionado.getLaboratorio() + "? El registro no se borra, solo se marcará como cancelado.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (!respuesta.getText().equalsIgnoreCase("Aceptar")) {
                return;
            }

            boolean ok = new Registro().cancelar(seleccionado.getIdRegistro());

            if (ok) {
                mostrarAlerta("Éxito", "La reservación fue cancelada.", AlertType.INFORMATION);
                cargarTabla();
                cargarEquiposDisponibles();
            } else {
                mostrarAlerta("Error",
                        "No se pudo cancelar (puede que la hora ya haya pasado o ya esté cancelado).",
                        AlertType.ERROR);
            }
        });
    }

    @FXML
    private void abrirHorarios(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(getClass().getResource("horario.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("Horario de Laboratorios");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }


    @FXML
    public void limpiarCampos() {

        txtMatricula.clear();
        txtNombre.clear();
        txtGrupo.clear();

        cmbLab.getSelectionModel().clearSelection();
        // El día NO se limpia: debe permanecer fijo en el día de hoy.
        cmbHorario.getSelectionModel().clearSelection();
        cmbActividad.getSelectionModel().clearSelection();
        cmbEquipo.getItems().clear();
        lblDisponibilidad.setText("");

        txtMatricula.requestFocus();
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);

        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);

        alerta.showAndWait();
    }
}
