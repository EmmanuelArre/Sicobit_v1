package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class BloqueHorario {

    private int idBloque;
    private String horaInicio;
    private String horaFin;
    private String ultimoError;

    public String getUltimoError() {
        return ultimoError;
    }


    public int getIdBloque() {
        return idBloque;
    }


    public void setIdBloque(int idBloque) {
        this.idBloque = idBloque;
    }


    public String getHoraInicio() {
        return horaInicio;
    }


    public void setHoraInicio(String horaInicio) {
        this.horaInicio = horaInicio;
    }


    public String getHoraFin() {
        return horaFin;
    }


    public void setHoraFin(String horaFin) {
        this.horaFin = horaFin;
    }


    @Override
    public String toString(){

        try {
            DateTimeFormatter salida = DateTimeFormatter.ofPattern("HH:mm");

            LocalTime inicio = LocalTime.parse(horaInicio);
            LocalTime fin = LocalTime.parse(horaFin);

            return inicio.format(salida) + " - " + fin.format(salida);

        } catch (Exception e){
            // Si por alguna razón no se puede parsear, muestra el texto tal cual
            return horaInicio + " - " + horaFin;
        }

    }

    public List<BloqueHorario> obtenerTodos() {

        List<BloqueHorario> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idBloque, horaInicio, horaFin FROM bloque_horario ORDER BY horaInicio";

            PreparedStatement ps = con.prepareStatement(sql);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                BloqueHorario b = new BloqueHorario();
                b.setIdBloque(rs.getInt("idBloque"));
                b.setHoraInicio(rs.getString("horaInicio"));
                b.setHoraFin(rs.getString("horaFin"));
                lista.add(b);

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
    // AGREGAR UN BLOQUE NUEVO
    //=========================================
    public boolean agregar(String horaInicio, String horaFin) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "INSERT INTO bloque_horario (horaInicio, horaFin) VALUES (?, ?)");
            ps.setString(1, horaInicio);
            ps.setString(2, horaFin);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // EDITAR LAS HORAS DE UN BLOQUE EXISTENTE
    //=========================================
    public boolean editar(int idBloque, String horaInicio, String horaFin) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            PreparedStatement ps = con.prepareStatement(
                    "UPDATE bloque_horario SET horaInicio = ?, horaFin = ? WHERE idBloque = ?");
            ps.setString(1, horaInicio);
            ps.setString(2, horaFin);
            ps.setInt(3, idBloque);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // ELIMINAR UN BLOQUE (falla si ya tiene clases u horarios/registros
    // asociados, por las llaves foráneas)
    //=========================================
    //=========================================
    // (ADMIN) ELIMINAR BLOQUE HORARIO (EN CASCADA)
    //=========================================
    // Un bloque horario puede estar usado en horarios regulares y en
    // registros extraclase. Se eliminan primero esas dependencias y
    // después el bloque, todo en una misma transacción.
    public boolean eliminar(int idBloque) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            ejecutarDelete(con, "DELETE FROM registro WHERE idBloque = ?", idBloque);
            ejecutarDelete(con, "DELETE FROM horario_clase WHERE idBloque = ?", idBloque);

            int filas = ejecutarDelete(con, "DELETE FROM bloque_horario WHERE idBloque = ?", idBloque);

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

}