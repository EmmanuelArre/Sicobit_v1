package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.LinkedHashMap;
import java.util.Map;

import utng.gtid234.jeae.conexiones.Conexion;

public class Reporte {

    private String condicionFecha(String periodo, String columna) {

        return switch (periodo) {

            case "Semana" -> "YEARWEEK(" + columna + ", 1) = YEARWEEK(CURDATE(), 1)";

            case "Mes" -> "YEAR(" + columna + ") = YEAR(CURDATE()) AND MONTH(" + columna + ") = MONTH(CURDATE())";

            case "Cuatrimestre" -> "YEAR(" + columna + ") = YEAR(CURDATE()) "
                    + "AND FLOOR((MONTH(" + columna + ") - 1) / 4) = FLOOR((MONTH(CURDATE()) - 1) / 4)";

            default -> columna + " = CURDATE()";

        };
    }

    public int obtenerTotalRegistros(String periodo) {
        return contar("SELECT COUNT(*) AS total FROM registro WHERE " + condicionFecha(periodo, "fecha"));
    }

    public int obtenerTotalIncidencias(String periodo) {
        return contar("SELECT COUNT(*) AS total FROM incidencia WHERE " + condicionFecha(periodo, "fecha"));
    }

    public int obtenerTotalPrestamos(String periodo) {
        return contar("SELECT COUNT(*) AS total FROM prestamo_equipo WHERE " + condicionFecha(periodo, "fecha"));
    }

    public int obtenerIncidenciasPendientes(String periodo) {
        return contar("SELECT COUNT(*) AS total FROM incidencia WHERE estado = 'Pendiente' AND "
                + condicionFecha(periodo, "fecha"));
    }

    public int obtenerIncidenciasResueltas(String periodo) {
        return contar("SELECT COUNT(*) AS total FROM incidencia WHERE estado = 'Resuelta' AND "
                + condicionFecha(periodo, "fecha"));
    }

    private int contar(String sql) {

        int total = 0;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                total = rs.getInt("total");
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return total;
    }

    public Map<String, Integer> obtenerUsoLaboratoriosExtraclases(String periodo) {

        String sql = """
                SELECT l.nombre AS laboratorio, COUNT(*) AS total
                FROM registro r
                INNER JOIN laboratorio l ON r.idLaboratorio = l.idLaboratorio
                WHERE %s
                GROUP BY l.nombre
                ORDER BY total DESC
                """.formatted(condicionFecha(periodo, "r.fecha"));

        return usoPorLaboratorio(sql);
    }

    public Map<String, Integer> obtenerUsoLaboratoriosPrestamos(String periodo) {

        String sql = """
                SELECT l.nombre AS laboratorio, COUNT(*) AS total
                FROM prestamo_equipo p
                INNER JOIN laboratorio l ON p.idLaboratorio = l.idLaboratorio
                WHERE %s
                GROUP BY l.nombre
                ORDER BY total DESC
                """.formatted(condicionFecha(periodo, "p.fecha"));

        return usoPorLaboratorio(sql);
    }

    private Map<String, Integer> usoPorLaboratorio(String sql) {

        Map<String, Integer> mapa = new LinkedHashMap<>();

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                mapa.put(rs.getString("laboratorio"), rs.getInt("total"));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return mapa;
    }
}
