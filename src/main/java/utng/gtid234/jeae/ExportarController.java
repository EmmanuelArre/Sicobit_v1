package utng.gtid234.jeae;

import java.io.File;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Exportador;

public class ExportarController implements Initializable {
    @FXML
    private Button btnExportar;


    @FXML
    private ComboBox<String> cmbTabla;
    @FXML
    private ComboBox<String> cmbPeriodo;
    @FXML
    private ComboBox<String> cmbFormato;
    @FXML
    private Label lblEstado;

    private final Exportador exportador = new Exportador();

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbTabla.getItems().addAll("Registros de extraclase", "Incidencias", "Préstamos de equipo");
        cmbTabla.getSelectionModel().selectFirst();

        cmbPeriodo.getItems().addAll("Día", "Semana", "Mes", "Cuatrimestre", "Todos");
        cmbPeriodo.getSelectionModel().select("Todos");

        cmbFormato.getItems().addAll("CSV", "Excel", "PDF");
        cmbFormato.getSelectionModel().selectFirst();
    }

    private String tablaSeleccionada() {

        return switch (cmbTabla.getValue()) {
            case "Registros de extraclase" -> "registro";
            case "Incidencias" -> "incidencia";
            case "Préstamos de equipo" -> "prestamo_equipo";
            default -> "registro";
        };
    }

    @FXML
    private void exportar() {

        String tabla = tablaSeleccionada();
        String periodo = cmbPeriodo.getValue();
        String formato = cmbFormato.getValue();

        String[] encabezados = exportador.obtenerEncabezados(tabla);
        List<String[]> filas = exportador.obtenerFilas(tabla, periodo);

        if (filas.isEmpty()) {

            if (exportador.obtenerUltimoError() != null) {
                lblEstado.setText("Error de conexión con la base de datos.");
                lblEstado.setStyle("-fx-text-fill:#E74C3C;");
                mostrarAlerta("Error de conexión", exportador.obtenerUltimoError(), AlertType.ERROR);
                return;
            }

            lblEstado.setText("No hay registros para ese periodo.");
            lblEstado.setStyle("-fx-text-fill:#F39C12;");
        }

        String extension = switch (formato) {
            case "Excel" -> "xls";
            case "PDF" -> "pdf";
            default -> "csv";
        };

        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar exportación");
        chooser.setInitialFileName(tabla + "_" + periodo + "." + extension);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter(formato, "*." + extension)
        );

        Stage stage = (Stage) cmbTabla.getScene().getWindow();
        File destino = chooser.showSaveDialog(stage);

        if (destino == null) {
            return;
        }

        boolean ok = switch (formato) {
            case "Excel" -> exportador.exportarExcel(encabezados, filas, destino, cmbTabla.getValue());
            case "PDF" -> exportador.exportarPDF(cmbTabla.getValue() + " - " + periodo, encabezados, filas, destino);
            default -> exportador.exportarCSV(encabezados, filas, destino);
        };

        if (ok) {
            lblEstado.setText("Archivo exportado correctamente: " + destino.getName());
            lblEstado.setStyle("-fx-text-fill:#2EAA4A;");
            mostrarAlerta("Éxito", "Se exportaron " + filas.size() + " registros a " + destino.getName(), AlertType.INFORMATION);
        } else {
            lblEstado.setText("Ocurrió un error al exportar.");
            lblEstado.setStyle("-fx-text-fill:#E74C3C;");
            mostrarAlerta("Error", "No se pudo generar el archivo.", AlertType.ERROR);
        }
    }

    private void mostrarAlerta(String titulo, String mensaje, AlertType tipo) {

        Alert alerta = new Alert(tipo);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.setContentText(mensaje);
        alerta.showAndWait();

    }
}
