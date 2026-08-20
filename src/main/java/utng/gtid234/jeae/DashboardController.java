package utng.gtid234.jeae;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Administrador;

public class DashboardController implements Initializable {
    @FXML
    private Button btnCerrarSesion;


    @FXML
    private Label lblAdmin;

    @FXML
    private javafx.scene.control.Button btnAdministradores;

    @FXML
    private javafx.scene.control.Button btnInicio;
    @FXML
    private javafx.scene.control.Button btnRegistros;
    @FXML
    private javafx.scene.control.Button btnHorarioClase;
    @FXML
    private javafx.scene.control.Button btnBloquesHorario;
    @FXML
    private javafx.scene.control.Button btnCuatrimestres;
    @FXML
    private javafx.scene.control.Button btnProfesores;
    @FXML
    private javafx.scene.control.Button btnMaterias;
    @FXML
    private javafx.scene.control.Button btnGrupos;
    @FXML
    private javafx.scene.control.Button btnActividades;
    @FXML
    private javafx.scene.control.Button btnIncidencias;
    @FXML
    private javafx.scene.control.Button btnLaboratorios;
    @FXML
    private javafx.scene.control.Button btnReportes;
    @FXML
    private javafx.scene.control.Button btnExportar;
    @FXML
    private javafx.scene.control.Button btnConfiguracion;

    @FXML
    private VBox contenedorCentro;

    private Administrador administrador;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // sin inicialización especial
    }

    public void setAdministrador(Administrador administrador) {

        this.administrador = administrador;

        if (lblAdmin != null && administrador != null) {
            lblAdmin.setText("Hola, " + administrador.getNombre());
        }

        if (btnAdministradores != null) {
            boolean esSuperAdmin = administrador != null && administrador.esSuperAdmin();
            btnAdministradores.setVisible(esSuperAdmin);
            btnAdministradores.setManaged(esSuperAdmin);
        }

        // Oculta del menú los apartados que el rol de este administrador
        // no tiene permitidos (el superadministrador siempre los ve todos)
        aplicarPermiso(btnRegistros, "REGISTROS_EXTRACLASE");
        aplicarPermiso(btnHorarioClase, "HORARIOS_REGULARES");
        aplicarPermiso(btnBloquesHorario, "BLOQUES_HORARIO");
        aplicarPermiso(btnCuatrimestres, "CUATRIMESTRES");
        aplicarPermiso(btnProfesores, "PROFESORES");
        aplicarPermiso(btnMaterias, "MATERIAS");
        aplicarPermiso(btnGrupos, "GESTION_ALUMNOS");
        aplicarPermiso(btnActividades, "ACTIVIDADES");
        aplicarPermiso(btnIncidencias, "INCIDENCIAS");
        aplicarPermiso(btnLaboratorios, "LABORATORIOS");
        aplicarPermiso(btnReportes, "REPORTES");
        aplicarPermiso(btnExportar, "EXPORTAR");
        aplicarPermiso(btnConfiguracion, "RESPALDOS");
    }

    private void aplicarPermiso(javafx.scene.control.Button boton, String claveModulo) {

        if (boton == null) {
            return;
        }

        boolean puedeVer = administrador == null || administrador.puedeVer(claveModulo);

        boton.setVisible(puedeVer);
        boton.setManaged(puedeVer);
    }

    @FXML
    private void irAdministradores() {

        if (administrador == null || !administrador.esSuperAdmin()) {
            mostrarPlaceholder("Acceso restringido", "Solo el superadministrador puede acceder a esta sección.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Administradores.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Administradores", "No se pudo cargar la pantalla de administradores.");

        }
    }

    @FXML
    private void irInicio() {
        mostrarPlaceholder("Panel de Administrador", "Usa el menú lateral para navegar entre las secciones.");
    }

    @FXML
    private void irRegistros() {

        if (administrador != null && !administrador.puedeVer("REGISTROS_EXTRACLASE")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("RegistrosAdmin.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Registros", "No se pudo cargar la pantalla de registros.");

        }
    }

    @FXML
    private void irHorarioClase() {

        if (administrador != null && !administrador.puedeVer("HORARIOS_REGULARES")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("HorarioClase.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Horario normal", "No se pudo cargar la pantalla de horario normal.");

        }
    }

    @FXML
    private void irBloquesHorario() {

        if (administrador != null && !administrador.puedeVer("BLOQUES_HORARIO")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("BloquesHorario.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Bloques de horario", "No se pudo cargar la pantalla de bloques de horario.");

        }
    }

    @FXML
    private void irCuatrimestres() {

        if (administrador != null && !administrador.puedeVer("CUATRIMESTRES")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Cuatrimestres.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Cuatrimestres", "No se pudo cargar la pantalla de cuatrimestres.");

        }
    }

    @FXML
    private void irProfesores() {

        if (administrador != null && !administrador.puedeVer("PROFESORES")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Profesores.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Profesores", "No se pudo cargar la pantalla de profesores.");

        }
    }

    @FXML
    private void irMaterias() {

        if (administrador != null && !administrador.puedeVer("MATERIAS")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Materias.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Materias", "No se pudo cargar la pantalla de materias.");

        }
    }

    @FXML
    private void irGrupos() {

        if (administrador != null && !administrador.puedeVer("GESTION_ALUMNOS")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("GestionAlumnos.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Grupos y alumnos", "No se pudo cargar la pantalla de grupos y alumnos.");

        }
    }

    @FXML
    private void irActividades() {

        if (administrador != null && !administrador.puedeVer("ACTIVIDADES")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Actividades.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Actividades", "No se pudo cargar la pantalla de actividades.");

        }
    }

    @FXML
    private void irIncidencias() {

        if (administrador != null && !administrador.puedeVer("INCIDENCIAS")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Incidencias.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Incidencias", "No se pudo cargar la pantalla de incidencias.");

        }
    }

    @FXML
    private void irLaboratorios() {

        if (administrador != null && !administrador.puedeVer("LABORATORIOS")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Laboratorios.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Laboratorios", "No se pudo cargar la pantalla de laboratorios.");

        }
    }

    @FXML
    private void irReportes() {

        if (administrador != null && !administrador.puedeVer("REPORTES")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Reportes.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Reportes", "No se pudo cargar la pantalla de reportes.");

        }
    }

    @FXML
    private void irExportar() {

        if (administrador != null && !administrador.puedeVer("EXPORTAR")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Exportar.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Exportar", "No se pudo cargar la pantalla de exportación.");

        }
    }

    @FXML
    private void irConfiguracion() {

        if (administrador != null && !administrador.puedeVer("RESPALDOS")) {
            mostrarPlaceholder("Acceso restringido", "Tu rol no tiene permiso para ver este apartado.");
            return;
        }

        try {

            Parent vista = FXMLLoader.load(getClass().getResource("Respaldo.fxml"));

            contenedorCentro.getChildren().setAll(vista);
            VBox.setVgrow(vista, javafx.scene.layout.Priority.ALWAYS);

        } catch (Exception e) {

            e.printStackTrace();
            mostrarPlaceholder("Configuración de Respaldos", "No se pudo cargar la pantalla de respaldos.");

        }
    }

    @FXML
    private void cerrarSesion(javafx.event.ActionEvent event) {

        try {

            administrador = null;

            Parent root = FXMLLoader.load(getClass().getResource("Inicio.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("SICOBIT - Inicio");

        } catch (Exception e) {

            e.printStackTrace();

        }
    }

    private void mostrarPlaceholder(String titulo, String mensaje) {

        Label lblTitulo = new Label(titulo);
        lblTitulo.setFont(Font.font("System Bold", 22));

        Label lblMensaje = new Label(mensaje);
        lblMensaje.setStyle("-fx-text-fill:#6C757D; -fx-font-size:14;");

        contenedorCentro.getChildren().setAll(lblTitulo, lblMensaje);
    }
}
