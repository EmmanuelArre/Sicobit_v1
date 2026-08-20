package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Roles de administrador. El superadministrador puede crear roles nuevos
 * además de "admin"/"superadmin" (por ejemplo "soporte", "coordinador"),
 * y elegir qué módulos del panel de administrador puede ver cada uno.
 */
public class Rol {

    private int idRol;
    private String nombre;
    private boolean esSuperAdmin;

    public Rol() {
    }

    public Rol(int idRol, String nombre, boolean esSuperAdmin) {
        this.idRol = idRol;
        this.nombre = nombre;
        this.esSuperAdmin = esSuperAdmin;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public boolean isEsSuperAdmin() {
        return esSuperAdmin;
    }

    public void setEsSuperAdmin(boolean esSuperAdmin) {
        this.esSuperAdmin = esSuperAdmin;
    }

    @Override
    public String toString() {
        return nombre;
    }

    //=========================================
    // TODOS LOS ROLES EXISTENTES
    //=========================================
    public List<Rol> obtenerTodos() {

        List<Rol> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idRol, nombre, esSuperAdmin FROM rol ORDER BY nombre";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Rol(rs.getInt("idRol"), rs.getString("nombre"), rs.getBoolean("esSuperAdmin")));
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
    // CREAR UN ROL NUEVO (nunca superadmin: eso solo se define en BD)
    //=========================================
    public boolean crear(String nombre) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO rol (nombre, esSuperAdmin) VALUES (?, 0)");
            ps.setString(1, nombre);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // ELIMINAR UN ROL (falla si algún administrador aún lo usa, por la FK)
    //=========================================
    public boolean eliminar(int idRol) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement("DELETE FROM rol WHERE idRol = ? AND esSuperAdmin = 0");
            ps.setInt(1, idRol);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // MÓDULOS QUE PUEDE VER UN ROL (ids)
    //=========================================
    public Set<Integer> obtenerModulosDeRol(int idRol) {

        Set<Integer> ids = new HashSet<>();

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "SELECT idModulo FROM rol_modulo WHERE idRol = ?");
            ps.setInt(1, idRol);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                ids.add(rs.getInt("idModulo"));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return ids;
    }

    //=========================================
    // GUARDAR LOS MÓDULOS QUE PUEDE VER UN ROL
    // (reemplaza por completo la selección anterior)
    //=========================================
    public boolean guardarModulosDeRol(int idRol, Set<Integer> idsModulo) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();
            con.setAutoCommit(false);

            try {

                PreparedStatement psDel = con.prepareStatement("DELETE FROM rol_modulo WHERE idRol = ?");
                psDel.setInt(1, idRol);
                psDel.executeUpdate();
                psDel.close();

                PreparedStatement psIns = con.prepareStatement(
                        "INSERT INTO rol_modulo (idRol, idModulo) VALUES (?, ?)");

                for (Integer idModulo : idsModulo) {
                    psIns.setInt(1, idRol);
                    psIns.setInt(2, idModulo);
                    psIns.addBatch();
                }

                psIns.executeBatch();
                psIns.close();

                con.commit();
                respuesta = true;

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
