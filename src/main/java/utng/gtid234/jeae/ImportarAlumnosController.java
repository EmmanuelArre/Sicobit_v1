package utng.gtid234.jeae;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import utng.gtid234.jeae.modelo.Alumno;
import utng.gtid234.jeae.modelo.Grupo;
import utng.gtid234.jeae.modelo.LectorExcel;


public class ImportarAlumnosController implements Initializable {

    @FXML
    private TextField txtArchivo;
    @FXML
    private Button btnSeleccionar;
    @FXML
    private Button btnImportar;
    @FXML
    private Button btnCerrar;
    @FXML
    private Label lblResumen;

    @FXML
    private TableView<FilaImportacion> tablaPreview;
    @FXML
    private TableColumn<FilaImportacion, String> colMatricula;
    @FXML
    private TableColumn<FilaImportacion, String> colNombre;
    @FXML
    private TableColumn<FilaImportacion, String> colGrupo;
    @FXML
    private TableColumn<FilaImportacion, String> colEstado;

    private File archivoSeleccionado;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        colMatricula.setCellValueFactory(new PropertyValueFactory<>("matricula"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombre"));
        colGrupo.setCellValueFactory(new PropertyValueFactory<>("grupo"));
        colEstado.setCellValueFactory(new PropertyValueFactory<>("estado"));

        btnImportar.setDisable(true);
    }

    @FXML
    private void seleccionarArchivo() {

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Selecciona el archivo Excel (.xlsx) de alumnos");
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel (.xlsx)", "*.xlsx"));

        Stage stage = (Stage) txtArchivo.getScene().getWindow();
        File archivo = chooser.showOpenDialog(stage);

        if (archivo == null) {
            return;
        }

        archivoSeleccionado = archivo;
        txtArchivo.setText(archivo.getAbsolutePath());
        lblResumen.setText("");

        try {

            List<String[]> filas = LectorExcel.leerPrimeraHoja(archivo);
            ObservableList<FilaImportacion> vista = construirVistaPrevia(filas);

            tablaPreview.setItems(vista);
            btnImportar.setDisable(vista.isEmpty());

            if (vista.isEmpty()) {
                lblResumen.setText("El archivo no tiene filas con datos.");
                lblResumen.setStyle("-fx-text-fill:#F39C12;");
            } else {
                lblResumen.setText(vista.size() + " fila(s) leídas del archivo. Revisa la vista previa y presiona \"Importar\".");
                lblResumen.setStyle("-fx-text-fill:#42474E;");
            }

        } catch (Exception e) {

            e.printStackTrace();
            btnImportar.setDisable(true);
            tablaPreview.setItems(FXCollections.observableArrayList());
            mostrarAlerta("Error", "No se pudo leer el archivo. Verifica que sea un .xlsx válido.\n\nDetalle: "
                    + e.getMessage(), AlertType.ERROR);
        }
    }

    // Detecta si la primera fila es un encabezado (contiene las palabras
    // "matricula", "nombre" y "grupo") para saber en qué columna está
    // cada dato; si no hay encabezado reconocible, asume el orden
    // matrícula, nombre, grupo en las primeras tres columnas.
    private ObservableList<FilaImportacion> construirVistaPrevia(List<String[]> filas) {

        ObservableList<FilaImportacion> lista = FXCollections.observableArrayList();

        if (filas.isEmpty()) {
            return lista;
        }

        int colMat = 0, colNom = 1, colGru = 2;
        int primeraFilaDatos = 0;

        String[] posibleEncabezado = filas.get(0);
        int idx = indiceColumnaEncabezado(posibleEncabezado, "matricula", "matrícula");

        if (idx != -1) {

            colMat = idx;
            colNom = indiceColumnaEncabezado(posibleEncabezado, "nombre");
            colGru = indiceColumnaEncabezado(posibleEncabezado, "grupo");

            if (colNom == -1) colNom = 1;
            if (colGru == -1) colGru = 2;

            primeraFilaDatos = 1; // la fila 0 era el encabezado, no un alumno
        }

        for (int i = primeraFilaDatos; i < filas.size(); i++) {

            String[] fila = filas.get(i);

            String matricula = valorColumna(fila, colMat);
            String nombre = valorColumna(fila, colNom);
            String grupo = valorColumna(fila, colGru);

            if (matricula.isEmpty() && nombre.isEmpty() && grupo.isEmpty()) {
                continue; // fila totalmente vacía, se ignora sin mostrarla
            }

            lista.add(new FilaImportacion(matricula, nombre, grupo, "Pendiente"));
        }

        return lista;
    }

    private String valorColumna(String[] fila, int columna) {

        if (columna < 0 || columna >= fila.length || fila[columna] == null) {
            return "";
        }

        return fila[columna].trim();
    }

    private int indiceColumnaEncabezado(String[] encabezado, String... nombresPosibles) {

        for (int i = 0; i < encabezado.length; i++) {

            String celda = encabezado[i] == null ? "" : encabezado[i].trim().toLowerCase();

            for (String nombre : nombresPosibles) {
                if (celda.contains(nombre)) {
                    return i;
                }
            }
        }

        return -1;
    }

    @FXML
    private void importar() {

        if (tablaPreview.getItems().isEmpty()) {
            mostrarAlerta("Error", "Primero selecciona un archivo con datos.", AlertType.ERROR);
            return;
        }

        int importados = 0;
        int actualizados = 0;
        int errores = 0;

        for (FilaImportacion fila : tablaPreview.getItems()) {

            String resultado = procesarFila(fila);

            fila.setEstado(resultado);

            if (resultado.startsWith("Importado")) {
                importados++;
            } else if (resultado.startsWith("Actualizado")) {
                actualizados++;
            } else {
                errores++;
            }
        }

        tablaPreview.refresh();

        lblResumen.setText("Importación terminada: " + importados + " nuevo(s), "
                + actualizados + " actualizado(s), " + errores + " con error.");
        lblResumen.setStyle(errores > 0 ? "-fx-text-fill:#E74C3C;" : "-fx-text-fill:#2EAA4A;");

        mostrarAlerta("Importación terminada",
                importados + " alumno(s) nuevo(s), " + actualizados + " actualizado(s) y "
                        + errores + " con error. Revisa la columna \"Estado\" de la tabla para el detalle.",
                errores > 0 ? AlertType.WARNING : AlertType.INFORMATION);
    }

    // Valida y guarda una fila; regresa el texto que se muestra en la
    // columna "Estado" ("Importado", "Actualizado" o "Error: ...").
    private String procesarFila(FilaImportacion fila) {

        String matricula = fila.getMatricula();
        String nombre = fila.getNombre();
        String nombreGrupo = fila.getGrupo();

        if (matricula.isEmpty() || nombre.isEmpty() || nombreGrupo.isEmpty()) {
            return "Error: faltan datos (matrícula, nombre o grupo)";
        }

        if (!matricula.matches("\\d{10}")) {
            return "Error: la matrícula debe tener exactamente 10 dígitos";
        }

        Grupo grupo = new Grupo().obtenerOCrear(nombreGrupo);

        if (grupo == null) {
            return "Error: no se pudo crear/encontrar el grupo \"" + nombreGrupo + "\"";
        }

        Alumno modeloAlumno = new Alumno();
        Alumno existente = modeloAlumno.buscarAlumno(matricula);

        Alumno a = new Alumno();
        a.setMatricula(matricula);
        a.setNombre(nombre);
        a.setIdGrupo(grupo.getIdGrupo());
        a.setGrupo(grupo.getNombre());

        boolean ok;

        if (existente != null) {
            ok = modeloAlumno.editar(a);
            return ok ? "Actualizado" : "Error: " + causaOGenerico(modeloAlumno.getUltimoError());
        } else {
            ok = modeloAlumno.agregar(a);
            return ok ? "Importado" : "Error: " + causaOGenerico(modeloAlumno.getUltimoError());
        }
    }

    private String causaOGenerico(String ultimoError) {
        return (ultimoError == null || ultimoError.isBlank()) ? "no se pudo guardar" : ultimoError;
    }

    @FXML
    private void cerrar() {

        Stage stage = (Stage) btnCerrar.getScene().getWindow();
        stage.close();
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }

    // Fila de la vista previa/resultado de la importación.
    public static class FilaImportacion {

        private final String matricula;
        private final String nombre;
        private final String grupo;
        private String estado;

        public FilaImportacion(String matricula, String nombre, String grupo, String estado) {
            this.matricula = matricula;
            this.nombre = nombre;
            this.grupo = grupo;
            this.estado = estado;
        }

        public String getMatricula() {
            return matricula;
        }

        public String getNombre() {
            return nombre;
        }

        public String getGrupo() {
            return grupo;
        }

        public String getEstado() {
            return estado;
        }

        public void setEstado(String estado) {
            this.estado = estado;
        }
    }
}
