package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Periodo cuatrimestral: Enero-Abril, Mayo-Agosto o Septiembre-Diciembre
 * de un año determinado. Solo uno debe estar marcado como "activo": ese
 * es el que ve el alumno. El administrador puede ver y filtrar por
 * cualquier cuatrimestre existente.
 */
public class Cuatrimestre {

    public static final String[] PERIODOS = {
        "Enero-Abril", "Mayo-Agosto", "Septiembre-Diciembre"
    };

    private int idCuatrimestre;
    private String periodo;
    private int anio;
    private boolean activo;
    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    public Cuatrimestre() {
    }

    public Cuatrimestre(int idCuatrimestre, String periodo, int anio, boolean activo) {
        this.idCuatrimestre = idCuatrimestre;
        this.periodo = periodo;
        this.anio = anio;
        this.activo = activo;
    }

    public int getIdCuatrimestre() {
        return idCuatrimestre;
    }

    public void setIdCuatrimestre(int idCuatrimestre) {
        this.idCuatrimestre = idCuatrimestre;
    }

    public String getPeriodo() {
        return periodo;
    }

    public void setPeriodo(String periodo) {
        this.periodo = periodo;
    }

    public int getAnio() {
        return anio;
    }

    public void setAnio(int anio) {
        this.anio = anio;
    }

    public boolean isActivo() {
        return activo;
    }

    public void setActivo(boolean activo) {
        this.activo = activo;
    }

    @Override
    public String toString() {
        return periodo + " " + anio + (activo ? "  (actual)" : "");
    }

    //=========================================
    // (ADMIN) TODOS LOS CUATRIMESTRES, MÁS RECIENTE PRIMERO
    //=========================================
    public List<Cuatrimestre> obtenerTodos() {

        List<Cuatrimestre> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT idCuatrimestre, periodo, anio, activo
                    FROM cuatrimestre
                    ORDER BY anio DESC,
                        FIELD(periodo,'Septiembre-Diciembre','Mayo-Agosto','Enero-Abril')
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Cuatrimestre(
                        rs.getInt("idCuatrimestre"),
                        rs.getString("periodo"),
                        rs.getInt("anio"),
                        rs.getBoolean("activo")
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
    // (ALUMNO) CUATRIMESTRE ACTIVO ACTUAL
    //=========================================
    public Cuatrimestre obtenerActivo() {

        Cuatrimestre c = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idCuatrimestre, periodo, anio, activo FROM cuatrimestre WHERE activo = 1 LIMIT 1";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                c = new Cuatrimestre(
                        rs.getInt("idCuatrimestre"),
                        rs.getString("periodo"),
                        rs.getInt("anio"),
                        rs.getBoolean("activo")
                );
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return c;
    }

    //=========================================
    // (ADMIN) CREAR UN NUEVO CUATRIMESTRE
    //=========================================
    public boolean agregar(String periodo, int anio) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO cuatrimestre (periodo, anio, activo) VALUES (?, ?, 0)");
            ps.setString(1, periodo);
            ps.setInt(2, anio);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // (ADMIN) EDITAR PERIODO/AÑO DE UN CUATRIMESTRE EXISTENTE
    //=========================================
    public boolean editar(int idCuatrimestre, String periodo, int anio) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE cuatrimestre SET periodo = ?, anio = ? WHERE idCuatrimestre = ?");
            ps.setString(1, periodo);
            ps.setInt(2, anio);
            ps.setInt(3, idCuatrimestre);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // (ADMIN) ELIMINAR UN CUATRIMESTRE (falla si ya tiene horarios
    // o registros asociados, por las llaves foráneas)
    //=========================================
    //=========================================
    // (ADMIN) ELIMINAR CUATRIMESTRE (EN CASCADA)
    //=========================================
    // Un cuatrimestre puede tener horarios regulares y registros
    // extraclase asociados. Se eliminan primero esas dependencias y
    // después el cuatrimestre, todo en una misma transacción.
    public boolean eliminar(int idCuatrimestre) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            ejecutarDelete(con, "DELETE FROM registro WHERE idCuatrimestre = ?", idCuatrimestre);
            ejecutarDelete(con, "DELETE FROM horario_clase WHERE idCuatrimestre = ?", idCuatrimestre);

            int filas = ejecutarDelete(con,
                    "DELETE FROM cuatrimestre WHERE idCuatrimestre = ?", idCuatrimestre);

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

    private int ejecutarDelete(Connection con, String sql, int id) throws Exception {

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, id);
        int filas = ps.executeUpdate();
        ps.close();
        return filas;

    }

    //=========================================
    // (ADMIN) MARCAR UN CUATRIMESTRE COMO EL ACTIVO
    // (desactiva cualquier otro que estuviera activo)
    //=========================================
    public boolean marcarActivo(int idCuatrimestre) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();
            con.setAutoCommit(false);

            try {

                PreparedStatement ps1 = con.prepareStatement("UPDATE cuatrimestre SET activo = 0");
                ps1.executeUpdate();
                ps1.close();

                PreparedStatement ps2 = con.prepareStatement(
                        "UPDATE cuatrimestre SET activo = 1 WHERE idCuatrimestre = ?");
                ps2.setInt(1, idCuatrimestre);
                int filas = ps2.executeUpdate();
                ps2.close();

                con.commit();
                respuesta = filas > 0;

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
                con.close();
            }

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }
}
