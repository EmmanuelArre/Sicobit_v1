package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Catálogo de los apartados/pantallas que existen dentro de la parte de
 * administrador (Registros, Horarios regulares, Incidencias, etc.). Se
 * usa para que el superadministrador elija cuáles puede ver cada rol.
 */
public class Modulo {

    private int idModulo;
    private String clave;
    private String nombre;

    public Modulo() {
    }

    public Modulo(int idModulo, String clave, String nombre) {
        this.idModulo = idModulo;
        this.clave = clave;
        this.nombre = nombre;
    }

    public int getIdModulo() {
        return idModulo;
    }

    public void setIdModulo(int idModulo) {
        this.idModulo = idModulo;
    }

    public String getClave() {
        return clave;
    }

    public void setClave(String clave) {
        this.clave = clave;
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

    public List<Modulo> obtenerTodos() {

        List<Modulo> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idModulo, clave, nombre FROM modulo ORDER BY idModulo";

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                lista.add(new Modulo(rs.getInt("idModulo"), rs.getString("clave"), rs.getString("nombre")));
            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    // Claves de los módulos que en verdad son apartados del menú del
    // administrador (ver Dashboard.fxml / DashboardController) y que por
    // lo tanto se le pueden asignar o quitar a un rol. Se excluyen:
    //  - DASHBOARD: es "Inicio", siempre visible, no es un permiso real.
    //  - EQUIPO_COMPUTO: no tiene botón propio en el menú (se administra
    //    dentro de Laboratorios).
    //  - ADMINISTRADORES: solo lo puede ver el superadministrador, nunca
    //    se le asigna a un rol normal.
    private static final java.util.List<String> CLAVES_ASIGNABLES = java.util.List.of(
            "REGISTROS_EXTRACLASE",
            "HORARIOS_REGULARES",
            "BLOQUES_HORARIO",
            "CUATRIMESTRES",
            "PROFESORES",
            "MATERIAS",
            "GESTION_ALUMNOS",
            "ACTIVIDADES",
            "INCIDENCIAS",
            "LABORATORIOS",
            "REPORTES",
            "EXPORTAR",
            "RESPALDOS"
    );

    //=========================================
    // Módulos que corresponden a los apartados reales del menú de
    // administrador (los que se pueden asignar por rol desde la
    // pantalla de Administradores)
    //=========================================
    public List<Modulo> obtenerAsignables() {

        List<Modulo> lista = new ArrayList<>();

        for (Modulo m : obtenerTodos()) {
            if (CLAVES_ASIGNABLES.contains(m.getClave())) {
                lista.add(m);
            }
        }

        return lista;
    }
}
