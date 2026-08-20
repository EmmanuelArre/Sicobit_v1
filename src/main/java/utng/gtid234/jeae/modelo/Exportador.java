package utng.gtid234.jeae.modelo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Exportador {

    //=========================================
    // Arma la consulta según la tabla y el periodo elegidos
    //=========================================
    private String construirSql(String tabla, String periodo) {

        return switch (tabla) {

            case "registro" -> """
                    SELECT r.idRegistro AS ID, a.nombre AS Alumno, a.matricula AS Matricula,
                        l.nombre AS Laboratorio, act.nombre AS Actividad,
                        r.horaEntrada AS HoraEntrada, r.horaSalida AS HoraSalida
                    FROM registro r
                    INNER JOIN alumno a ON r.matricula = a.matricula
                    INNER JOIN laboratorio l ON r.idLaboratorio = l.idLaboratorio
                    INNER JOIN actividad act ON r.idActividad = act.idActividad
                    WHERE """ + " " + condicionFecha(periodo, "r.fecha") + " ORDER BY r.fecha DESC, r.idRegistro DESC";

            case "incidencia" -> """
                    SELECT i.idIncidencia AS ID, l.nombre AS Laboratorio, i.tipo AS Tipo,
                        i.descripcion AS Descripcion, i.estado AS Estado, i.fecha AS Fecha,
                        i.hora AS Hora, i.fechaResolucion AS FechaResolucion
                    FROM incidencia i
                    INNER JOIN laboratorio l ON i.idLaboratorio = l.idLaboratorio
                    WHERE """ + " " + condicionFecha(periodo, "i.fecha") + " ORDER BY i.fecha DESC, i.idIncidencia DESC";

            case "prestamo_equipo" -> """
                    SELECT p.idPrestamo AS ID, a.nombre AS Alumno, a.matricula AS Matricula,
                        l.nombre AS Laboratorio, e.clave AS Equipo,
                        p.fecha AS Fecha, p.horaPrestamo AS HoraPrestamo, p.horaDevolucion AS HoraDevolucion,
                        p.observaciones AS Observaciones
                    FROM prestamo_equipo p
                    INNER JOIN alumno a ON p.matricula = a.matricula
                    INNER JOIN laboratorio l ON p.idLaboratorio = l.idLaboratorio
                    INNER JOIN equipo_computo e ON p.idEquipo = e.idEquipo
                    WHERE """ + " " + condicionFecha(periodo, "p.fecha") + " ORDER BY p.fecha DESC, p.idPrestamo DESC";

            default -> throw new IllegalArgumentException("Tabla no soportada: " + tabla);
        };
    }


    private String condicionFecha(String periodo, String columnaConAlias) {

        return switch (periodo) {
            case "Semana" -> "YEARWEEK(" + columnaConAlias + ", 1) = YEARWEEK(CURDATE(), 1)";
            case "Mes" -> "YEAR(" + columnaConAlias + ") = YEAR(CURDATE()) AND MONTH(" + columnaConAlias + ") = MONTH(CURDATE())";
            case "Cuatrimestre" -> "YEAR(" + columnaConAlias + ") = YEAR(CURDATE()) "
                    + "AND FLOOR((MONTH(" + columnaConAlias + ") - 1) / 4) = FLOOR((MONTH(CURDATE()) - 1) / 4)";
            case "Todos" -> "1 = 1";
            case "Día" -> columnaConAlias + " = CURDATE()";
            default -> columnaConAlias + " = CURDATE()";
        };
    }

    public String[] obtenerEncabezados(String tabla) {

        return switch (tabla) {
            case "registro" -> new String[] {"ID", "Alumno", "Matricula", "Laboratorio", "Actividad", "Fecha", "HoraEntrada", "HoraSalida"};
            case "incidencia" -> new String[] {"ID", "Laboratorio", "Tipo", "Descripcion", "Estado", "Fecha", "Hora", "FechaResolucion"};
            case "prestamo_equipo" -> new String[] {"ID", "Alumno", "Matricula", "Laboratorio", "Equipo", "Fecha", "HoraPrestamo", "HoraDevolucion", "Observaciones"};
            default -> new String[0];
        };
    }

    private String ultimoError;

    public String obtenerUltimoError() {
        return ultimoError;
    }

    public List<String[]> obtenerFilas(String tabla, String periodo) {

        List<String[]> filas = new ArrayList<>();
        ultimoError = null;


        try (Connection con = Conexion.conectar();
            PreparedStatement ps = con.prepareStatement(construirSql(tabla, periodo));
            ResultSet rs = ps.executeQuery()) {

            ResultSetMetaData meta = rs.getMetaData();
            int columnas = meta.getColumnCount();

            while (rs.next()) {

                String[] fila = new String[columnas];

                for (int i = 1; i <= columnas; i++) {
                    Object valor = rs.getObject(i);
                    fila[i - 1] = valor == null ? "" : valor.toString();
                }

                filas.add(fila);
            }

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return filas;
    }


    public boolean exportarCSV(String[] encabezados, List<String[]> filas, File destino) {

        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {

            // BOM para que Excel detecte UTF-8 y muestre bien los acentos
            w.write('\uFEFF');

            w.write(String.join(";", escaparFila(encabezados)));
            w.write("\n");

            for (String[] fila : filas) {
                w.write(String.join(";", escaparFila(fila)));
                w.write("\n");
            }

            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        }
    }

    private String[] escaparFila(String[] fila) {

        String[] resultado = new String[fila.length];

        for (int i = 0; i < fila.length; i++) {

            String valor = fila[i] == null ? "" : fila[i];

            if (valor.contains(";") || valor.contains(",") || valor.contains("\"") || valor.contains("\n")) {
                valor = "\"" + valor.replace("\"", "\"\"") + "\"";
            }

            resultado[i] = valor;
        }

        return resultado;
    }


    public boolean exportarExcel(String[] encabezados, List<String[]> filas, File destino, String tituloHoja) {

        try (Writer w = new OutputStreamWriter(new FileOutputStream(destino), StandardCharsets.UTF_8)) {

            String fechaGeneracion = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            // Es indispensable declarar aquí encoding="UTF-8": el archivo se
            // escribe en UTF-8 (StandardCharsets.UTF_8 arriba), pero si el
            // XML no lo declara, Excel lo interpreta con el charset ANSI
            // del sistema y los acentos/ñ se ven como símbolos raros aunque
            // el archivo se genere sin ningún error.
            w.write("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
            w.write("<?mso-application progid=\"Excel.Sheet\"?>\n");
            w.write("<Workbook xmlns=\"urn:schemas-microsoft-com:office:spreadsheet\" "
                    + "xmlns:o=\"urn:schemas-microsoft-com:office:office\" "
                    + "xmlns:x=\"urn:schemas-microsoft-com:office:excel\" "
                    + "xmlns:ss=\"urn:schemas-microsoft-com:office:spreadsheet\">\n");

            w.write("<Styles>\n");

            w.write("<Style ss:ID=\"titulo\"><Font ss:Size=\"16\" ss:Bold=\"1\" ss:Color=\"#1A56E8\"/></Style>\n");

            w.write("<Style ss:ID=\"subtitulo\"><Font ss:Size=\"10\" ss:Italic=\"1\" ss:Color=\"#6C757D\"/></Style>\n");

            w.write("<Style ss:ID=\"encabezado\">"
                    + "<Font ss:Color=\"#FFFFFF\" ss:Bold=\"1\" ss:Size=\"11\"/>"
                    + "<Interior ss:Color=\"#1A56E8\" ss:Pattern=\"Solid\"/>"
                    + "<Alignment ss:Horizontal=\"Center\" ss:Vertical=\"Center\"/>"
                    + "<Borders><Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"2\" ss:Color=\"#0F3A9E\"/></Borders>"
                    + "</Style>\n");

            w.write("<Style ss:ID=\"celda\">"
                    + "<Borders>"
                    + "<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "<Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "<Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "</Borders>"
                    + "<Alignment ss:Vertical=\"Center\"/>"
                    + "</Style>\n");

            w.write("<Style ss:ID=\"celdaAlt\">"
                    + "<Interior ss:Color=\"#F3F6FD\" ss:Pattern=\"Solid\"/>"
                    + "<Borders>"
                    + "<Border ss:Position=\"Bottom\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "<Border ss:Position=\"Left\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "<Border ss:Position=\"Right\" ss:LineStyle=\"Continuous\" ss:Weight=\"1\" ss:Color=\"#D9DCE1\"/>"
                    + "</Borders>"
                    + "<Alignment ss:Vertical=\"Center\"/>"
                    + "</Style>\n");

            w.write("</Styles>\n");

            w.write("<Worksheet ss:Name=\"" + limpiarNombreHoja(tituloHoja) + "\">\n");

            w.write("<Table>\n");

            for (int i = 0; i < encabezados.length; i++) {
                w.write("<Column ss:Width=\"" + anchoColumna(i, encabezados[i], filas) + "\"/>\n");
            }


            w.write("<Row ss:Height=\"22\">\n");
            w.write("<Cell ss:StyleID=\"titulo\" ss:MergeAcross=\"" + (encabezados.length - 1)
                    + "\"><Data ss:Type=\"String\">" + escaparXml(tituloHoja) + "</Data></Cell>\n");
            w.write("</Row>\n");

            // Fila con la fecha de generación
            w.write("<Row>\n");
            w.write("<Cell ss:StyleID=\"subtitulo\" ss:MergeAcross=\"" + (encabezados.length - 1)
                    + "\"><Data ss:Type=\"String\">Generado el " + fechaGeneracion
                    + "  —  " + filas.size() + " registro(s)</Data></Cell>\n");
            w.write("</Row>\n");

            // Fila vacía de separación
            w.write("<Row></Row>\n");

            // Encabezados de columna
            w.write("<Row ss:Height=\"20\">\n");
            for (String enc : encabezados) {
                w.write("<Cell ss:StyleID=\"encabezado\"><Data ss:Type=\"String\">" + escaparXml(enc) + "</Data></Cell>\n");
            }
            w.write("</Row>\n");

            int numFila = 0;

            for (String[] fila : filas) {

                String estilo = (numFila % 2 == 0) ? "celda" : "celdaAlt";

                w.write("<Row>\n");

                for (String valor : fila) {
                    w.write("<Cell ss:StyleID=\"" + estilo + "\"><Data ss:Type=\"String\">" + escaparXml(valor) + "</Data></Cell>\n");
                }

                w.write("</Row>\n");
                numFila++;
            }

            w.write("</Table>\n");

            // Encabezado de columnas congelado (fila de títulos, la 4ta fila)
            w.write("<WorksheetOptions xmlns=\"urn:schemas-microsoft-com:office:excel\">\n");
            w.write("<FreezePanes/><FrozenNoSplit/><SplitHorizontal>4</SplitHorizontal><TopRowBottomPane>4</TopRowBottomPane>\n");
            w.write("<ActivePane>2</ActivePane>\n");
            w.write("</WorksheetOptions>\n");

            w.write("</Worksheet>\n");
            w.write("</Workbook>\n");

            return true;

        } catch (IOException e) {

            e.printStackTrace();
            return false;

        }
    }

    private int anchoColumna(int indiceColumna, String encabezado, List<String[]> filas) {

        int maxLargo = encabezado.length();

        int muestra = Math.min(filas.size(), 300);

        for (int f = 0; f < muestra; f++) {

            String[] fila = filas.get(f);

            if (indiceColumna < fila.length && fila[indiceColumna] != null) {
                maxLargo = Math.max(maxLargo, fila[indiceColumna].length());
            }
        }

        String enc = encabezado.toLowerCase();
        boolean textoLibre = enc.contains("alumno") || enc.contains("nombre")
                || enc.contains("descripcion") || enc.contains("observaciones")
                || enc.contains("laboratorio") || enc.contains("equipo");

        int tope = textoLibre ? 420 : 220;

        int ancho = Math.max(maxLargo * 7 + 24, 60);

        return Math.min(ancho, tope);
    }

    private String limpiarNombreHoja(String nombre) {

        // Excel no permite ciertos caracteres ni más de 31 caracteres en el nombre de hoja
        String limpio = nombre.replaceAll("[\\[\\]:*?/\\\\]", "-");
        return limpio.length() > 31 ? limpio.substring(0, 31) : limpio;
    }

    private String escaparXml(String texto) {

        if (texto == null) {
            return "";
        }

        return texto
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;");
    }

    public boolean exportarPDF(String titulo, String[] encabezados, List<String[]> filas, File destino) {

        try {

            String fechaGeneracion = java.time.LocalDateTime.now()
                    .format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));

            String subtitulo = "Generado el " + fechaGeneracion + "  —  " + filas.size() + " registro(s)";

            PdfSimple.generarTabla(destino, "SICOBIT — " + titulo, subtitulo, encabezados, filas);
            return true;

        } catch (Exception e) {

            e.printStackTrace();
            return false;

        }
    }
}
