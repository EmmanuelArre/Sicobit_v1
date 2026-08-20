package utng.gtid234.jeae.modelo;

import java.io.File;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;


public class Respaldo {

    private int idRespaldo;
    private String nombreArchivo;
    private String rutaCompleta;
    private LocalDate fecha;
    private LocalTime hora;
    private long tamanoBytes;
    private String estado;
    private String tipo; // 'Respaldo' | 'Importacion'
    private String detalleError; // mensaje de mysqldump/mysql cuando falla (no se guarda en BD)

    public String getDetalleError() {
        return detalleError;
    }

    public void setDetalleError(String detalleError) {
        this.detalleError = detalleError;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public int getIdRespaldo() {
        return idRespaldo;
    }

    public void setIdRespaldo(int idRespaldo) {
        this.idRespaldo = idRespaldo;
    }

    public String getNombreArchivo() {
        return nombreArchivo;
    }

    public void setNombreArchivo(String nombreArchivo) {
        this.nombreArchivo = nombreArchivo;
    }

    public String getRutaCompleta() {
        return rutaCompleta;
    }

    public void setRutaCompleta(String rutaCompleta) {
        this.rutaCompleta = rutaCompleta;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    public long getTamanoBytes() {
        return tamanoBytes;
    }

    public void setTamanoBytes(long tamanoBytes) {
        this.tamanoBytes = tamanoBytes;
    }

    public String getTamanoLegible() {

        if (tamanoBytes < 1024) {
            return tamanoBytes + " B";
        }

        double kb = tamanoBytes / 1024.0;

        if (kb < 1024) {
            return String.format("%.1f KB", kb);
        }

        return String.format("%.1f MB", kb / 1024.0);
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    private static String resolverRutaMysqldump() {
        return resolverEjecutable("mysqldump", "mysqldump.exe");
    }

    private static String resolverRutaMysql() {
        return resolverEjecutable("mysql", "mysql.exe");
    }

    // Busca el ejecutable en rutas típicas de Windows, macOS y Linux, y si
    // no lo encuentra ahí, intenta resolverlo desde el PATH del sistema
    // (con "where" en Windows o "which" en macOS/Linux). Si nada de eso
    // funciona, regresa solo el nombre y se deja que el sistema operativo
    // lo busque al ejecutar el proceso.
    private static String resolverEjecutable(String nombreUnix, String nombreWindows) {

        boolean esWindows = System.getProperty("os.name", "").toLowerCase().contains("win");

        List<String> rutasComunes = new ArrayList<>();

        if (esWindows) {
            // XAMPP, WAMP y las rutas típicas del instalador oficial de MySQL en Windows
            String[] basesWindows = {
                    "C:\\xampp\\mysql\\bin\\",
                    "C:\\wamp64\\bin\\mysql\\mysql8.0.40\\bin\\",
                    "C:\\Program Files\\MySQL\\MySQL Server 8.0\\bin\\",
                    "C:\\Program Files\\MySQL\\MySQL Server 8.4\\bin\\",
                    "C:\\Program Files\\MySQL\\MySQL Server 9.0\\bin\\",
                    "C:\\Program Files (x86)\\MySQL\\MySQL Server 8.0\\bin\\"
            };
            for (String base : basesWindows) {
                rutasComunes.add(base + nombreWindows);
            }
        } else {
            rutasComunes.add("/opt/homebrew/bin/" + nombreUnix);
            rutasComunes.add("/usr/local/bin/" + nombreUnix);
            rutasComunes.add("/usr/local/mysql/bin/" + nombreUnix);
            rutasComunes.add("/usr/bin/" + nombreUnix);
        }

        for (String ruta : rutasComunes) {
            if (new File(ruta).exists()) {
                return ruta;
            }
        }

        // No está en ninguna ruta típica: pregúntale al sistema operativo si
        // lo tiene registrado en el PATH (esto es lo que casi siempre falla
        // en Windows cuando el instalador no agregó la carpeta bin al PATH).
        String desdeSistema = buscarEnPath(esWindows ? "where" : "which", nombreUnix);
        if (desdeSistema != null) {
            return desdeSistema;
        }

        return nombreUnix;
    }

    private static String buscarEnPath(String comandoBusqueda, String nombreEjecutable) {

        try {

            Process proceso = new ProcessBuilder(comandoBusqueda, nombreEjecutable)
                    .redirectErrorStream(true)
                    .start();

            String salida;
            try (java.io.InputStream is = proceso.getInputStream()) {
                salida = new String(is.readAllBytes()).trim();
            }

            int codigo = proceso.waitFor();

            if (codigo == 0 && !salida.isEmpty()) {
                // "where" puede devolver varias líneas; toma la primera
                return salida.split("\\R")[0].trim();
            }

        } catch (Exception e) {
            // Sin "where"/"which" disponible, o sin resultado: se ignora y
            // se sigue con el siguiente método de búsqueda.
        }

        return null;
    }

    public Respaldo generar(String carpetaDestino, String host, String usuario, String password, String baseDatos) {

        Respaldo resultado = new Respaldo();

        String marcaTiempo = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String nombreArchivo = "respaldo_" + baseDatos + "_" + marcaTiempo + ".sql";

        File carpeta = new File(carpetaDestino);

        if (!carpeta.exists()) {
            carpeta.mkdirs();
        }

        File destino = new File(carpeta, nombreArchivo);

        resultado.setNombreArchivo(nombreArchivo);
        resultado.setRutaCompleta(destino.getAbsolutePath());
        resultado.setFecha(LocalDate.now());
        resultado.setHora(LocalTime.now().withNano(0));
        resultado.setTipo("Respaldo");

        try {

            String rutaMysqldump = resolverRutaMysqldump();

            ProcessBuilder pb = new ProcessBuilder(
                    rutaMysqldump,
                    "-h", host,
                    "-u", usuario,
                    "-p" + password,
                    baseDatos
            );

            pb.redirectOutput(destino);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            Process proceso = pb.start();

            String errorSalida;
            try (java.io.InputStream is = proceso.getErrorStream()) {
                errorSalida = new String(is.readAllBytes()).trim();
            }

            int codigo = proceso.waitFor();

            if (codigo == 0 && destino.exists() && destino.length() > 0) {

                resultado.setTamanoBytes(destino.length());
                resultado.setEstado("Exitoso");

            } else {

                resultado.setTamanoBytes(destino.exists() ? destino.length() : 0);
                resultado.setEstado("Fallido");
                resultado.setDetalleError(!errorSalida.isEmpty() ? errorSalida
                        : "No se encontró 'mysqldump' (se buscó como: " + rutaMysqldump + ")");

            }

        } catch (Exception e) {

            e.printStackTrace();
            resultado.setEstado("Fallido");
            resultado.setTamanoBytes(0);
            resultado.setDetalleError(e.getMessage());

        }

        registrarEnHistorial(resultado);

        return resultado;
    }

    //=========================================
    // IMPORTAR (RESTAURAR) un archivo .sql de respaldo hacia la base
    // de datos actual, ejecutando el cliente `mysql` con el archivo
    // como entrada estándar.
    //=========================================
    public Respaldo importar(File archivoSql, String host, String usuario, String password, String baseDatos) {

        Respaldo resultado = new Respaldo();

        resultado.setNombreArchivo(archivoSql.getName());
        resultado.setRutaCompleta(archivoSql.getAbsolutePath());
        resultado.setFecha(LocalDate.now());
        resultado.setHora(LocalTime.now().withNano(0));
        resultado.setTamanoBytes(archivoSql.exists() ? archivoSql.length() : 0);
        resultado.setTipo("Importacion");

        if (!archivoSql.exists() || !archivoSql.isFile()) {
            resultado.setEstado("Fallido");
            registrarEnHistorial(resultado);
            return resultado;
        }

        try {

            String rutaMysql = resolverRutaMysql();

            ProcessBuilder pb = new ProcessBuilder(
                    rutaMysql,
                    "-h", host,
                    "-u", usuario,
                    "-p" + password,
                    baseDatos
            );

            pb.redirectInput(archivoSql);
            pb.redirectError(ProcessBuilder.Redirect.PIPE);

            Process proceso = pb.start();

            String errorSalida;
            try (java.io.InputStream is = proceso.getErrorStream()) {
                errorSalida = new String(is.readAllBytes()).trim();
            }

            int codigo = proceso.waitFor();

            resultado.setEstado(codigo == 0 ? "Exitoso" : "Fallido");

            if (codigo != 0) {
                resultado.setDetalleError(!errorSalida.isEmpty() ? errorSalida
                        : "No se encontró 'mysql' (se buscó como: " + rutaMysql + ")");
            }

        } catch (Exception e) {

            e.printStackTrace();
            resultado.setEstado("Fallido");
            resultado.setDetalleError(e.getMessage());

        }

        registrarEnHistorial(resultado);

        return resultado;
    }

 
    private void registrarEnHistorial(Respaldo r) {

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    INSERT INTO respaldo (nombreArchivo, rutaCompleta, fecha, hora, tamanoBytes, estado, tipo)
                    VALUES (?, ?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, r.getNombreArchivo());
            ps.setString(2, r.getRutaCompleta());
            ps.setDate(3, Date.valueOf(r.getFecha()));
            ps.setTime(4, Time.valueOf(r.getHora()));
            ps.setLong(5, r.getTamanoBytes());
            ps.setString(6, r.getEstado());
            ps.setString(7, r.getTipo() == null ? "Respaldo" : r.getTipo());

            ps.executeUpdate();

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }
    }


    public List<Respaldo> obtenerHistorial() {

        List<Respaldo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT * FROM respaldo ORDER BY fecha DESC, hora DESC";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Respaldo r = new Respaldo();
                r.setIdRespaldo(rs.getInt("idRespaldo"));
                r.setNombreArchivo(rs.getString("nombreArchivo"));
                r.setRutaCompleta(rs.getString("rutaCompleta"));

                Date fecha = rs.getDate("fecha");
                if (fecha != null) {
                    r.setFecha(fecha.toLocalDate());
                }

                Time hora = rs.getTime("hora");
                if (hora != null) {
                    r.setHora(hora.toLocalTime());
                }

                r.setTamanoBytes(rs.getLong("tamanoBytes"));
                r.setEstado(rs.getString("estado"));
                r.setTipo(rs.getString("tipo"));

                lista.add(r);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }
}
