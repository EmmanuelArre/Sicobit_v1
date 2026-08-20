package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class RegistroHorario {

    //=========================================
    // CLASES REGULARES DE UN DÍA, DENTRO DE UN CUATRIMESTRE
    //=========================================
    public List<Horario> obtenerHorario(String dia, int idCuatrimestre) {

        List<Horario> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT
                        hc.idHorario,
                        l.idLaboratorio,
                        l.nombre AS laboratorio,
                        hc.dia,
                        bh.idBloque,
                        bh.horaInicio,
                        bh.horaFin,
                        m.nombre AS materia,
                        p.nombre AS profesor
                    FROM horario_clase hc
                    INNER JOIN laboratorio l ON hc.idLaboratorio = l.idLaboratorio
                    INNER JOIN bloque_horario bh ON hc.idBloque = bh.idBloque
                    INNER JOIN materia m ON hc.idMateria = m.idMateria
                    INNER JOIN profesor p ON hc.idProfesor = p.idProfesor
                    WHERE hc.dia = ? AND hc.idCuatrimestre = ?
                    ORDER BY l.idLaboratorio, bh.idBloque
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, dia);
            ps.setInt(2, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Horario h = new Horario();
                h.setIdHorario(rs.getInt("idHorario"));
                h.setIdLaboratorio(rs.getInt("idLaboratorio"));
                h.setLaboratorio(rs.getString("laboratorio"));
                h.setDia(rs.getString("dia"));
                h.setIdBloque(rs.getInt("idBloque"));
                h.setHoraInicio(rs.getString("horaInicio"));
                h.setHoraFin(rs.getString("horaFin"));
                h.setMateria(rs.getString("materia"));
                h.setGrupo(rs.getString("profesor"));
                h.setTipo("CLASE");

                lista.add(h);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;

    }

    //=========================================
    // DISPONIBILIDAD DE EQUIPOS POR LABORATORIO/BLOQUE/DIA, DENTRO DE UN
    // CUATRIMESTRE. Sustituye la marca azul de "extraclase": ahora se
    // muestra cuántos equipos quedan libres en ese laboratorio/bloque
    // (equipos totales del laboratorio - registros extraclase ya hechos
    // ese día/bloque - si hay clase regular, no hay extraclase posible).
    //=========================================
    public List<Horario> obtenerDisponibilidad(String dia, int idCuatrimestre) {

        List<Horario> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT
                        l.idLaboratorio,
                        l.nombre AS laboratorio,
                        bh.idBloque,
                        bh.horaInicio,
                        bh.horaFin,
                        (SELECT COUNT(*) FROM equipo_computo e
                          WHERE e.idLaboratorio = l.idLaboratorio
                            AND e.estatus = 'Disponible'
                            AND e.idEquipo NOT IN (
                                  SELECT i.idEquipo FROM incidencia i
                                  WHERE i.estado = 'Pendiente' AND i.idEquipo IS NOT NULL
                            )) AS totalEquipos,
                        (SELECT COUNT(*) FROM registro r
                          WHERE r.idLaboratorio = l.idLaboratorio
                            AND r.idBloque = bh.idBloque
                            AND r.dia = ?
                            AND r.idCuatrimestre = ?
                            AND r.tipo = 'Extraclase'
                            AND r.estado = 'Activo') AS ocupados,
                        (SELECT COUNT(*) FROM horario_clase hc
                          WHERE hc.idLaboratorio = l.idLaboratorio
                            AND hc.idBloque = bh.idBloque
                            AND hc.dia = ?
                            AND hc.idCuatrimestre = ?) AS tieneClaseRegular
                    FROM laboratorio l
                    CROSS JOIN bloque_horario bh
                    ORDER BY l.idLaboratorio, bh.idBloque
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, dia);
            ps.setInt(2, idCuatrimestre);
            ps.setString(3, dia);
            ps.setInt(4, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Horario h = new Horario();
                h.setIdLaboratorio(rs.getInt("idLaboratorio"));
                h.setLaboratorio(rs.getString("laboratorio"));
                h.setDia(dia);
                h.setIdBloque(rs.getInt("idBloque"));
                h.setHoraInicio(rs.getString("horaInicio"));
                h.setHoraFin(rs.getString("horaFin"));

                int total = rs.getInt("totalEquipos");
                int ocupados = rs.getInt("ocupados");
                boolean claseRegular = rs.getInt("tieneClaseRegular") > 0;

                int disponibles = claseRegular ? 0 : Math.max(total - ocupados, 0);

                h.setTotalEquipos(total);
                h.setDisponibles(disponibles);
                h.setTipo(claseRegular ? "CLASE" : "EXTRACLASE");

                lista.add(h);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;

    }

    //=========================================
    // VALIDAR SI UN BLOQUE YA ESTÁ OCUPADO PARA EXTRACLASE
    // (por una clase regular, o porque ya no hay equipos libres)
    //=========================================
    public String verificarOcupado(int idLaboratorio, String dia, int idBloque, int idCuatrimestre) {

        try {

            Connection con = Conexion.conectar();

            // 1) ¿Hay una clase regular en ese laboratorio/día/bloque/cuatrimestre?
            String sqlClase = """
                    SELECT m.nombre AS materia FROM horario_clase hc
                    INNER JOIN materia m ON hc.idMateria = m.idMateria
                    WHERE hc.idLaboratorio = ? AND hc.dia = ? AND hc.idBloque = ? AND hc.idCuatrimestre = ?
                    """;

            PreparedStatement psClase = con.prepareStatement(sqlClase);
            psClase.setInt(1, idLaboratorio);
            psClase.setString(2, dia);
            psClase.setInt(3, idBloque);
            psClase.setInt(4, idCuatrimestre);

            ResultSet rsClase = psClase.executeQuery();

            if (rsClase.next()) {
                String materia = rsClase.getString("materia");
                rsClase.close();
                psClase.close();
                con.close();
                return "Ese horario ya está ocupado por la clase: " + materia;
            }

            rsClase.close();
            psClase.close();

            // 2) ¿Ya no hay equipos disponibles en ese laboratorio/bloque?
            String sqlEquipos = """
                    SELECT COUNT(*) AS total FROM equipo_computo e
                    WHERE e.idLaboratorio = ?
                      AND e.estatus = 'Disponible'
                      AND e.idEquipo NOT IN (
                            SELECT i.idEquipo FROM incidencia i
                            WHERE i.estado = 'Pendiente' AND i.idEquipo IS NOT NULL
                      )
                    """;
            PreparedStatement psEq = con.prepareStatement(sqlEquipos);
            psEq.setInt(1, idLaboratorio);
            ResultSet rsEq = psEq.executeQuery();
            int total = rsEq.next() ? rsEq.getInt("total") : 0;
            rsEq.close();
            psEq.close();

            String sqlOcupados = """
                    SELECT COUNT(*) AS ocupados FROM registro
                    WHERE idLaboratorio = ? AND dia = ? AND idBloque = ? AND idCuatrimestre = ?
                      AND tipo = 'Extraclase' AND estado = 'Activo'
                    """;
            PreparedStatement psOc = con.prepareStatement(sqlOcupados);
            psOc.setInt(1, idLaboratorio);
            psOc.setString(2, dia);
            psOc.setInt(3, idBloque);
            psOc.setInt(4, idCuatrimestre);
            ResultSet rsOc = psOc.executeQuery();
            int ocupados = rsOc.next() ? rsOc.getInt("ocupados") : 0;
            rsOc.close();
            psOc.close();

            con.close();

            if (ocupados >= total) {
                return "Ya no hay equipos disponibles en ese laboratorio para ese horario (" + ocupados + "/" + total + ").";
            }

        } catch (Exception e) {

            e.printStackTrace();
            return "No se pudo validar la disponibilidad del horario.";

        }

        return null; // null = disponible
    }

    //=========================================
    // ¿ESE LABORATORIO/DÍA/BLOQUE YA TIENE UNA CLASE REGULAR?
    //=========================================
    public boolean tieneClaseRegular(int idLaboratorio, String dia, int idBloque, int idCuatrimestre) {

        boolean ocupado = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT COUNT(*) AS total FROM horario_clase
                    WHERE idLaboratorio = ? AND dia = ? AND idBloque = ? AND idCuatrimestre = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);
            ps.setString(2, dia);
            ps.setInt(3, idBloque);
            ps.setInt(4, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                ocupado = rs.getInt("total") > 0;
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return ocupado;
    }

    //=========================================
    // BLOQUES DISPONIBLES PARA EXTRACLASE EN UN LABORATORIO/DÍA:
    // excluye los bloques que ya tienen una clase regular asignada,
    // para que ni siquiera aparezcan como opción en el combobox.
    //=========================================
    public List<BloqueHorario> obtenerBloquesDisponibles(int idLaboratorio, String dia, int idCuatrimestre) {

        List<BloqueHorario> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT bh.idBloque, bh.horaInicio, bh.horaFin
                    FROM bloque_horario bh
                    WHERE NOT EXISTS (
                        SELECT 1 FROM horario_clase hc
                        WHERE hc.idBloque = bh.idBloque
                          AND hc.idLaboratorio = ?
                          AND hc.dia = ?
                          AND hc.idCuatrimestre = ?
                    )
                    ORDER BY bh.idBloque
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);
            ps.setString(2, dia);
            ps.setInt(3, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                BloqueHorario b = new BloqueHorario();
                b.setIdBloque(rs.getInt("idBloque"));
                b.setHoraInicio(rs.getString("horaInicio"));
                b.setHoraFin(rs.getString("horaFin"));
                lista.add(b);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    public List<BloqueHorario> obtenerBloques() {

        List<BloqueHorario> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idBloque, horaInicio, horaFin FROM bloque_horario ORDER BY idBloque";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                BloqueHorario b = new BloqueHorario();
                b.setIdBloque(rs.getInt("idBloque"));
                b.setHoraInicio(rs.getString("horaInicio"));
                b.setHoraFin(rs.getString("horaFin"));
                lista.add(b);

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
