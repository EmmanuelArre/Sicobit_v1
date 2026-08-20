package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Time;
import java.time.LocalTime;

import utng.gtid234.jeae.conexiones.Conexion;

/**
 * Préstamo de un equipo de cómputo específico (idEquipo) de un
 * laboratorio, en vez de un "equipo genérico" con cantidad.
 */
public class PrestamoEquipo {

    private int idPrestamo;
    private String matricula;
    private int idLaboratorio;
    private String laboratorio;
    private int idEquipo;
    private String equipoClave;
    private LocalTime horaPrestamo;
    private LocalTime horaDevolucion;
    private String observaciones;

    public PrestamoEquipo() {
    }

    public int getIdPrestamo() {
        return idPrestamo;
    }

    public void setIdPrestamo(int idPrestamo) {
        this.idPrestamo = idPrestamo;
    }

    public String getMatricula() {
        return matricula;
    }

    public void setMatricula(String matricula) {
        this.matricula = matricula;
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

    public LocalTime getHoraPrestamo() {
        return horaPrestamo;
    }

    public void setHoraPrestamo(LocalTime horaPrestamo) {
        this.horaPrestamo = horaPrestamo;
    }

    public LocalTime getHoraDevolucion() {
        return horaDevolucion;
    }

    public void setHoraDevolucion(LocalTime horaDevolucion) {
        this.horaDevolucion = horaDevolucion;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    //=========================================
    // VALIDA SI EL EQUIPO SIGUE DISPONIBLE JUSTO ANTES DE GUARDAR
    // (evita que dos alumnos se ganen el mismo equipo)
    //=========================================
    private boolean equipoDisponible(Connection con, int idEquipo) throws Exception {

        String sql = """
                SELECT e.estatus,
                       (SELECT COUNT(*) FROM prestamo_equipo p
                         WHERE p.idEquipo = e.idEquipo
                           AND p.fecha = CURDATE()
                           AND p.horaDevolucion IS NULL) AS prestado
                FROM equipo_computo e
                WHERE e.idEquipo = ?
                """;

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setInt(1, idEquipo);
        ResultSet rs = ps.executeQuery();

        boolean disponible = false;

        if (rs.next()) {
            disponible = "Disponible".equalsIgnoreCase(rs.getString("estatus"))
                    && rs.getInt("prestado") == 0;
        }

        rs.close();
        ps.close();

        return disponible;
    }

    //=========================================
    // INSERTAR PRÉSTAMO (valida disponibilidad del equipo)
    // Devuelve: 1 = OK, 0 = equipo ya no disponible, -1 = error
    //=========================================
    public int insertarPrestamo(PrestamoEquipo p) {

        int resultado = -1;

        try {

            Connection con = Conexion.conectar();
            con.setAutoCommit(false);

            try {

                if (!equipoDisponible(con, p.getIdEquipo())) {
                    con.rollback();
                    con.close();
                    return 0;
                }

                String sql = """
                        INSERT INTO prestamo_equipo
                            (matricula, idLaboratorio, idEquipo, fecha, horaPrestamo, horaDevolucion, observaciones)
                        VALUES (?, ?, ?, CURDATE(), ?, ?, ?)
                        """;

                PreparedStatement ps = con.prepareStatement(sql);

                ps.setString(1, p.getMatricula());
                ps.setInt(2, p.getIdLaboratorio());
                ps.setInt(3, p.getIdEquipo());
                ps.setTime(4, Time.valueOf(p.getHoraPrestamo()));

                if (p.getHoraDevolucion() != null) {
                    ps.setTime(5, Time.valueOf(p.getHoraDevolucion()));
                } else {
                    ps.setNull(5, java.sql.Types.TIME);
                }

                ps.setString(6, p.getObservaciones());

                int filas = ps.executeUpdate();
                ps.close();

                if (filas > 0) {
                    // Marca el equipo como prestado mientras no se registre devolución
                    if (p.getHoraDevolucion() == null) {
                        PreparedStatement psEq = con.prepareStatement(
                                "UPDATE equipo_computo SET estatus = 'Prestado' WHERE idEquipo = ?");
                        psEq.setInt(1, p.getIdEquipo());
                        psEq.executeUpdate();
                        psEq.close();
                    }
                    con.commit();
                    resultado = 1;
                } else {
                    con.rollback();
                    resultado = -1;
                }

            } catch (Exception ex) {
                con.rollback();
                throw ex;
            } finally {
                con.setAutoCommit(true);
                con.close();
            }

        } catch (Exception e) {

            e.printStackTrace();
            resultado = -1;

        }

        return resultado;
    }

    //=========================================
    // REGISTRAR DEVOLUCIÓN (libera el equipo)
    //=========================================
    public boolean registrarDevolucion(int idPrestamo, LocalTime horaDevolucion) {

        boolean respuesta = false;

        try {

            Connection con = Conexion.conectar();
            con.setAutoCommit(false);

            try {
                PreparedStatement ps = con.prepareStatement(
                        "UPDATE prestamo_equipo SET horaDevolucion = ? WHERE idPrestamo = ?");
                ps.setTime(1, Time.valueOf(horaDevolucion));
                ps.setInt(2, idPrestamo);
                int filas = ps.executeUpdate();
                ps.close();

                if (filas > 0) {
                    PreparedStatement psEq = con.prepareStatement("""
                            UPDATE equipo_computo e
                            INNER JOIN prestamo_equipo p ON p.idEquipo = e.idEquipo
                            SET e.estatus = 'Disponible'
                            WHERE p.idPrestamo = ?
                            """);
                    psEq.setInt(1, idPrestamo);
                    psEq.executeUpdate();
                    psEq.close();
                }

                con.commit();
                respuesta = filas > 0;

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
