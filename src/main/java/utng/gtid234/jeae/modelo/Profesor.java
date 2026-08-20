package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Profesor {

    private int idProfesor;
    private String nombre;
    private String estatus;
    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    public Profesor() {
    }

    public Profesor(int idProfesor, String nombre) {
        this.idProfesor = idProfesor;
        this.nombre = nombre;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEstatus() {
        return estatus;
    }

    public void setEstatus(String estatus) {
        this.estatus = estatus;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public List<Profesor> obtenerActivos() {

        List<Profesor> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idProfesor, nombre FROM profesor WHERE estatus = 'Activo' ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Profesor(rs.getInt("idProfesor"), rs.getString("nombre")));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    public List<Profesor> obtenerTodos() {

        List<Profesor> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idProfesor, nombre, estatus FROM profesor ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Profesor p = new Profesor(rs.getInt("idProfesor"), rs.getString("nombre"));
                p.setEstatus(rs.getString("estatus"));
                lista.add(p);
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    public boolean agregar(String nombre) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO profesor (nombre, estatus) VALUES (?, 'Activo')");
            ps.setString(1, nombre);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    public boolean cambiarEstatus(int idProfesor, String estatus) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE profesor SET estatus = ? WHERE idProfesor = ?");
            ps.setString(1, estatus);
            ps.setInt(2, idProfesor);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // (ADMIN) ELIMINAR PROFESOR (EN CASCADA)
    //=========================================
    // Un profesor puede estar asignado a varios horarios de clase
    // (horario_clase.idProfesor). Se eliminan primero esos horarios y
    // después el profesor, todo dentro de una misma transacción.
    public boolean eliminar(int idProfesor) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            PreparedStatement psHorarios = con.prepareStatement(
                    "DELETE FROM horario_clase WHERE idProfesor = ?");
            psHorarios.setInt(1, idProfesor);
            psHorarios.executeUpdate();
            psHorarios.close();

            PreparedStatement psProfesor = con.prepareStatement(
                    "DELETE FROM profesor WHERE idProfesor = ?");
            psProfesor.setInt(1, idProfesor);
            int filas = psProfesor.executeUpdate();
            psProfesor.close();

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
}
