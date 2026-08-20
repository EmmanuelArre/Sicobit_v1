package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Actividad {

    private int idActividad;
    private String nombre;
    private String estatus;

    public Actividad() {
    }

    public Actividad(int idActividad, String nombre) {
        this.idActividad = idActividad;
        this.nombre = nombre;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
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

    public List<Actividad> obtenerActivas() {

        List<Actividad> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idActividad, nombre FROM actividad WHERE estatus = 'Activa' ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Actividad(rs.getInt("idActividad"), rs.getString("nombre")));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    public List<Actividad> obtenerTodas() {

        List<Actividad> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idActividad, nombre, estatus FROM actividad ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Actividad a = new Actividad(rs.getInt("idActividad"), rs.getString("nombre"));
                a.setEstatus(rs.getString("estatus"));
                lista.add(a);
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
                    "INSERT INTO actividad (nombre, estatus) VALUES (?, 'Activa')");
            ps.setString(1, nombre);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    public boolean actualizar(int idActividad, String nombre) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE actividad SET nombre = ? WHERE idActividad = ?");
            ps.setString(1, nombre);
            ps.setInt(2, idActividad);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    private String ultimoError;

    public String obtenerUltimoError() {
        return ultimoError;
    }

    // Borra la actividad. Si ya fue usada en algún registro (llave foránea
    // en la tabla registro), la base de datos rechaza el borrado; en ese
    // caso se recomienda usar cambiarEstatus(..., "Inactiva") en vez de
    // eliminarla, para no perder el historial de registros que la usan.
    public boolean eliminar(int idActividad) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM actividad WHERE idActividad = ?");
            ps.setInt(1, idActividad);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;
    }

    public boolean cambiarEstatus(int idActividad, String estatus) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE actividad SET estatus = ? WHERE idActividad = ?");
            ps.setString(1, estatus);
            ps.setInt(2, idActividad);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }
}
