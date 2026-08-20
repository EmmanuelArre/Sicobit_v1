package utng.gtid234.jeae;

import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import utng.gtid234.jeae.modelo.Reporte;

public class ReportesController implements Initializable {
    @FXML
    private Button btnGenerar;


    @FXML
    private ComboBox<String> cmbPeriodo;

    @FXML
    private Label lblTotalRegistros;
    @FXML
    private Label lblTotalIncidencias;
    @FXML
    private Label lblTotalPrestamos;
    @FXML
    private Label lblIncidenciasPendientes;
    @FXML
    private Label lblIncidenciasResueltas;

    @FXML
    private BarChart<String, Number> chartExtraclases;
    @FXML
    private BarChart<String, Number> chartPrestamos;
    @FXML
    private CategoryAxis ejeXExtraclases;
    @FXML
    private CategoryAxis ejeXPrestamos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbPeriodo.getItems().addAll("Día", "Semana", "Mes", "Cuatrimestre");
        cmbPeriodo.getSelectionModel().select("Semana");

        generar();
    }

    @FXML
    private void generar() {

        String periodo = cmbPeriodo.getValue();

        if (periodo == null) {
            periodo = "Semana";
        }

        Reporte reporte = new Reporte();

        lblTotalRegistros.setText(String.valueOf(reporte.obtenerTotalRegistros(periodo)));
        lblTotalIncidencias.setText(String.valueOf(reporte.obtenerTotalIncidencias(periodo)));
        lblTotalPrestamos.setText(String.valueOf(reporte.obtenerTotalPrestamos(periodo)));
        lblIncidenciasPendientes.setText(String.valueOf(reporte.obtenerIncidenciasPendientes(periodo)));
        lblIncidenciasResueltas.setText(String.valueOf(reporte.obtenerIncidenciasResueltas(periodo)));

        llenarGrafico(chartExtraclases, reporte.obtenerUsoLaboratoriosExtraclases(periodo), "#1A56E8");
        llenarGrafico(chartPrestamos, reporte.obtenerUsoLaboratoriosPrestamos(periodo), "#2EAA4A");
    }

    private void llenarGrafico(BarChart<String, Number> chart, Map<String, Integer> datos, String color) {

        chart.getData().clear();

        XYChart.Series<String, Number> serie = new XYChart.Series<>();

        for (Map.Entry<String, Integer> e : datos.entrySet()) {
            serie.getData().add(new XYChart.Data<>(e.getKey(), e.getValue()));
        }

        chart.getData().add(serie);

        // Colorea las barras según el módulo (azul = extraclases, verde = préstamos)
        for (XYChart.Data<String, Number> dato : serie.getData()) {

            if (dato.getNode() != null) {
                dato.getNode().setStyle("-fx-bar-fill:" + color + ";");
            } else {
                dato.nodeProperty().addListener((obs, viejo, nuevoNodo) -> {
                    if (nuevoNodo != null) {
                        nuevoNodo.setStyle("-fx-bar-fill:" + color + ";");
                    }
                });
            }
        }
    }
}
