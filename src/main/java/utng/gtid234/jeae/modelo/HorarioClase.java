package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Administra el horario NORMAL de clases (tabla horario_clase), ahora
 * llamado "Horarios regulares". profesor y materia se eligen por
 * catálogo (idProfesor / idMateria) en vez de texto libre, y cada
 * clase pertenece a un cuatrimestre.
 */
public class HorarioClase {

    private int idHorario;
    private int idLaboratorio;
    private String laboratorio;
    private int idCuatrimestre;
    private String cuatrimestreTexto;
    private String dia;
    private int idBloque;
    private String bloqueTexto;
    private int idMateria;
    private String materia;
    private int idProfesor;
    private String profesor;

    public HorarioClase() {
    }

    public int getIdHorario() {
        return idHorario;
    }

    public void setIdHorario(int idHorario) {
        this.idHorario = idHorario;
    }

    public int getIdLaboratorio() {
        return idLaboratorio;
    }

    public void setIdLaboratorio(int idLaboratorio) {
        this.idLaboratorio = idLaboratorio;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public int getIdCuatrimestre() {
        return idCuatrimestre;
    }

    public void setIdCuatrimestre(int idCuatrimestre) {
        this.idCuatrimestre = idCuatrimestre;
    }

    public String getCuatrimestreTexto() {
        return cuatrimestreTexto;
    }

    public void setCuatrimestreTexto(String cuatrimestreTexto) {
        this.cuatrimestreTexto = cuatrimestreTexto;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public int getIdBloque() {
        return idBloque;
    }

    public void setIdBloque(int idBloque) {
        this.idBloque = idBloque;
    }

    public String getBloqueTexto() {
        return bloqueTexto;
    }

    public void setBloqueTexto(String bloqueTexto) {
        this.bloqueTexto = bloqueTexto;
    }

    public int getIdMateria() {
        return idMateria;
    }

    public void setIdMateria(int idMateria) {
        this.idMateria = idMateria;
    }

    public String getMateria() {
        return materia;
    }

    public void setMateria(String materia) {
        this.materia = materia;
    }

    public int getIdProfesor() {
        return idProfesor;
    }

    public void setIdProfesor(int idProfesor) {
        this.idProfesor = idProfesor;
    }

    public String getProfesor() {
        return profesor;
    }

    public void setProfesor(String profesor) {
        this.profesor = profesor;
    }

    //=========================================
    // OBTENER HORARIOS REGULARES DE UN CUATRIMESTRE (con detalle)
    //=========================================
    public List<HorarioClase> obtenerPorCuatrimestre(int idCuatrimestre) {

        List<HorarioClase> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT
                        h.idHorario, h.idLaboratorio, lab.nombre AS laboratorio,
                        h.idCuatrimestre, CONCAT(c.periodo,' ',c.anio) AS cuatrimestreTexto,
                        h.dia, h.idBloque,
                        CONCAT(TIME_FORMAT(b.horaInicio,'%H:%i'),' - ',TIME_FORMAT(b.horaFin,'%H:%i')) AS bloqueTexto,
                        h.idMateria, m.nombre AS materia,
                        h.idProfesor, p.nombre AS profesor
                    FROM horario_clase h
                    INNER JOIN laboratorio lab ON h.idLaboratorio = lab.idLaboratorio
                    INNER JOIN bloque_horario b ON h.idBloque = b.idBloque
                    INNER JOIN cuatrimestre c ON h.idCuatrimestre = c.idCuatrimestre
                    INNER JOIN materia m ON h.idMateria = m.idMateria
                    INNER JOIN profesor p ON h.idProfesor = p.idProfesor
                    WHERE h.idCuatrimestre = ?
                    ORDER BY FIELD(h.dia,'Lunes','Martes','Miércoles','Jueves','Viernes','Sábado'), b.horaInicio
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                HorarioClase h = new HorarioClase();
                h.setIdHorario(rs.getInt("idHorario"));
                h.setIdLaboratorio(rs.getInt("idLaboratorio"));
                h.setLaboratorio(rs.getString("laboratorio"));
                h.setIdCuatrimestre(rs.getInt("idCuatrimestre"));
                h.setCuatrimestreTexto(rs.getString("cuatrimestreTexto"));
                h.setDia(rs.getString("dia"));
                h.setIdBloque(rs.getInt("idBloque"));
                h.setBloqueTexto(rs.getString("bloqueTexto"));
                h.setIdMateria(rs.getInt("idMateria"));
                h.setMateria(rs.getString("materia"));
                h.setIdProfesor(rs.getInt("idProfesor"));
                h.setProfesor(rs.getString("profesor"));
                lista.add(h);

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
    // VALIDAR SI YA HAY UNA CLASE EN ESE LABORATORIO/DIA/BLOQUE/CUATRIMESTRE
    // Devuelve null si está libre, o un mensaje si está ocupado.
    //=========================================
    public String verificarOcupado(int idLaboratorio, String dia, int idBloque, int idCuatrimestre, int idHorarioExcluir) {

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT m.nombre AS materia, p.nombre AS profesor
                    FROM horario_clase h
                    INNER JOIN materia m ON h.idMateria = m.idMateria
                    INNER JOIN profesor p ON h.idProfesor = p.idProfesor
                    WHERE h.idLaboratorio = ? AND h.dia = ? AND h.idBloque = ?
                      AND h.idCuatrimestre = ? AND h.idHorario <> ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idLaboratorio);
            ps.setString(2, dia);
            ps.setInt(3, idBloque);
            ps.setInt(4, idCuatrimestre);
            ps.setInt(5, idHorarioExcluir);

            ResultSet rs = ps.executeQuery();

            String mensaje = null;

            if (rs.next()) {
                mensaje = "Ese laboratorio ya tiene la clase \"" + rs.getString("materia")
                        + "\" con el profesor " + rs.getString("profesor") + " en ese día y bloque.";
            }

            rs.close();
            ps.close();
            con.close();

            return mensaje;

        } catch (Exception e) {

            e.printStackTrace();
            return "No se pudo validar la disponibilidad del horario.";

        }
    }

    //=========================================
    // AGREGAR CLASE AL HORARIO REGULAR (valida choque primero)
    //=========================================
    public boolean agregar(HorarioClase h) {

        String ocupado = verificarOcupado(h.getIdLaboratorio(), h.getDia(), h.getIdBloque(), h.getIdCuatrimestre(), -1);

        if (ocupado != null) {
            return false;
        }

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    INSERT INTO horario_clase (idLaboratorio, idCuatrimestre, dia, idBloque, idMateria, idProfesor)
                    VALUES (?, ?, ?, ?, ?, ?)
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, h.getIdLaboratorio());
            ps.setInt(2, h.getIdCuatrimestre());
            ps.setString(3, h.getDia());
            ps.setInt(4, h.getIdBloque());
            ps.setInt(5, h.getIdMateria());
            ps.setInt(6, h.getIdProfesor());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

    //=========================================
    // EDITAR CLASE DEL HORARIO REGULAR (valida choque, excluyéndose a sí misma)
    //=========================================
    public boolean editar(HorarioClase h) {

        String ocupado = verificarOcupado(h.getIdLaboratorio(), h.getDia(), h.getIdBloque(), h.getIdCuatrimestre(), h.getIdHorario());

        if (ocupado != null) {
            return false;
        }

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    UPDATE horario_clase
                    SET idLaboratorio = ?, idCuatrimestre = ?, dia = ?, idBloque = ?, idMateria = ?, idProfesor = ?
                    WHERE idHorario = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, h.getIdLaboratorio());
            ps.setInt(2, h.getIdCuatrimestre());
            ps.setString(3, h.getDia());
            ps.setInt(4, h.getIdBloque());
            ps.setInt(5, h.getIdMateria());
            ps.setInt(6, h.getIdProfesor());
            ps.setInt(7, h.getIdHorario());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

    //=========================================
    // ELIMINAR CLASE DEL HORARIO REGULAR
    //=========================================
    public boolean eliminar(int idHorario) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = "DELETE FROM horario_clase WHERE idHorario = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, idHorario);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;

    }

}
