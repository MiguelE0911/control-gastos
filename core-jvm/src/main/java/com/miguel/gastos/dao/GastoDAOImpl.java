package com.miguel.gastos.dao;
import com.miguel.gastos.model.*;
import com.miguel.gastos.util.DatabaseConnection;
import java.sql.*;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class GastoDAOImpl implements GastoDAO {

    // INSERT
    @Override
    public void insertar(Gasto gasto) {
        String sql = """
                INSERT INTO gastos
                    (tipo, descripcion, monto, fecha, dia_pago, subcategoria, es_recurrente)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, gasto.getTipoGasto());
            ps.setString(2, gasto.getDescripcion());
            ps.setDouble(3, gasto.getMonto());
            ps.setDate  (4, Date.valueOf(gasto.getFecha()));
            // Cada subclase solo llena su columna, las demás quedan null
            if (gasto instanceof GastoFijo gf) {
                ps.setInt   (5, gf.getDiaPago());
                ps.setNull  (6, Types.VARCHAR);
                ps.setNull  (7, Types.BOOLEAN);
            } else if (gasto instanceof GastoVariable gv) {
                ps.setNull  (5, Types.INTEGER);
                ps.setString(6, gv.getSubcategoria());
                ps.setNull  (7, Types.BOOLEAN);
            } else if (gasto instanceof GastoOcio go) {
                ps.setNull  (5, Types.INTEGER);
                ps.setNull  (6, Types.VARCHAR);
                ps.setBoolean(7, go.getEsRecurrente());
            }
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // SELECT por ID
    @Override
    public Gasto buscarPorId(int id) {
        String sql = "SELECT * FROM gastos WHERE id = ?";

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return mapearGasto(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    // SELECT todos
    @Override
    public List<Gasto> listarTodos() {
        List<Gasto> lista = new ArrayList<>();
        String sql = "SELECT * FROM gastos ORDER BY fecha DESC";

        try (Statement st = DatabaseConnection.getConnection().createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                lista.add(mapearGasto(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return lista;
    }

    // UPDATE
    @Override
    public void actualizar(Gasto gasto) {
        String sql = """
                UPDATE gastos SET
                    descripcion   = ?,
                    monto         = ?,
                    fecha         = ?,
                    dia_pago      = ?,
                    subcategoria  = ?,
                    es_recurrente = ?
                WHERE id = ?
                """;

        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setString(1, gasto.getDescripcion());
            ps.setDouble(2, gasto.getMonto());
            ps.setDate  (3, Date.valueOf(gasto.getFecha()));
            if (gasto instanceof GastoFijo gf) {
                ps.setInt  (4, gf.getDiaPago());
                ps.setNull (5, Types.VARCHAR);
                ps.setNull (6, Types.BOOLEAN);
            } else if (gasto instanceof GastoVariable gv) {
                ps.setNull (4, Types.INTEGER);
                ps.setString(5, gv.getSubcategoria());
                ps.setNull (6, Types.BOOLEAN);
            } else if (gasto instanceof GastoOcio go) {
                ps.setNull (4, Types.INTEGER);
                ps.setNull (5, Types.VARCHAR);
                ps.setBoolean(6, go.getEsRecurrente());
            }
            ps.setInt(7, gasto.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // DELETE
    @Override
    public void eliminar(int id) {
        String sql = "DELETE FROM gastos WHERE id = ?";
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Acepta tanto fechas ISO ("2026-10-11") como epoch millis ("1791694800000"),
    // para poder leer bases de datos creadas por versiones anteriores.
    private LocalDate parseFecha(ResultSet rs) throws SQLException {
        Object valor = rs.getObject("fecha");
        if (valor == null) return null;

        if (valor instanceof Number n) {
            return Instant.ofEpochMilli(n.longValue())
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        if (valor instanceof java.sql.Date d) return d.toLocalDate();
        if (valor instanceof java.sql.Timestamp t) return t.toLocalDateTime().toLocalDate();

        String texto = valor.toString().trim();
        if (texto.matches("-?\\d+")) {
            return Instant.ofEpochMilli(Long.parseLong(texto))
                    .atZone(ZoneId.systemDefault()).toLocalDate();
        }
        // Formato ISO "YYYY-MM-DD" (o recortar la parte de hora si la hubiera)
        return LocalDate.parse(texto.length() > 10 ? valor.toString().substring(0, 10) : valor.toString());
    }

    // MAPPER: Convierte una fila del ResultSet en el objeto Java correcto
    private Gasto mapearGasto(ResultSet rs) throws SQLException {
        String tipo        = rs.getString("tipo");
        String descripcion = rs.getString("descripcion");
        double monto       = rs.getDouble("monto");
        LocalDate fecha    = parseFecha(rs);

        Gasto gasto = switch (tipo) {
            case "FIJO" -> {
                GastoFijo gf = new GastoFijo(descripcion, monto, fecha,
                        rs.getInt("dia_pago"));
                yield gf;
            }
            case "VARIABLE" -> {
                GastoVariable gv = new GastoVariable(descripcion, monto, fecha,
                        rs.getString("subcategoria"));
                yield gv;
            }
            case "OCIO" -> {
                GastoOcio go = new GastoOcio(descripcion, monto, fecha,
                        rs.getBoolean("es_recurrente"));
                yield go;
            }
            default -> throw new SQLException("Tipo de gasto desconocido: " + tipo);
        };
        gasto.setId(rs.getInt("id"));
        return gasto;
    }
}