package utng.gtid234.jeae;

/**
 * Clase lanzadora separada. No debe extender javafx.application.Application,
 * para que el jar empacado (jpackage / jar con dependencias) detecte
 * correctamente el método main sin ambigüedad con el ciclo de vida de JavaFX.
 */
public class Launcher {
    public static void main(String[] args) {
        App.main(args);
    }
}
