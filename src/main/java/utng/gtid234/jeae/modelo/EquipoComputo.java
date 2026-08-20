package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Representa un equipo de cómputo físico dentro de un laboratorio
 * (ej. "PC-01" del laboratorio "Lab 1"). Se usa para el préstamo de
 * equipo, validando cuáles están disponibles según el laboratorio
 * elegido.
 */
public class EquipoComputo {

    private int idEquipo;
    private int idLaboratorio;
    private String laboratorio;
    private String clave;
    private String estatus; // Disponible / Prestado / Mantenimiento / Baja
    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    public EquipoComputo() {
    }

    public EquipoComputo(int idEquipo, String clave, String estatus) {
        this.idEquipo = idEquipo;
        this.clave = clave;
        this.estatus = estatus;
    }

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public int getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(int idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    @Override
    public String toString() {
        // Se usa directamente en el ComboBox
        return clave;
    }

    //=========================================
    // (ALUMNO) EQUIPOS DISPONIBLES DE UN LABORATORIO
    // Un equipo está disponible si su estatus es 'Disponible'
    // Y no tiene un préstamo activo (sin horaDevolucion) hoy.
    //=========================================
    public List<EquipoComputo> obtenerDisponiblesPorLaboratorio(int idLaboratorio) {

        List<EquipoComputo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT e.idEquipo, e.clave, e.estatus
                    FROM equipo_computo e
                    WHERE e.idLaboratorio = ?
                      AND e.estatus = 'Disponible'
                      AND e.idEquipo NOT IN (
                            SELECT p.idEquipo
                            FROM prestamo_equipo p
                            WHERE p.fecha = CURDATE()
                              AND p.horaDevolucion IS NULL
                      )
                    ORDER BY e.clave
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new EquipoComputo(
                        rs.getInt("idEquipo"),
                        rs.getString("clave"),
                        rs.getString("estatus")
                ));
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
    // (ALUMNO) EQUIPOS DISPONIBLES DE UN LABORATORIO PARA UN
    // DÍA/BLOQUE/CUATRIMESTRE ESPECÍFICO. Un equipo NO aparece si:
    //  - su estatus no es 'Disponible' (Prestado / Mantenimiento / Baja)
    //  - ya fue usado en un registro de extraclase ese mismo día/bloque/
    //    cuatrimestre
    //  - tiene una incidencia en estado 'Pendiente' (está en revisión)
    //=========================================
    public List<EquipoComputo> obtenerDisponiblesPorLaboratorioYHorario(
            int idLaboratorio, String dia, int idBloque, int idCuatrimestre) {

        List<EquipoComputo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT e.idEquipo, e.clave, e.estatus
                    FROM equipo_computo e
                    WHERE e.idLaboratorio = ?
                      AND e.estatus = 'Disponible'
                      AND e.idEquipo NOT IN (
                            SELECT r.idEquipo
                            FROM registro r
                            WHERE r.idLaboratorio = ?
                              AND r.dia = ?
                              AND r.idBloque = ?
                              AND r.idCuatrimestre = ?
                              AND r.tipo = 'Extraclase'
                              AND r.estado = 'Activo'
                              AND r.idEquipo IS NOT NULL
                      )
                      AND e.idEquipo NOT IN (
                            SELECT i.idEquipo
                            FROM incidencia i
                            WHERE i.estado = 'Pendiente'
                              AND i.idEquipo IS NOT NULL
                      )
                    ORDER BY e.clave
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);
            ps.setInt(2, idLaboratorio);
            ps.setString(3, dia);
            ps.setInt(4, idBloque);
            ps.setInt(5, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new EquipoComputo(
                        rs.getInt("idEquipo"),
                        rs.getString("clave"),
                        rs.getString("estatus")
                ));
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
    // (ADMIN) TODOS LOS EQUIPOS DE UN LABORATORIO, CON DETALLE
    //=========================================
    public List<EquipoComputo> obtenerPorLaboratorio(int idLaboratorio) {

        List<EquipoComputo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT idEquipo, clave, estatus
                    FROM equipo_computo
                    WHERE idLaboratorio = ?
                    ORDER BY clave
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new EquipoComputo(
                        rs.getInt("idEquipo"),
                        rs.getString("clave"),
                        rs.getString("estatus")
                ));
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
    // (ADMIN) TODOS LOS EQUIPOS, CON NOMBRE DE LABORATORIO
    //=========================================
    public List<EquipoComputo> obtenerTodos() {

        List<EquipoComputo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT e.idEquipo, e.idLaboratorio, l.nombre AS laboratorio, e.clave, e.estatus
                    FROM equipo_computo e
                    INNER JOIN laboratorio l ON e.idLaboratorio = l.idLaboratorio
                    ORDER BY l.nombre, e.clave
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                EquipoComputo eq = new EquipoComputo();
                eq.setIdEquipo(rs.getInt("idEquipo"));
                eq.setIdLaboratorio(rs.getInt("idLaboratorio"));
                eq.setLaboratorio(rs.getString("laboratorio"));
                eq.setClave(rs.getString("clave"));
                eq.setEstatus(rs.getString("estatus"));
                lista.add(eq);
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
    // (ADMIN) AGREGAR EQUIPO A UN LABORATORIO
    //=========================================
    public boolean agregar(EquipoComputo eq) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "INSERT INTO equipo_computo (idLaboratorio, clave, estatus) VALUES (?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, eq.getIdLaboratorio());
            ps.setString(2, eq.getClave());
            ps.setString(3, eq.getEstatus() == null ? "Disponible" : eq.getEstatus());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // (ADMIN) EDITAR EQUIPO (clave / estatus)
    //=========================================
    public boolean editar(EquipoComputo eq) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "UPDATE equipo_computo SET clave = ?, estatus = ? WHERE idEquipo = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, eq.getClave());
            ps.setString(2, eq.getEstatus());
            ps.setInt(3, eq.getIdEquipo());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // (ADMIN) ELIMINAR EQUIPO
    //=========================================
    //=========================================
    // (ADMIN) ELIMINAR EQUIPO (EN CASCADA)
    //=========================================
    // Los préstamos (prestamo_equipo) exigen equipo obligatorio, así que
    // se eliminan junto con el equipo. Los registros e incidencias solo
    // usan el equipo como dato opcional (idEquipo puede ser NULL), así
    // que ahí solo se desvincula el equipo (no se pierde el historial
    // del alumno). Todo dentro de una misma transacción.
    public boolean eliminar(int idEquipo) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            ejecutarUpdate(con, "UPDATE registro SET idEquipo = NULL WHERE idEquipo = ?", idEquipo);
            ejecutarUpdate(con, "UPDATE incidencia SET idEquipo = NULL WHERE idEquipo = ?", idEquipo);
            ejecutarUpdate(con, "DELETE FROM prestamo_equipo WHERE idEquipo = ?", idEquipo);

            int filas = ejecutarUpdate(con, "DELETE FROM equipo_computo WHERE idEquipo = ?", idEquipo);

            con.commit();

            respuesta = filas > 0;

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

            if (con != null) {
                try {
                    con.rollback();
                } catch (Exception rollbackEx) {
                    rollbackEx.printStackTrace();
                }
            }

        } finally {

            if (con != null) {
                try {
                    con.setAutoCommit(true);
                    con.close();
                } catch (Exception closeEx) {
                    closeEx.printStackTrace();
                }
            }

        }

        return respuesta;
    }

    private int ejecutarUpdate(Connection con, String sql, int id) throws Exception {

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        int filas = ps.executeUpdate();
        ps.close();
        return filas;

    }

    //=========================================
    // CONTAR EQUIPOS DISPONIBLES DE UN LABORATORIO (para mostrar
    // "X de Y disponibles" en la pantalla de préstamo / horarios)
    //=========================================
    public int contarDisponibles(int idLaboratorio) {
        return obtenerDisponiblesPorLaboratorio(idLaboratorio).size();
    }

    //=========================================
    // CONTAR TOTAL DE EQUIPOS DE UN LABORATORIO
    //=========================================
    public int contarTotal(int idLaboratorio) {

        int total = 0;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT COUNT(*) AS total FROM equipo_computo WHERE idLaboratorio = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);

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

    //=========================================
    // RESULTADO DE SINCRONIZAR EQUIPOS CON LA CAPACIDAD DEL LABORATORIO
    //=========================================
    public static class ResultadoSincronizacion {
        public int agregados = 0;
        public int eliminados = 0;
        public int noSePudieronEliminar = 0;
    }

    // Extrae el número de una clave tipo "PC-07" -> 7 (0 si no trae número)
    private int extraerNumero(String clave) {

        if (clave == null) {
            return 0;
        }

        String soloDigitos = clave.replaceAll("\\D+", "");

        if (soloDigitos.isEmpty()) {
            return 0;
        }

        try {
            return Integer.parseInt(soloDigitos);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    //=========================================
    // SINCRONIZAR LOS EQUIPOS DE UN LABORATORIO CON SU CAPACIDAD
    // Se llama cada vez que se crea o edita un laboratorio (desde
    // LaboratoriosController). Si la capacidad sube, se crean PC-XX
    // nuevas ("Disponible") hasta completar la capacidad. Si baja,
    // se intentan eliminar las PC con el número más alto primero; si
    // alguna ya tiene historial (registros, préstamos o incidencias),
    // no se puede borrar por la llave foránea y se deja tal cual,
    // reportándose en el resultado para avisar al administrador.
    //=========================================
    public ResultadoSincronizacion sincronizarConCapacidad(int idLaboratorio, int capacidadDeseada) {

        ResultadoSincronizacion resultado = new ResultadoSincronizacion();

        List<EquipoComputo> actuales = obtenerPorLaboratorio(idLaboratorio);
        int actualCount = actuales.size();

        if (capacidadDeseada > actualCount) {

            int maxNumero = 0;
            for (EquipoComputo e : actuales) {
                maxNumero = Math.max(maxNumero, extraerNumero(e.getClave()));
            }

            int aAgregar = capacidadDeseada - actualCount;

            for (int i = 1; i <= aAgregar; i++) {

                int numero = maxNumero + i;

                EquipoComputo nuevo = new EquipoComputo();
                nuevo.setIdLaboratorio(idLaboratorio);
                nuevo.setClave(String.format("PC-%02d", numero));
                nuevo.setEstatus("Disponible");

                if (agregar(nuevo)) {
                    resultado.agregados++;
                }
            }

        } else if (capacidadDeseada < actualCount) {

            // Se intenta quitar primero las PC con número más alto
            actuales.sort((a, b) -> extraerNumero(b.getClave()) - extraerNumero(a.getClave()));

            int aEliminar = actualCount - capacidadDeseada;

            for (EquipoComputo e : actuales) {

                if (aEliminar <= 0) {
                    break;
                }

                if (eliminar(e.getIdEquipo())) {
                    resultado.eliminados++;
                    aEliminar--;
                } else {
                    resultado.noSePudieronEliminar++;
                }
            }
        }

        return resultado;
    }
}
