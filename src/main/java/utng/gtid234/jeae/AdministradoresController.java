package utng.gtid234.jeae;

import java.net.URL;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.Set;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;

import utng.gtid234.jeae.modelo.Administrador;
import utng.gtid234.jeae.modelo.Modulo;
import utng.gtid234.jeae.modelo.Rol;

public class AdministradoresController implements Initializable {

    @FXML
    private TextField txtUsuario;
    @FXML
    private PasswordField txtPassword;
    @FXML
    private TextField txtNombre;
    @FXML
    private ComboBox<Rol> cmbRol;
    @FXML
    private Button btnAgregar;
    @FXML
    private Button btnLimpiar;
    @FXML
    private Button btnEliminar;

    @FXML
    private TableView<Administrador> tablaAdmins;
    @FXML
    private TableColumn<Administrador, Integer> colId;
    @FXML
    private TableColumn<Administrador, String> colUsuario;
    @FXML
    private TableColumn<Administrador, String> colNombre;
    @FXML
    private TableColumn<Administrador, String> colRol;

    // Roles y permisos
    @FXML
    private TextField txtNuevoRol;
    @FXML
    private Button btnCrearRol;
    @FXML
    private ComboBox<Rol> cmbRolPermisos;
    @FXML
    private VBox flowModulos;
    @FXML
    private Button btnGuardarPermisos;
    @FXML
    private Button btnEliminarRol;

    private int idSeleccionado = -1;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colId.setCellValueFactory(new PropertyValueFactory<>("idAdministrador"));
        colUsuario.setCellValueFactory(new PropertyValueFactory<>("usuario"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colRol.setCellValueFactory(new PropertyValueFactory<>("rol"));

        tablaAdmins.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {

            if (seleccionado != null) {
                idSeleccionado = seleccionado.getIdAdministrador();
            }

        });

        cargarRoles();
        cargarModulosEnPantalla();
        cargarTabla();

        cmbRolPermisos.valueProperty().addListener((obs, anterior, nuevo) -> cargarPermisosDelRol());
    }

    private void cargarRoles() {

        ObservableList<Rol> roles = FXCollections.observableArrayList(new Rol().obtenerTodos());

        cmbRol.setItems(roles);
        if (!roles.isEmpty()) {
            cmbRol.getSelectionModel().selectFirst();
        }

        cmbRolPermisos.setItems(roles);
        if (!roles.isEmpty()) {
            cmbRolPermisos.getSelectionModel().selectFirst();
        }
    }

    // Dibuja un checkbox por cada módulo existente en el sistema
    private void cargarModulosEnPantalla() {

        flowModulos.getChildren().clear();

        for (Modulo m : new Modulo().obtenerAsignables()) {

            CheckBox cb = new CheckBox(m.getNombre());
            cb.setUserData(m.getIdModulo());
            cb.setMaxWidth(Double.POSITIVE_INFINITY);
            cb.getStyleClass().add("modulo-check");
            flowModulos.getChildren().add(cb);

        }

        cargarPermisosDelRol();
    }

    // Marca los checkbox de los módulos que el rol seleccionado ya puede ver
    private void cargarPermisosDelRol() {

        if (cmbRolPermisos.getValue() == null) {
            return;
        }

        Rol rolSel = cmbRolPermisos.getValue();

        if (rolSel.isEsSuperAdmin()) {
            // El superadmin siempre ve todo; se marcan todos y se deshabilitan
            for (var nodo : flowModulos.getChildren()) {
                CheckBox cb = (CheckBox) nodo;
                cb.setSelected(true);
                cb.setDisable(true);
            }
            return;
        }

        Set<Integer> permitidos = new Rol().obtenerModulosDeRol(rolSel.getIdRol());

        for (var nodo : flowModulos.getChildren()) {
            CheckBox cb = (CheckBox) nodo;
            cb.setDisable(false);
            cb.setSelected(permitidos.contains((Integer) cb.getUserData()));
        }
    }

    private void cargarTabla() {

        ObservableList<Administrador> lista =
                FXCollections.observableArrayList(new Administrador().obtenerTodos());

        tablaAdmins.setItems(lista);
    }

    @FXML
    private void agregar() {

        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        String nombre = txtNombre.getText().trim();
        Rol rol = cmbRol.getValue();

        if (usuario.isEmpty() || password.isEmpty() || nombre.isEmpty() || rol == null) {
            mostrarAlerta("Error", "Todos los campos son obligatorios.", AlertType.ERROR);
            return;
        }

        Administrador a = new Administrador();
        a.setUsuario(usuario);
        a.setPassword(password);
        a.setNombre(nombre);
        a.setIdRol(rol.getIdRol());

        boolean ok = new Administrador().agregar(a);

        if (ok) {
            mostrarAlerta("Éxito", "Administrador agregado correctamente.", AlertType.INFORMATION);
            limpiar();
            cargarTabla();
        } else {
            mostrarAlerta("Error", "No se pudo agregar (el usuario ya existe).", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminar() {

        if (idSeleccionado == -1) {
            mostrarAlerta("Error", "Selecciona un administrador de la tabla primero.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar este administrador? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new Administrador().eliminar(idSeleccionado);

                if (ok) {
                    mostrarAlerta("Éxito", "Administrador eliminado.", AlertType.INFORMATION);
                    idSeleccionado = -1;
                    cargarTabla();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar.", AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void crearRol() {

        String nombre = txtNuevoRol.getText().trim();

        if (nombre.isEmpty()) {
            mostrarAlerta("Error", "Escribe el nombre del rol nuevo.", AlertType.ERROR);
            return;
        }

        boolean ok = new Rol().crear(nombre);

        if (ok) {
            mostrarAlerta("Éxito", "Rol \"" + nombre + "\" creado. Ahora puedes elegir qué apartados puede ver.", AlertType.INFORMATION);
            txtNuevoRol.clear();
            cargarRoles();
        } else {
            mostrarAlerta("Error", "No se pudo crear el rol (¿ya existe uno con ese nombre?).", AlertType.ERROR);
        }
    }

    @FXML
    private void eliminarRol() {

        Rol rolSel = cmbRolPermisos.getValue();

        if (rolSel == null) {
            mostrarAlerta("Error", "Elige un rol primero.", AlertType.ERROR);
            return;
        }

        if (rolSel.isEsSuperAdmin()) {
            mostrarAlerta("Error", "El rol de superadministrador no se puede eliminar.", AlertType.ERROR);
            return;
        }

        Alert confirmacion = new Alert(AlertType.CONFIRMATION);
        confirmacion.setTitle("Confirmar");
        confirmacion.setHeaderText(null);
        confirmacion.setContentText("¿Eliminar el rol \"" + rolSel.getNombre() + "\"? Esta acción no se puede deshacer.");

        confirmacion.showAndWait().ifPresent(respuesta -> {

            if (respuesta.getText().equalsIgnoreCase("Aceptar")) {

                boolean ok = new Rol().eliminar(rolSel.getIdRol());

                if (ok) {
                    mostrarAlerta("Éxito", "Rol eliminado.", AlertType.INFORMATION);
                    cargarRoles();
                    cargarModulosEnPantalla();
                } else {
                    mostrarAlerta("Error", "No se pudo eliminar el rol (puede que aún tenga administradores asignados).", AlertType.ERROR);
                }
            }
        });
    }

    @FXML
    private void guardarPermisos() {

        Rol rolSel = cmbRolPermisos.getValue();

        if (rolSel == null) {
            mostrarAlerta("Error", "Elige un rol primero.", AlertType.ERROR);
            return;
        }

        if (rolSel.isEsSuperAdmin()) {
            mostrarAlerta("Información", "El superadministrador siempre ve todos los apartados; no hace falta guardar nada.", AlertType.INFORMATION);
            return;
        }

        Set<Integer> seleccionados = new HashSet<>();

        for (var nodo : flowModulos.getChildren()) {
            CheckBox cb = (CheckBox) nodo;
            if (cb.isSelected()) {
                seleccionados.add((Integer) cb.getUserData());
            }
        }

        boolean ok = new Rol().guardarModulosDeRol(rolSel.getIdRol(), seleccionados);

        if (ok) {
            mostrarAlerta("Éxito", "Permisos del rol \"" + rolSel.getNombre() + "\" actualizados.", AlertType.INFORMATION);
        } else {
            mostrarAlerta("Error", "No se pudieron guardar los permisos.", AlertType.ERROR);
        }
    }

    @FXML
    private void limpiar() {

        idSeleccionado = -1;
        txtUsuario.clear();
        txtPassword.clear();
        txtNombre.clear();
        if (!cmbRol.getItems().isEmpty()) {
            cmbRol.getSelectionModel().selectFirst();
        }
        tablaAdmins.getSelectionModel().clearSelection();

    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
