package com.miguel.gastos.controller;

import com.miguel.gastos.service.ReporteService;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;

public class ReportesController implements Initializable {

    @FXML private DatePicker dateDesde;
    @FXML private DatePicker dateHasta;
    @FXML private Label lblEstado;

    private final ReporteService reporteService = new ReporteService();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // Fechas por defecto: primer y último día del mes actual
        LocalDate hoy = LocalDate.now();
        dateDesde.setValue(hoy.withDayOfMonth(1));
        dateHasta.setValue(hoy);
    }

    @FXML
    private void generarResumenGeneral() {
        File archivo = elegirArchivoDestino("resumen_general.pdf");
        if (archivo == null) return;

        try {
            reporteService.generarResumenGeneral(archivo.getAbsolutePath());
            mostrarExito("Reporte generado: " + archivo.getAbsolutePath());
            abrirPDF(archivo);
        } catch (Exception e) {
            mostrarError("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void generarReportePeriodo() {
        if (dateDesde.getValue() == null || dateHasta.getValue() == null) {
            mostrarError("Selecciona ambas fechas.");
            return;
        }
        if (dateDesde.getValue().isAfter(dateHasta.getValue())) {
            mostrarError("La fecha 'Desde' no puede ser mayor que 'Hasta'.");
            return;
        }

        File archivo = elegirArchivoDestino("reporte_periodo.pdf");
        if (archivo == null) return;

        try {
            reporteService.generarReportePeriodo(
                    dateDesde.getValue(),
                    dateHasta.getValue(),
                    archivo.getAbsolutePath());
            mostrarExito("Reporte generado: " + archivo.getAbsolutePath());
            abrirPDF(archivo);
        } catch (Exception e) {
            mostrarError("Error al generar el reporte: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // Abre un FileChooser para que el usuario elija dónde guardar el PDF
    private File elegirArchivoDestino(String nombreSugerido) {
        FileChooser chooser = new FileChooser();
        chooser.setTitle("Guardar reporte como...");
        chooser.setInitialFileName(nombreSugerido);
        chooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("PDF", "*.pdf"));

        Stage stage = (Stage) lblEstado.getScene().getWindow();
        return chooser.showSaveDialog(stage);
    }

    // Abre el PDF automáticamente con el visor del sistema
    private void abrirPDF(File archivo) {
        try {
            java.awt.Desktop.getDesktop().open(archivo);
        } catch (Exception e) {
            // Si no puede abrirlo automáticamente, no pasa nada
        }
    }

    private void mostrarExito(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: #a6e3a1; -fx-font-size: 12px;");
        lblEstado.setText("✅ " + mensaje);
    }

    private void mostrarError(String mensaje) {
        lblEstado.setStyle("-fx-text-fill: #f38ba8; -fx-font-size: 12px;");
        lblEstado.setText("❌ " + mensaje);
    }
}