package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Materia {

    private int idMateria;
    private String nombre;
    private String estatus;

    public Materia() {
    }

    public Materia(int idMateria, String nombre) {
        this.idMateria = idMateria;
        this.nombre = nombre;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
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

    public List<Materia> obtenerActivas() {

        List<Materia> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idMateria, nombre FROM materia WHERE estatus = 'Activa' ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Materia(rs.getInt("idMateria"), rs.getString("nombre")));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    public List<Materia> obtenerTodas() {

        List<Materia> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idMateria, nombre, estatus FROM materia ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Materia m = new Materia(rs.getInt("idMateria"), rs.getString("nombre"));
                m.setEstatus(rs.getString("estatus"));
                lista.add(m);
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
                    "INSERT INTO materia (nombre, estatus) VALUES (?, 'Activa')");
            ps.setString(1, nombre);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    public boolean cambiarEstatus(int idMateria, String estatus) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE materia SET estatus = ? WHERE idMateria = ?");
            ps.setString(1, estatus);
            ps.setInt(2, idMateria);

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

    // Borra la materia. Si ya está referenciada por algún profesor/grupo
    // (llave foránea), la base de datos rechaza el borrado; en ese caso
    // conviene usar cambiarEstatus(..., "Inactiva") en vez de eliminarla,
    // para no perder información ya asociada.
    public boolean eliminar(int idMateria) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "DELETE FROM materia WHERE idMateria = ?");
            ps.setInt(1, idMateria);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;
    }
}
