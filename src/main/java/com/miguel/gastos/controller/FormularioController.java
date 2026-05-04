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

    private final GastoDAO dao = new GastoDAOImpl();

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
        // Validaciones
        if (!validarCampos()) return;

        String tipo        = cmbTipo.getValue();
        String descripcion = txtDescripcion.getText().trim();
        double monto       = Double.parseDouble(txtMonto.getText().trim());
        LocalDate fecha    = dateFecha.getValue();

        // Crear el objeto correcto según el tipo
        Gasto gasto = switch (tipo) {
            case "FIJO" -> {
                int diaPago = Integer.parseInt(txtDiaPago.getText().trim());
                yield new GastoFijo(descripcion, monto, fecha, diaPago);
            }
            case "VARIABLE" -> {
                String sub = cmbSubcategoria.getValue();
                yield new GastoVariable(descripcion, monto, fecha, sub);
            }
            case "OCIO" -> {
                boolean recurrente = chkRecurrente.isSelected();
                yield new GastoOcio(descripcion, monto, fecha, recurrente);
            }
            default -> null;
        };

        if (gasto != null) {
            dao.insertar(gasto);
            cerrarVentana();
        }
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