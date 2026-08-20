package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Grupo {

    private int idGrupo;
    private String nombre;
    private String ultimoError;

    public Grupo() {
    }

    public String getUltimoError() {
        return ultimoError;
    }

    public Grupo(int idGrupo, String nombre) {
        this.idGrupo = idGrupo;
        this.nombre = nombre;
    }

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    @Override
    public String toString() {
        return nombre;
    }

    public List<Grupo> obtenerTodos() {

        List<Grupo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idGrupo, nombre FROM grupo ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                lista.add(new Grupo(rs.getInt("idGrupo"), rs.getString("nombre")));

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;

    }

    public boolean agregar(Grupo grupo) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "INSERT INTO grupo (nombre) VALUES (?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, grupo.getNombre());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;

    }


    public boolean editar(Grupo grupo) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "UPDATE grupo SET nombre = ? WHERE idGrupo = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, grupo.getNombre());
            ps.setInt(2, grupo.getIdGrupo());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;

    }

    public boolean eliminar(int idGrupo) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "DELETE FROM grupo WHERE idGrupo = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idGrupo);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;

    }

    // Busca un grupo por nombre (sin distinguir mayúsculas/minúsculas ni
    // espacios sobrantes) y si no existe lo crea. Se usa en la importación
    // masiva de alumnos desde Excel, donde el grupo viene como texto.
    public Grupo obtenerOCrear(String nombre) {

        String nombreLimpio = nombre == null ? "" : nombre.trim();

        if (nombreLimpio.isEmpty()) {
            return null;
        }

        try {

            Connection con = Conexion.conectar();

            PreparedStatement psBuscar = con.prepareStatement(
                    "SELECT idGrupo, nombre FROM grupo WHERE UPPER(nombre) = UPPER(?)");
            psBuscar.setString(1, nombreLimpio);

            ResultSet rs = psBuscar.executeQuery();

            if (rs.next()) {

                Grupo g = new Grupo(rs.getInt("idGrupo"), rs.getString("nombre"));
                rs.close();
                psBuscar.close();
                con.close();
                return g;
            }

            rs.close();
            psBuscar.close();

            PreparedStatement psInsertar = con.prepareStatement(
                    "INSERT INTO grupo (nombre) VALUES (?)",
                    java.sql.Statement.RETURN_GENERATED_KEYS);
            psInsertar.setString(1, nombreLimpio);
            psInsertar.executeUpdate();

            Grupo creado = null;

            ResultSet llaves = psInsertar.getGeneratedKeys();
            if (llaves.next()) {
                creado = new Grupo(llaves.getInt(1), nombreLimpio);
            }
            llaves.close();
            psInsertar.close();
            con.close();

            return creado;

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();
            return null;

        }
    }

}