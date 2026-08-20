package utng.gtid234.jeae;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.stage.Stage;

import utng.gtid234.jeae.modelo.BloqueHorario;
import utng.gtid234.jeae.modelo.Cuatrimestre;
import utng.gtid234.jeae.modelo.Horario;
import utng.gtid234.jeae.modelo.Laboratorio;
import utng.gtid234.jeae.modelo.RegistroHorario;

/**
 * Vista de horario del ALUMNO. Siempre muestra el cuatrimestre marcado
 * como activo (el alumno no puede ver cuatrimestres pasados; eso solo
 * lo puede hacer el administrador desde "Horarios regulares").
 * Las celdas de clase regular se marcan en verde; las que no tienen
 * clase muestran cuántos equipos de cómputo quedan disponibles para
 * registrar extraclase en ese laboratorio/bloque.
 */
public class HorarioController implements Initializable {

    @FXML
    private Button btnRegresar;

    @FXML
    private ComboBox<String> cmbDia;
    @FXML
    private ScrollPane scrollHorario;

    @FXML
    private GridPane gridHorario;

    private List<Laboratorio> laboratorios;
    private int idCuatrimestreActivo = -1;

    // Los bloques de horario ya no están fijos en el código: se toman de la
    // tabla bloque_horario, para que sean editables desde el administrador
    // (pantalla "Bloques de horario") y este calendario los refleje solo.
    private List<BloqueHorario> bloques;

    @FXML
    private void cambiarDia() {
        cargarHorario(cmbDia.getValue());
    }

    @FXML
    private void regresarFormulario(ActionEvent event) {

        try {

            Parent root = FXMLLoader.load(
                    getClass().getResource("formularioAlumno.fxml"));

            Stage stage = (Stage) ((Node) event.getSource())
                    .getScene()
                    .getWindow();

            stage.getScene().setRoot(root);
            stage.setTitle("Registro de Laboratorios");

        } catch (Exception e) {

            e.printStackTrace();

        }

    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {

        cmbDia.getItems().addAll(
                "Lunes",
                "Martes",
                "Miércoles",
                "Jueves",
                "Viernes",
                "Sábado"
        );

        cmbDia.getSelectionModel().selectFirst();

        laboratorios = new Laboratorio().obtenerListaCompleta();
        bloques = new BloqueHorario().obtenerTodos();

        Cuatrimestre activo = new Cuatrimestre().obtenerActivo();
        idCuatrimestreActivo = (activo != null) ? activo.getIdCuatrimestre() : -1;

        cargarHorario(cmbDia.getValue());
    }

    private void crearHorario() {

        gridHorario.getChildren().clear();
        gridHorario.getColumnConstraints().clear();
        gridHorario.getRowConstraints().clear();

        int totalLabs = laboratorios.size();

        // Anchos fijos (no por porcentaje) para que, si hay muchos
        // laboratorios, la tabla crezca más allá del ancho visible y el
        // ScrollPane que la contiene muestre una barra horizontal en vez
        // de apachurrar las columnas.
        ColumnConstraints hora = new ColumnConstraints();
        hora.setMinWidth(130);
        hora.setPrefWidth(130);
        hora.setHgrow(Priority.NEVER);
        gridHorario.getColumnConstraints().add(hora);

        for (int i = 0; i < totalLabs; i++) {

            ColumnConstraints cc = new ColumnConstraints();

            cc.setMinWidth(190);
            cc.setPrefWidth(190);
            cc.setHgrow(Priority.NEVER);
            cc.setFillWidth(true);

            gridHorario.getColumnConstraints().add(cc);
        }

        // Filas más altas y letra más grande para que la tabla no se vea
        // amontonada, con scroll vertical si no caben todos los bloques.
        for (int i = 0; i <= bloques.size(); i++) {

            RowConstraints rc = new RowConstraints();

            rc.setPrefHeight(72);
            rc.setMinHeight(72);
            rc.setVgrow(Priority.NEVER);

            gridHorario.getRowConstraints().add(rc);
        }

        Label tituloHora = crearLabel("HORA");
        gridHorario.add(tituloHora, 0, 0);

        for (int i = 0; i < totalLabs; i++) {

            gridHorario.add(
                    crearLabel(laboratorios.get(i).getNombre()),
                    i + 1,
                    0
            );

        }

        // Celdas por defecto: "Disponible" mientras no se sepa el detalle
        for (int fila = 0; fila < bloques.size(); fila++) {

            Label horaLabel = crearLabel(bloques.get(fila).toString());
            gridHorario.add(horaLabel, 0, fila + 1);

            for (int columna = 1; columna <= totalLabs; columna++) {

                Label espacio = new Label("Disponible");

                espacio.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

                espacio.setStyle(
                        "-fx-background-color:#F3F7FF;" +
                        "-fx-text-fill:#93A3C4;" +
                        "-fx-font-size:13;" +
                        "-fx-font-style:italic;" +
                        "-fx-border-color:#DCE6FA;" +
                        "-fx-alignment:center;"
                );

                gridHorario.add(espacio, columna, fila + 1);
            }
        }
    }


    private void cargarHorario(String dia) {

        if (laboratorios == null || laboratorios.isEmpty()) {
            laboratorios = new Laboratorio().obtenerListaCompleta();
        }

        // Se vuelve a leer de la base de datos por si el administrador
        // agregó, editó o quitó algún bloque desde "Bloques de horario".
        bloques = new BloqueHorario().obtenerTodos();

        gridHorario.getChildren().clear();

        crearHorario();

        if (idCuatrimestreActivo == -1) {
            return; // no hay cuatrimestre activo configurado, nada que mostrar
        }

        RegistroHorario registro = new RegistroHorario();

        // Clases regulares del día (verde)
        List<Horario> listaClases = registro.obtenerHorario(dia, idCuatrimestreActivo);

        for (Horario h : listaClases) {
            marcarClase(h);
        }

        // Disponibilidad de equipos por laboratorio/bloque (reemplaza la marca azul)
        List<Horario> disponibilidad = registro.obtenerDisponibilidad(dia, idCuatrimestreActivo);

        for (Horario h : disponibilidad) {
            if (!"CLASE".equals(h.getTipo())) {
                marcarDisponibilidad(h);
            }
        }

    }

    private void marcarClase(Horario h) {

        int columna = obtenerColumnaLaboratorio(h.getIdLaboratorio());
        int fila = obtenerFilaBloque(h.getIdBloque());

        if (columna == -1 || fila == -1) {
            return;
        }

        Label celda = new Label();

        celda.setText(h.getMateria() + "\n" + h.getGrupo());

        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        celda.setStyle(
                "-fx-background-color:#2EAA4A;" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:13;" +
                "-fx-alignment:center;" +
                "-fx-background-radius:4;" +
                "-fx-border-color:white;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:4;" +
                "-fx-effect:dropshadow(gaussian, rgba(0,0,0,0.25), 4, 0, 0, 1);"
        );

        gridHorario.add(celda, columna, fila);
    }

    // Muestra "X/Y equipos" en vez de una marca azul de "extraclase".
    // Verde-azulado si hay equipos libres, rojo si ya no hay ninguno.
    private void marcarDisponibilidad(Horario h) {

        int columna = obtenerColumnaLaboratorio(h.getIdLaboratorio());
        int fila = obtenerFilaBloque(h.getIdBloque());

        if (columna == -1 || fila == -1) {
            return;
        }

        int disponibles = h.getDisponibles();
        int total = h.getTotalEquipos();

        if (total == 0) {
            return; // ese laboratorio no tiene equipos cargados, deja la celda "Disponible" genérica
        }

        Label celda = new Label(disponibles + "/" + total + " equipos");

        celda.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        String color = disponibles == 0 ? "#D93025" : "#1A73E8";

        celda.setStyle(
                "-fx-background-color:" + color + ";" +
                "-fx-text-fill:white;" +
                "-fx-font-weight:bold;" +
                "-fx-font-size:13;" +
                "-fx-alignment:center;" +
                "-fx-background-radius:4;" +
                "-fx-border-color:white;" +
                "-fx-border-width:2;" +
                "-fx-border-radius:4;"
        );

        gridHorario.add(celda, columna, fila);
    }

    private int obtenerColumnaLaboratorio(int idLaboratorio) {

        for (int i = 0; i < laboratorios.size(); i++) {

            if (laboratorios.get(i).getIdLaboratorio() == idLaboratorio) {

                return i + 1;

            }
        }

        return -1;
    }

    // Ubica la fila del bloque por idBloque (ya no se parsea texto de hora:
    // los bloques vienen de la tabla bloque_horario y pueden haber sido
    // editados/agregados desde el administrador).
    private int obtenerFilaBloque(int idBloque) {

        for (int i = 0; i < bloques.size(); i++) {

            if (bloques.get(i).getIdBloque() == idBloque) {

                return i + 1;

            }
        }

        return -1;
    }

    private Label crearLabel(String texto) {

        Label label = new Label(texto);

        label.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        label.setStyle(
                "-fx-font-size:15;" +
                "-fx-font-weight:bold;" +
                "-fx-text-fill:white;" +
                "-fx-alignment:center;" +
                "-fx-background-color:linear-gradient(to bottom, #1A56E8, #123FAE);" +
                "-fx-border-color:white;" +
                "-fx-border-width:0 1 1 0;"
        );

        label.setWrapText(true);

        return label;
    }
}
