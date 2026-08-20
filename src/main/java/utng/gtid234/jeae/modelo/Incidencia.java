package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import utng.gtid234.jeae.conexiones.Conexion;

public class Incidencia {

    private int idIncidencia;
    private String matricula;
    private String nombreAlumno;
    private String laboratorio;
    private Integer idEquipo;
    private String equipo;
    private String tipo;
    private String descripcion;
    private String estado;
    private LocalTime hora;
    private LocalDate fecha;
    private LocalDate fechaResolucion;
    private String observacionesAdmin;

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalDate getFechaResolucion() {
        return fechaResolucion;
    }

    public void setFechaResolucion(LocalDate fechaResolucion) {
        this.fechaResolucion = fechaResolucion;
    }

    public String getObservacionesAdmin() {
        return observacionesAdmin;
    }

    public void setObservacionesAdmin(String observacionesAdmin) {
        this.observacionesAdmin = observacionesAdmin;
    }

    public Incidencia() {
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNombreAlumno() {
        return nombreAlumno;
    }

    public void setNombreAlumno(String nombreAlumno) {
        this.nombreAlumno = nombreAlumno;
    }

    public int getIdIncidencia() {
        return idIncidencia;
    }

    public void setIdIncidencia(int idIncidencia) {
        this.idIncidencia = idIncidencia;
    }

    public String getLaboratorio() {
        return laboratorio;
    }

    public void setLaboratorio(String laboratorio) {
        this.laboratorio = laboratorio;
    }

    public Integer getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(Integer idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getEquipo() {
        return equipo;
    }

    public void setEquipo(String equipo) {
        this.equipo = equipo;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    public LocalTime getHora() {
        return hora;
    }

    public void setHora(LocalTime hora) {
        this.hora = hora;
    }

    //=========================================
    // OBTENER ID DE LABORATORIO POR NOMBRE
    //=========================================
    public int obtenerIdLaboratorio(String nombreLab) {

        int id = -1;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idLaboratorio FROM laboratorio WHERE nombre = ?";

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, nombreLab);

            var rs = ps.executeQuery();

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

    //=========================================
    // INSERTAR INCIDENCIA (estado inicial: Pendiente)
    //=========================================
    public boolean insertarIncidencia(Incidencia inc) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            int idLaboratorio = obtenerIdLaboratorio(inc.getLaboratorio());

            // fecha y hora las pone el propio sistema (CURDATE/CURTIME),
            // no se capturan manualmente
            String sql = """
                    INSERT INTO incidencia
                        (matricula, idLaboratorio, idEquipo, fecha, hora, tipo, descripcion, estado)
                    VALUES (?, ?, ?, CURDATE(), CURTIME(), ?, ?, 'Pendiente')
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, inc.getMatricula());
            ps.setInt(2, idLaboratorio);

            if (inc.getIdEquipo() != null) {
                ps.setInt(3, inc.getIdEquipo());
            } else {
                ps.setNull(3, java.sql.Types.INTEGER);
            }

            ps.setString(4, inc.getTipo());
            ps.setString(5, inc.getDescripcion());

            int filas = ps.executeUpdate();

            respuesta = filas > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // OBTENER TODAS LAS INCIDENCIAS (para administración)
    //=========================================
    public List<Incidencia> obtenerTodas() {

        List<Incidencia> lista = new ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT i.idIncidencia, i.matricula, a.nombre AS nombreAlumno,
                           l.nombre AS laboratorio, i.idEquipo, e.clave AS equipo, i.tipo, i.descripcion,
                           i.estado, i.hora, i.fecha, i.fechaResolucion, i.observacionesAdmin
                    FROM incidencia i
                    INNER JOIN laboratorio l ON i.idLaboratorio = l.idLaboratorio
                    INNER JOIN alumno a ON i.matricula = a.matricula
                    LEFT JOIN equipo_computo e ON i.idEquipo = e.idEquipo
                    ORDER BY i.fecha DESC, i.hora DESC
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Incidencia inc = new Incidencia();
                inc.setIdIncidencia(rs.getInt("idIncidencia"));
                inc.setMatricula(rs.getString("matricula"));
                inc.setNombreAlumno(rs.getString("nombreAlumno"));
                inc.setLaboratorio(rs.getString("laboratorio"));

                int idEquipo = rs.getInt("idEquipo");
                inc.setIdEquipo(rs.wasNull() ? null : idEquipo);
                inc.setEquipo(rs.getString("equipo"));

                inc.setTipo(rs.getString("tipo"));
                inc.setDescripcion(rs.getString("descripcion"));
                inc.setEstado(rs.getString("estado"));

                Time hora = rs.getTime("hora");
                if (hora != null) {
                    inc.setHora(hora.toLocalTime());
                }

                Date fecha = rs.getDate("fecha");
                if (fecha != null) {
                    inc.setFecha(fecha.toLocalDate());
                }

                Date fechaResolucion = rs.getDate("fechaResolucion");
                if (fechaResolucion != null) {
                    inc.setFechaResolucion(fechaResolucion.toLocalDate());
                }

                inc.setObservacionesAdmin(rs.getString("observacionesAdmin"));

                lista.add(inc);
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
    // MARCAR INCIDENCIA COMO RESUELTA
    //=========================================
    public boolean marcarResuelta(int idIncidencia, String observaciones) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    UPDATE incidencia
                    SET estado = 'Resuelta', fechaResolucion = CURDATE(), observacionesAdmin = ?
                    WHERE idIncidencia = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, observaciones);
            ps.setInt(2, idIncidencia);

            int filas = ps.executeUpdate();
            respuesta = filas > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //=========================================
    // REABRIR INCIDENCIA (volver a Pendiente)
    //=========================================
    public boolean reabrir(int idIncidencia) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    UPDATE incidencia
                    SET estado = 'Pendiente', fechaResolucion = NULL
                    WHERE idIncidencia = ?
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idIncidencia);

            int filas = ps.executeUpdate();
            respuesta = filas > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }
}
