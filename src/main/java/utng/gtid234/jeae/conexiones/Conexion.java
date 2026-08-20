package utng.gtid234.jeae.conexiones;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexion {

    private static final String URL = System.getenv().getOrDefault("DB_URL", "jdbc:mysql://localhost:3306/sicobit");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "root");
    private static final String PASSWORD = System.getenv().getOrDefault("DB_PASS", "Emma2569@");

    // Getters usados por otras partes del sistema (p. ej. Respaldo) que
    // necesitan host/base de datos/usuario/contraseña reales en vez de
    // repetir valores fijos que podrían no coincidir con las variables
    // de entorno DB_URL/DB_USER/DB_PASS configuradas en cada equipo.
    public static String getUsuario() {
        return USUARIO;
    }

    public static String getPassword() {
        return PASSWORD;
    }

    // Extrae el host de la URL JDBC, p. ej. de
    // "jdbc:mysql://localhost:3306/sicobit" regresa "localhost".
    public static String getHost() {

        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("//([^:/]+)").matcher(URL);

        return m.find() ? m.group(1) : "localhost";
    }

    // Extrae el nombre de la base de datos de la URL JDBC, p. ej. de
    // "jdbc:mysql://localhost:3306/sicobit" regresa "sicobit".
    public static String getBaseDatos() {

        java.util.regex.Matcher m =
                java.util.regex.Pattern.compile("/([A-Za-z0-9_]+)(\\?.*)?$").matcher(URL);

        return m.find() ? m.group(1) : "sicobit";
    }

    public static Connection conectar() throws SQLException {

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("No se encontró el driver de MySQL (mysql-connector-j) en el proyecto.", e);
        }

        try {
            return DriverManager.getConnection(URL, USUARIO, PASSWORD);
        } catch (SQLException e) {
            throw new SQLException("No se pudo conectar a la base de datos ('" + URL
                    + "', usuario '" + USUARIO + "'). Verifica que MySQL esté encendido, que la "
                    + "base de datos 'sicobit' exista (importa sicobit.sql) y que el usuario y "
                    + "la contraseña sean correctos (puedes ajustarlos con las variables de "
                    + "entorno DB_URL, DB_USER y DB_PASS). Detalle: " + e.getMessage(), e);
        }
    }

}