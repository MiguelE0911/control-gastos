package com.miguel.gastos.controller;

import com.miguel.gastos.dao.GastoDAO;
import com.miguel.gastos.dao.GastoDAOImpl;
import com.miguel.gastos.model.*;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class FormularioController implements Initializable {
    @FXML private ComboBox<String> cmbTipo;
    @FXML private TextField txtDescripcion;
    @FXML private TextField txtMonto;
    @FXML private DatePicker dateFecha;

    // Paneles dinámicos
    @FXML private VBox panelFijo;
    @FXML private VBox panelVariable;
    @FXML private VBox panelOcio;

    // Campos específicos
    @FXML private TextField txtDiaPago;
    @FXML private ComboBox<String> cmbSubcategoria;
    @FXML private CheckBox chkRecurrente;
    @FXML private Label lblTitulo;

    private final GastoDAO dao = new GastoDAOImpl();
    private Gasto gastoEditando = null;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Cargar tipos de gasto
        cmbTipo.setItems(FXCollections.observableArrayList(
                "FIJO", "VARIABLE", "OCIO"
        ));
        // Cargar subcategorías para GastoVariable
        cmbSubcategoria.setItems(FXCollections.observableArrayList(
                "Alimentación", "Transporte", "Salud",
                "Educación", "Ropa", "Tecnología", "Otro"
        ));
        // Fecha por defecto: hoy
        dateFecha.setValue(LocalDate.now());
    }

    @FXML
    private void onTipoSeleccionado() {
        String tipo = cmbTipo.getValue();
        if (tipo == null) return;
        // Ocultar todos los paneles primero
        panelFijo.setVisible(false);     panelFijo.setManaged(false);
        panelVariable.setVisible(false); panelVariable.setManaged(false);
        panelOcio.setVisible(false);     panelOcio.setManaged(false);
        // Mostrar solo el panel correspondiente
        switch (tipo) {
            case "FIJO"     -> { panelFijo.setVisible(true);     panelFijo.setManaged(true); }
            case "VARIABLE" -> { panelVariable.setVisible(true); panelVariable.setManaged(true); }
            case "OCIO"     -> { panelOcio.setVisible(true);     panelOcio.setManaged(true); }
        }
    }

    @FXML
    private void guardar() {
        if (!validarCampos()) return;
        String tipo        = cmbTipo.getValue();
        String descripcion = txtDescripcion.getText().trim();
        double monto       = Double.parseDouble(txtMonto.getText().trim());
        LocalDate fecha    = dateFecha.getValue();

        if (gastoEditando == null) {
            // MODO CREAR
            Gasto gasto = construirGasto(tipo, descripcion, monto, fecha);
            if (gasto != null) dao.insertar(gasto);

        } else {
            // MODO EDITAR: actualizar los campos del objeto existente
            gastoEditando.setDescripcion(descripcion);
            gastoEditando.setMonto(monto);
            gastoEditando.setFecha(fecha);
            if (gastoEditando instanceof GastoFijo gf) {
                gf.setDiaPago(Integer.parseInt(txtDiaPago.getText().trim()));
            } else if (gastoEditando instanceof GastoVariable gv) {
                gv.setSubcategoria(cmbSubcategoria.getValue());
            } else if (gastoEditando instanceof GastoOcio go) {
                go.setEsRecurrente(chkRecurrente.isSelected());
            }
            dao.actualizar(gastoEditando);
        }
        cerrarVentana();
    }

    @FXML
    private void cancelar() {
        cerrarVentana();
    }

    private boolean validarCampos() {
        // Tipo
        if (cmbTipo.getValue() == null) {
            mostrarError("Selecciona el tipo de gasto.");
            return false;
        }
        // Descripción
        if (txtDescripcion.getText().trim().isEmpty()) {
            mostrarError("La descripción no puede estar vacía.");
            return false;
        }
        // Monto
        try {
            double monto = Double.parseDouble(txtMonto.getText().trim());
            if (monto <= 0) throw new NumberFormatException();
        } catch (NumberFormatException e) {
            mostrarError("El monto debe ser un número mayor a 0.");
            return false;
        }
        // Fecha
        if (dateFecha.getValue() == null) {
            mostrarError("Selecciona una fecha.");
            return false;
        }
        // Campo específico según tipo
        String tipo = cmbTipo.getValue();
        if (tipo.equals("FIJO")) {
            try {
                int dia = Integer.parseInt(txtDiaPago.getText().trim());
                if (dia < 1 || dia > 31) throw new NumberFormatException();
            } catch (NumberFormatException e) {
                mostrarError("El día de pago debe ser un número entre 1 y 31.");
                return false;
            }
        }
        if (tipo.equals("VARIABLE") && cmbSubcategoria.getValue() == null) {
            mostrarError("Selecciona una subcategoría.");
            return false;
        }
        return true;
    }

    public void cargarGasto(Gasto gasto) {
        this.gastoEditando = gasto;
        lblTitulo.setText("Editar Gasto");
        txtDescripcion.getScene(); // ya está inicializado cuando se llama esto

        // Precargar campos comunes
        cmbTipo.setValue(gasto.getTipoGasto());
        txtDescripcion.setText(gasto.getDescripcion());
        txtMonto.setText(String.valueOf(gasto.getMonto()));
        dateFecha.setValue(gasto.getFecha());

        // Disparar el evento para mostrar el panel correcto
        onTipoSeleccionado();
        // Precargar campo específico según tipo
        if (gasto instanceof GastoFijo gf) {
            txtDiaPago.setText(String.valueOf(gf.getDiaPago()));

        } else if (gasto instanceof GastoVariable gv) {
            cmbSubcategoria.setValue(gv.getSubcategoria());

        } else if (gasto instanceof GastoOcio go) {
            chkRecurrente.setSelected(go.getEsRecurrente());
        }
    }

    private Gasto construirGasto(String tipo, String descripcion,
                                 double monto, LocalDate fecha) {
        return switch (tipo) {
            case "FIJO" -> new GastoFijo(descripcion, monto, fecha,
                    Integer.parseInt(txtDiaPago.getText().trim()));
            case "VARIABLE" -> new GastoVariable(descripcion, monto, fecha,
                    cmbSubcategoria.getValue());
            case "OCIO" -> new GastoOcio(descripcion, monto, fecha,
                    chkRecurrente.isSelected());
            default -> null;
        };
    }

    private void mostrarError(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.setTitle("Campo requerido");
        alert.showAndWait();
    }

    private void cerrarVentana() {
        Stage stage = (Stage) txtDescripcion.getScene().getWindow();
        stage.close();
    }
}