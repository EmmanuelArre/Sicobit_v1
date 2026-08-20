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
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;

import utng.gtid234.jeae.modelo.Alumno;
import utng.gtid234.jeae.modelo.Grupo;

public class GestionAlumnosController implements Initializable {

    // ---- Panel de grupos ----
    @FXML
    private TextField txtNombreGrupo;
    @FXML
    private Button btnAgregarGrupo;
    @FXML
    private Button btnEditarGrupo;
    @FXML
    private Button btnEliminarGrupo;
    @FXML
    private ListView<Grupo> listaGrupos;

    // ---- Panel de alumnos ----
    @FXML
    private javafx.scene.control.Label lblGrupoSeleccionado;
    @FXML
    private TextField txtMatricula;
    @FXML
    private TextField txtNombreAlumno;
    @FXML
    private ComboBox<Grupo> cmbGrupoAlumno;
    @FXML
    private Button btnAgregarAlumno;
    @FXML
    private Button btnEditarAlumno;
    @FXML
    private Button btnEliminarAlumno;
    @FXML
    private Button btnLimpiarAlumno;
    @FXML
    private Button btnImportarExcel;
    @FXML
    private TableView<Alumno> tablaAlumnos;
    @FXML
    private TableColumn<Alumno, String> colMatricula;
    @FXML
    private TableColumn<Alumno, String> colNombre;
    @FXML
    private TableColumn<Alumno, String> colGrupo;

    private Grupo grupoSeleccionado = null;
    private String matriculaSeleccionada = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));

        listaGrupos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                grupoSeleccionado = seleccionado;
                txtNombreGrupo.setText(seleccionado.getNombre());
                lblGrupoSeleccionado.setText("Alumnos del grupo: " + seleccionado.getNombre());
                cmbGrupoAlumno.setValue(seleccionado);
                cargarAlumnosDelGrupo();
            }

        });

        tablaAlumnos.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                cargarAlumnoEnFormulario(seleccionado);
            }

        });

        txtMatricula.textProperty().addListener((observable, valorAnterior, valorNuevo) -> {
            if (!valorNuevo.matches("\\d{0,10}")) {
                txtMatricula.setText(valorAnterior);
            }
        });

        cargarGrupos();
    }

    //=========================================
    // GRUPOS
    //=========================================
    private void cargarGrupos() {

        ObservableList<Grupo> lista = FXCollections.observableArrayList(new Grupo().obtenerTodos());
        listaGrupos.setItems(lista);
        cmbGrupoAlumno.setItems(lista);
    }

    @FXML
    private void agregarGrupo() {

        String nombre = txtNombreGrupo.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "Escribe el nombre del grupo.", AlertType.ERROR);
            return;
        }

        Grupo g = new Grupo();
        g.setNombre(nombre);

        Grupo modeloGrupo = new Grupo();
        boolean ok = modeloGrupo.agregar(g);

        if (ok) {
            mostrarAlerta("Éxito", "Grupo agregado.", AlertType.INFORMATION);
            txtNombreGrupo.clear();
            cargarGrupos();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el grupo (puede que ya exista). "
                    + mensajeCausa(modeloGrupo.getUltimoError()), AlertType.ERROR);
        }
    }

    @FXML
    private void editarGrupo() {

        if (grupoSeleccionado == null) {
            mostrarAlerta("Error", "Selecciona un grupo de la lista primero.", AlertType.ERROR);
            return;
        }

        String nombre = txtNombreGrupo.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "Escribe el nombre del grupo.", AlertType.ERROR);
            return;
        }

        Grupo g = new Grupo();
        g.setIdGrupo(grupoSeleccionado.getIdGrupo());
        g.setNombre(nombre);

        boolean ok = new Grupo().editar(g);

        if (ok) {
            mostrarAlerta("Éxito", "Grupo actualizado.", AlertType.INFORMATION);
            cargarGrupos();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el grupo.", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarGrupo() {

        if (grupoSeleccionado == null) {
            mostrarAlerta("Error", "Selecciona un grupo de la lista primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este grupo? Solo se puede eliminar si no tiene alumnos registrados.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new Grupo().eliminar(grupoSeleccionado.getIdGrupo());

                if (ok) {
                    mostrarAlerta("Éxito", "Grupo eliminado.", AlertType.INFORMATION);
                    grupoSeleccionado = null;
                    txtNombreGrupo.clear();
                    tablaAlumnos.getItems().clear();
                    lblGrupoSeleccionado.setText("Alumnos — selecciona un grupo a la izquierda");
                    cargarGrupos();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar (probablemente tiene alumnos asociados).", AlertType.ERROR);
                }
            }
        });
    }

    //=========================================
    // ALUMNOS (siempre ligados a un grupo ya registrado)
    //=========================================
    private void cargarAlumnosDelGrupo() {

        if (grupoSeleccionado == null) {
            tablaAlumnos.getItems().clear();
            return;
        }

        ObservableList<Alumno> lista =
                FXCollections.observableArrayList(new Alumno().obtenerPorGrupo(grupoSeleccionado.getIdGrupo()));

        tablaAlumnos.setItems(lista);
    }

    private void cargarAlumnoEnFormulario(Alumno a) {

        matriculaSeleccionada = a.getMatricula();
        txtMatricula.setText(a.getMatricula());
        txtMatricula.setDisable(true); // la matrícula es la llave, no se edita
        txtNombreAlumno.setText(a.getNombre());

        for (Grupo g : cmbGrupoAlumno.getItems()) {
            if (g.getIdGrupo() == a.getIdGrupo()) {
                cmbGrupoAlumno.setValue(g);
                break;
            }
        }
    }

    @FXML
    private void agregarAlumno() {

        Alumno a = leerFormularioAlumno();

        if (a == null) {
            return;
        }

        Alumno modeloAlumno = new Alumno();
        boolean ok = modeloAlumno.agregar(a);

        if (ok) {
            mostrarAlerta("Éxito", "Alumno agregado al grupo " + a.getGrupo() + ".", AlertType.INFORMATION);
            limpiarAlumno();
            cargarAlumnosDelGrupo();
        } else {
            mostrarAlerta("Error", "No se pudo agregar el alumno (verifica que la matrícula no esté repetida). "
                    + mensajeCausa(modeloAlumno.getUltimoError()), AlertType.ERROR);
        }
    }

    @FXML
    private void editarAlumno() {

        if (matriculaSeleccionada == null) {
            mostrarAlerta("Error", "Selecciona un alumno de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alumno a = leerFormularioAlumno();

        if (a == null) {
            return;
        }

        a.setMatricula(matriculaSeleccionada);

        Alumno modeloAlumno = new Alumno();
        boolean ok = modeloAlumno.editar(a);

        if (ok) {
            mostrarAlerta("Éxito", "Alumno actualizado.", AlertType.INFORMATION);
            limpiarAlumno();
            cargarAlumnosDelGrupo();
        } else {
            mostrarAlerta("Error", "No se pudo actualizar el alumno. " + mensajeCausa(modeloAlumno.getUltimoError()), AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarAlumno() {

        if (matriculaSeleccionada == null) {
            mostrarAlerta("Error", "Selecciona un alumno de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este alumno? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                Alumno modeloAlumno = new Alumno();
                boolean ok = modeloAlumno.eliminar(matriculaSeleccionada);

                if (ok) {
                    mostrarAlerta("Éxito", "Alumno eliminado.", AlertType.INFORMATION);
                    limpiarAlumno();
                    cargarAlumnosDelGrupo();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar (puede tener registros asociados). "
                            + mensajeCausa(modeloAlumno.getUltimoError()), AlertType.ERROR);
                }
            }
        });
    }

    // Abre en una ventana modal la pantalla de importación masiva de
    // alumnos desde un archivo .xlsx (matrícula, nombre y grupo). Al
    // cerrarla, se refresca la tabla por si el grupo actualmente
    // seleccionado recibió alumnos nuevos.
    @FXML
    private void abrirImportarExcel() {

        try {

            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("ImportarAlumnos.fxml"));
            javafx.scene.Parent vista = loader.load();

            javafx.stage.Stage ventana = new javafx.stage.Stage();
            ventana.setTitle("Importar alumnos desde Excel");
            ventana.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            ventana.setScene(new javafx.scene.Scene(vista));
            ventana.showAndWait();

            cargarGrupos();
            cargarAlumnosDelGrupo();

        } catch (Exception e) {

            e.printStackTrace();
            mostrarAlerta("Error", "No se pudo abrir la pantalla de importación.", AlertType.ERROR);

        }
    }

    @FXML
    private void limpiarAlumno() {

        matriculaSeleccionada = null;
        txtMatricula.clear();
        txtMatricula.setDisable(false);
        txtNombreAlumno.clear();

        if (grupoSeleccionado != null) {
            cmbGrupoAlumno.setValue(grupoSeleccionado);
        }

        tablaAlumnos.getSelectionModel().clearSelection();

    }

    private Alumno leerFormularioAlumno() {

        String matricula = txtMatricula.getText().trim();
        String nombre = txtNombreAlumno.getText().trim();
        Grupo grupo = cmbGrupoAlumno.getValue();

        if (matricula.isEmpty() || nombre.isEmpty() || grupo == null) {
            mostrarAlerta("Error", "Matrícula, nombre y grupo son obligatorios. El grupo debe ser uno ya registrado.", AlertType.ERROR);
            return null;
        }

        if (!matricula.matches("\\d{10}")) {
            mostrarAlerta("Error", "La matrícula debe tener exactamente 10 dígitos numéricos.", AlertType.ERROR);
            return null;
        }

        Alumno a = new Alumno();
        a.setMatricula(matricula);
        a.setNombre(nombre);
        a.setIdGrupo(grupo.getIdGrupo());
        a.setGrupo(grupo.getNombre());

        return a;
    }

    // Muestra la causa real reportada por MySQL (si la hay) para que el
    // usuario pueda diagnosticar el problema en vez de solo ver un mensaje
    // genérico (antes el error real solo se imprimía en la consola).
    private String mensajeCausa(String ultimoError) {

        if (ultimoError == null || ultimoError.isBlank()) {
            return "";
        }

        return "\n\nDetalle: " + ultimoError;
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
