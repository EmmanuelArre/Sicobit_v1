package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import utng.gtid234.jeae.conexiones.Conexion;

public class Administrador {

    private int idAdministrador;
    private String usuario;
    private String password;
    private String nombre;
    private int idRol;
    private String rol;
    private boolean superAdmin;

    // Claves de los módulos que este administrador puede ver, cargadas
    // al iniciar sesión (rol_modulo). Vacío para el superadmin porque
    // él siempre puede ver todo.
    private Set<String> modulosPermitidos = new HashSet<>();

    public Administrador() {
    }

    public Administrador(int idAdministrador, String usuario, String nombre) {
        this.idAdministrador = idAdministrador;
        this.usuario = usuario;
        this.nombre = nombre;
    }

    public int getIdAdministrador() {
        return idAdministrador;
    }

    public void setIdAdministrador(int idAdministrador) {
        this.idAdministrador = idAdministrador;
    }

    public String getUsuario() {
        return usuario;
    }

    public void setUsuario(String usuario) {
        this.usuario = usuario;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getIdRol() {
        return idRol;
    }

    public void setIdRol(int idRol) {
        this.idRol = idRol;
    }

    public String getRol() {
        return rol;
    }

    public void setRol(String rol) {
        this.rol = rol;
    }

    public boolean esSuperAdmin() {
        return superAdmin;
    }

    public void setSuperAdmin(boolean superAdmin) {
        this.superAdmin = superAdmin;
    }

    // El superadmin siempre puede ver todos los módulos; los demás roles
    // solo los que tengan asignados en rol_modulo.
    public boolean puedeVer(String claveModulo) {
        return superAdmin || modulosPermitidos.contains(claveModulo);
    }

    public Administrador validarLogin(String usuario, String password) {

        Administrador admin = null;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT a.idAdministrador, a.usuario, a.nombre, a.idRol, r.nombre AS rol, r.esSuperAdmin
                    FROM administrador a
                    INNER JOIN rol r ON a.idRol = r.idRol
                    WHERE a.usuario = ? AND a.password = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, usuario);
            ps.setString(2, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                admin = new Administrador();
                admin.setIdAdministrador(rs.getInt("idAdministrador"));
                admin.setUsuario(rs.getString("usuario"));
                admin.setNombre(rs.getString("nombre"));
                admin.setIdRol(rs.getInt("idRol"));
                admin.setRol(rs.getString("rol"));
                admin.setSuperAdmin(rs.getBoolean("esSuperAdmin"));

            }

            rs.close();
            ps.close();

            if (admin != null && !admin.esSuperAdmin()) {

                String sqlModulos = """
                        SELECT m.clave FROM rol_modulo rm
                        INNER JOIN modulo m ON rm.idModulo = m.idModulo
                        WHERE rm.idRol = ?
                        """;

                PreparedStatement psMod = con.prepareStatement(sqlModulos);
                psMod.setInt(1, admin.getIdRol());

                ResultSet rsMod = psMod.executeQuery();

                while (rsMod.next()) {
                    admin.modulosPermitidos.add(rsMod.getString("clave"));
                }

                rsMod.close();
                psMod.close();
            }

            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return admin;
    }

    public List<Administrador> obtenerTodos() {

        List<Administrador> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT a.idAdministrador, a.usuario, a.nombre, a.idRol, r.nombre AS rol
                    FROM administrador a
                    INNER JOIN rol r ON a.idRol = r.idRol
                    ORDER BY a.nombre
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Administrador a = new Administrador();
                a.setIdAdministrador(rs.getInt("idAdministrador"));
                a.setUsuario(rs.getString("usuario"));
                a.setNombre(rs.getString("nombre"));
                a.setIdRol(rs.getInt("idRol"));
                a.setRol(rs.getString("rol"));
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

    public boolean agregar(Administrador a) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "INSERT INTO administrador (usuario, password, nombre, idRol) VALUES (?, ?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, a.getUsuario());
            ps.setString(2, a.getPassword());
            ps.setString(3, a.getNombre());
            ps.setInt(4, a.getIdRol());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }


    public boolean eliminar(int idAdministrador) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "DELETE FROM administrador WHERE idAdministrador = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idAdministrador);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }
}
