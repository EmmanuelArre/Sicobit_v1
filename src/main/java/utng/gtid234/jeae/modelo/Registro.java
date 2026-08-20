package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalDate;
import java.time.LocalTime;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import utng.gtid234.jeae.conexiones.Conexion;

public class Registro {

    private int idRegistro;

    private String matricula;
    private String nombre;
    private String grupo;
    private int idLaboratorio;
    private String laboratorio;
    private int idEquipo;
    private String equipoClave;
    private int idActividad;
    private String actividad;
    private int idCuatrimestre;

    private LocalDate fecha;
    private LocalTime horaEntrada;
    private LocalTime horaSalida;
    private int idBloque;
    private String dia;
    private String tipo;
    private String estado;

    //==========================================
    // GETTERS Y SETTERS
    //==========================================

    public int getIdRegistro() {
        return idRegistro;
    }

    public void setIdRegistro(int idRegistro) {
        this.idRegistro = idRegistro;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getGrupo() {
        return grupo;
    }

    public void setGrupo(String grupo) {
        this.grupo = grupo;
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

    public int getIdEquipo() {
        return idEquipo;
    }

    public void setIdEquipo(int idEquipo) {
        this.idEquipo = idEquipo;
    }

    public String getEquipoClave() {
        return equipoClave;
    }

    public void setEquipoClave(String equipoClave) {
        this.equipoClave = equipoClave;
    }

    public int getIdActividad() {
        return idActividad;
    }

    public void setIdActividad(int idActividad) {
        this.idActividad = idActividad;
    }

    public String getActividad() {
        return actividad;
    }

    public void setActividad(String actividad) {
        this.actividad = actividad;
    }

    public int getIdCuatrimestre() {
        return idCuatrimestre;
    }

    public void setIdCuatrimestre(int idCuatrimestre) {
        this.idCuatrimestre = idCuatrimestre;
    }

    public LocalDate getFecha() {
        return fecha;
    }

    public void setFecha(LocalDate fecha) {
        this.fecha = fecha;
    }

    public LocalTime getHoraEntrada() {
        return horaEntrada;
    }

    public void setHoraEntrada(LocalTime horaEntrada) {
        this.horaEntrada = horaEntrada;
    }

    public LocalTime getHoraSalida() {
        return horaSalida;
    }

    public void setIdBloque(int idBloque) {
        this.idBloque = idBloque;
    }
    public int getIdBloque() {
        return idBloque;
    }

    public void setHoraSalida(LocalTime horaSalida) {
        this.horaSalida = horaSalida;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }

    //==========================================
    // INSERTAR ENTRADA (equipo y actividad de catálogo)
    //==========================================

    public boolean insertarRegistro(Registro reg) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    INSERT INTO registro(
                        matricula, idLaboratorio, idEquipo, idActividad, idCuatrimestre,
                        fecha, dia, idBloque, horaEntrada, horaSalida, tipo
                    )
                    VALUES(?,?,?,?,?,?,?,?,?,?,?)
                    """;

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, reg.getMatricula());
            ps.setInt(2, reg.getIdLaboratorio());
            ps.setInt(3, reg.getIdEquipo());
            ps.setInt(4, reg.getIdActividad());
            ps.setInt(5, reg.getIdCuatrimestre());
            ps.setDate(6, Date.valueOf(LocalDate.now()));
            ps.setString(7, reg.getDia());
            ps.setInt(8, reg.getIdBloque());
            ps.setTime(9, Time.valueOf(reg.getHoraEntrada()));
            ps.setTime(10, Time.valueOf(reg.getHoraSalida()));
            ps.setString(11, "Extraclase");

            ps.executeUpdate();

            respuesta = true;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //==========================================
    // MOSTRAR REGISTROS EN TABLA (del cuatrimestre activo)
    //==========================================

    public ObservableList<Registro> obtenerRegistros(int idCuatrimestre) {

        ObservableList<Registro> lista = FXCollections.observableArrayList();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT
                        r.idRegistro, a.matricula, a.nombre, g.nombre AS grupo,
                        l.nombre AS laboratorio, e.clave AS equipoClave,
                        act.nombre AS actividad, r.fecha, r.dia, r.idBloque,
                        r.horaEntrada, r.horaSalida, r.tipo, r.estado
                    FROM registro r
                    INNER JOIN alumno a ON r.matricula = a.matricula
                    INNER JOIN grupo g ON a.idGrupo = g.idGrupo
                    INNER JOIN laboratorio l ON r.idLaboratorio = l.idLaboratorio
                    LEFT JOIN equipo_computo e ON r.idEquipo = e.idEquipo
                    INNER JOIN actividad act ON r.idActividad = act.idActividad
                    WHERE r.idCuatrimestre = ?
                    ORDER BY r.idRegistro DESC
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idCuatrimestre);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Registro r = new Registro();

                r.setIdRegistro(rs.getInt("idRegistro"));
                r.setMatricula(rs.getString("matricula"));
                r.setNombre(rs.getString("nombre"));
                r.setGrupo(rs.getString("grupo"));
                r.setLaboratorio(rs.getString("laboratorio"));
                r.setEquipoClave(rs.getString("equipoClave"));
                r.setActividad(rs.getString("actividad"));
                r.setDia(rs.getString("dia"));
                r.setIdBloque(rs.getInt("idBloque"));
                r.setTipo(rs.getString("tipo"));
                r.setEstado(rs.getString("estado"));

                if (rs.getDate("fecha") != null) {
                    r.setFecha(rs.getDate("fecha").toLocalDate());
                }

                if (rs.getTime("horaEntrada") != null) {
                    r.setHoraEntrada(rs.getTime("horaEntrada").toLocalTime());
                }

                if (rs.getTime("horaSalida") != null) {
                    r.setHoraSalida(rs.getTime("horaSalida").toLocalTime());
                }

                lista.add(r);

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return lista;
    }

    //==========================================
    // OBTENER LABORATORIOS PARA COMBOBOX
    //==========================================

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

    //==========================================
    // OBTENER ID DEL LABORATORIO
    //==========================================

    public int obtenerIdLaboratorio(String nombre) {

        int id = 0;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT idLaboratorio FROM laboratorio WHERE nombre=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, nombre);

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

    //==========================================
    // CANCELAR UN REGISTRO (no lo borra, solo cambia su estado).
    // Solo se permite cancelar registros de HOY cuya hora de entrada
    // sea POSTERIOR a la hora actual del sistema; la validación se hace
    // en el propio SQL para que no se pueda evadir desde la interfaz.
    //==========================================

    public boolean cancelar(int idRegistro) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    UPDATE registro
                    SET estado = 'Cancelado'
                    WHERE idRegistro = ?
                      AND estado = 'Activo'
                      AND fecha = CURDATE()
                      AND horaEntrada > CURTIME()
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idRegistro);

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return respuesta;
    }

    //==========================================
    // BUSCAR ALUMNO POR MATRÍCULA
    //==========================================

    public Alumno buscarAlumno(String matricula) {

        Alumno alumno = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "SELECT alumno.matricula, alumno.nombre, grupo.nombre AS grupo "
                    + "FROM alumno "
                    + "INNER JOIN grupo "
                    + "ON alumno.idGrupo = grupo.idGrupo "
                    + "WHERE alumno.matricula=?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, matricula);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                alumno = new Alumno();

                alumno.setMatricula(rs.getString("matricula"));
                alumno.setNombre(rs.getString("nombre"));
                alumno.setGrupo(rs.getString("grupo"));

            }

            rs.close();
            ps.close();
            con.close();

        } catch (Exception e) {

            e.printStackTrace();

        }

        return alumno;
    }

}
