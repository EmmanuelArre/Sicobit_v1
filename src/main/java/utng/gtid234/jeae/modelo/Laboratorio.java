package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utng.gtid234.jeae.conexiones.Conexion;

public class Laboratorio {

    private int idLaboratorio;
    private String nombre;
    private int capacidad;
    private String ubicacion;
    private String estado;
    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }

    public Laboratorio() {
    }

    public Laboratorio(int idLaboratorio, String nombre) {
        this.idLaboratorio = idLaboratorio;
        this.nombre = nombre;
    }

    public Laboratorio(int idLaboratorio, String nombre, int capacidad, String ubicacion, String estado) {
        this.idLaboratorio = idLaboratorio;
        this.nombre = nombre;
        this.capacidad = capacidad;
        this.ubicacion = ubicacion;
        this.estado = estado;
    }

    public int getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(int idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public int getCapacidad() {
        return capacidad;
    }

    public void setCapacidad(int capacidad) {
        this.capacidad = capacidad;
    }

    public String getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(String ubicacion) {
        this.ubicacion = ubicacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    //=========================================
    // OBTENER LISTA COMPLETA DE LABORATORIOS (id + nombre)
    // Se usa para armar las columnas de la tabla de disponibilidad
    //=========================================

    public List<Laboratorio> obtenerListaCompleta() {

        List<Laboratorio> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idLaboratorio, nombre FROM laboratorio ORDER BY idLaboratorio";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                lista.add(new Laboratorio(
                        rs.getInt("idLaboratorio"),
                        rs.getString("nombre")
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
    // (ADMIN) OBTENER TODOS LOS LABORATORIOS CON DETALLE
    //=========================================

    public List<Laboratorio> obtenerTodosDetalle() {

        List<Laboratorio> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT idLaboratorio, nombre, capacidad, ubicacion, estatus
                    FROM laboratorio
                    ORDER BY idLaboratorio
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                lista.add(new Laboratorio(
                        rs.getInt("idLaboratorio"),
                        rs.getString("nombre"),
                        rs.getInt("capacidad"),
                        rs.getString("ubicacion"),
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
    // (ADMIN) AGREGAR LABORATORIO
    //=========================================

    public boolean agregar(Laboratorio lab) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    INSERT INTO laboratorio (nombre, capacidad, ubicacion, estatus)
                    VALUES (?, ?, ?, ?)
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, lab.getNombre());
            ps.setInt(2, lab.getCapacidad());
            ps.setString(3, lab.getUbicacion());
            ps.setString(4, lab.getEstado());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

    //=========================================
    // (ADMIN) EDITAR LABORATORIO
    //=========================================

    public boolean editar(Laboratorio lab) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    UPDATE laboratorio
                    SET nombre = ?, capacidad = ?, ubicacion = ?, estatus = ?
                    WHERE idLaboratorio = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, lab.getNombre());
            ps.setInt(2, lab.getCapacidad());
            ps.setString(3, lab.getUbicacion());
            ps.setString(4, lab.getEstado());
            ps.setInt(5, lab.getIdLaboratorio());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

    //=========================================
    // (ADMIN) CAMBIAR SOLO EL ESTADO
    //=========================================

    public boolean cambiarEstado(int idLaboratorio, String nuevoEstado) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "UPDATE laboratorio SET estatus = ? WHERE idLaboratorio = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nuevoEstado);
            ps.setInt(2, idLaboratorio);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

    //=========================================
    // (ADMIN) ELIMINAR LABORATORIO
    //=========================================

    //=========================================
    // (ADMIN) ELIMINAR LABORATORIO (EN CASCADA)
    //=========================================
    // El laboratorio es referenciado por incidencias, préstamos,
    // registros extraclase y horarios regulares (todas con FK
    // obligatoria), así que esas filas se eliminan primero. El equipo
    // de cómputo del laboratorio se elimina automáticamente por la base
    // de datos (ON DELETE CASCADE), pero eso ocurre después de borrar
    // registro/prestamo_equipo/incidencia para no violar sus FKs hacia
    // equipo_computo. Todo en una misma transacción.
    public boolean eliminar(int idLaboratorio) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            ejecutarDelete(con, "DELETE FROM incidencia WHERE idLaboratorio = ?", idLaboratorio);
            ejecutarDelete(con, "DELETE FROM prestamo_equipo WHERE idLaboratorio = ?", idLaboratorio);
            ejecutarDelete(con, "DELETE FROM registro WHERE idLaboratorio = ?", idLaboratorio);
            ejecutarDelete(con, "DELETE FROM horario_clase WHERE idLaboratorio = ?", idLaboratorio);

            // equipo_computo se borra solo (ON DELETE CASCADE) al borrar el laboratorio
            int filas = ejecutarDelete(con,
                    "DELETE FROM laboratorio WHERE idLaboratorio = ?", idLaboratorio);

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
    // OBTENER LABORATORIOS
    //=========================================

    public ObservableList<String> obtenerLaboratorios() {

        ObservableList<String> lista = FXCollections.observableArrayList();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT nombre FROM laboratorio ORDER BY idLaboratorio";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                lista.add(rs.getString("nombre"));

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
    // OBTENER ID DEL LABORATORIO
    //=========================================

    public int obtenerIdLaboratorio(String nombreLaboratorio) {

        int id = 0;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idLaboratorio FROM laboratorio WHERE nombre=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nombreLaboratorio);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                id = rs.getInt("idLaboratorio");

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return id;

    }

}