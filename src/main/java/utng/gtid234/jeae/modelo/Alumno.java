package utng.gtid234.jeae.modelo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import utng.gtid234.jeae.conexiones.Conexion;

public class Alumno {

    private String matricula;
    private String nombre;
    private String grupo;
    private int idGrupo;
    private String ultimoError;

    public Alumno() {
    }

    // Mensaje real de la última operación fallida (para mostrarlo al usuario
    // en vez de solo un mensaje genérico y ocultar la causa en la consola).
    public String getUltimoError() {
        return ultimoError;
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

    public int getIdGrupo() {
        return idGrupo;
    }

    public void setIdGrupo(int idGrupo) {
        this.idGrupo = idGrupo;
    }
//===BUSCAR ALUMNO POR MATRICULA

    public Alumno buscarAlumno(String matricula) {

        Alumno alumno = null;

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT
                        a.matricula,
                        a.nombre,
                        g.nombre AS grupo
                    FROM alumno a
                    INNER JOIN grupo g
                        ON a.idGrupo = g.idGrupo
                    WHERE a.matricula = ?
                    """;

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

    public java.util.List<Alumno> obtenerPorGrupo(int idGrupo) {

        java.util.List<Alumno> lista = new java.util.ArrayList<>();

        try {

            Connection con = Conexion.conectar();

            String sql = """
                    SELECT a.matricula, a.nombre, a.idGrupo, g.nombre AS grupo
                    FROM alumno a
                    INNER JOIN grupo g ON a.idGrupo = g.idGrupo
                    WHERE a.idGrupo = ?
                    ORDER BY a.nombre
                    """;

            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, idGrupo);

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {

                Alumno a = new Alumno();
                a.setMatricula(rs.getString("matricula"));
                a.setNombre(rs.getString("nombre"));
                a.setIdGrupo(rs.getInt("idGrupo"));
                a.setGrupo(rs.getString("grupo"));
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

    
    //  AGREGAR ALUMNO
    public boolean agregar(Alumno a) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "INSERT INTO alumno (matricula, nombre, idGrupo) VALUES (?, ?, ?)";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, a.getMatricula());
            ps.setString(2, a.getNombre());
            ps.setInt(3, a.getIdGrupo());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;

    }

    
    //  EDITAR ALUMNO (nombre y/o grupo; la matrícula es la llave)
    
    public boolean editar(Alumno a) {

        boolean respuesta = false;
        ultimoError = null;

        try {

            Connection con = Conexion.conectar();

            String sql = "UPDATE alumno SET nombre = ?, idGrupo = ? WHERE matricula = ?";

            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, a.getNombre());
            ps.setInt(2, a.getIdGrupo());
            ps.setString(3, a.getMatricula());

            respuesta = ps.executeUpdate() > 0;

            ps.close();
            con.close();

        } catch (Exception e) {

            ultimoError = e.getMessage();
            e.printStackTrace();

        }

        return respuesta;

    }


    // Borra primero todo lo que depende del alumno (incidencias, préstamos
    // de equipo y registros extraclase) y al final el propio alumno, todo
    // dentro de una misma transacción: si algo falla, no se elimina nada.
    public boolean eliminar(String matricula) {

        boolean respuesta = false;
        ultimoError = null;

        Connection con = null;

        try {

            con = Conexion.conectar();
            con.setAutoCommit(false);

            ejecutarDelete(con, "DELETE FROM incidencia WHERE matricula = ?", matricula);
            ejecutarDelete(con, "DELETE FROM prestamo_equipo WHERE matricula = ?", matricula);
            ejecutarDelete(con, "DELETE FROM registro WHERE matricula = ?", matricula);

            int filas = ejecutarDelete(con, "DELETE FROM alumno WHERE matricula = ?", matricula);

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

    // Auxiliar para ejecutar un DELETE parametrizado con una sola matrícula
    // dentro de la conexión/transcación indicada.
    private int ejecutarDelete(Connection con, String sql, String matricula) throws Exception {

        PreparedStatement ps = con.prepareStatement(sql);
        ps.setString(1, matricula);
        int filas = ps.executeUpdate();
        ps.close();
        return filas;

    }

}