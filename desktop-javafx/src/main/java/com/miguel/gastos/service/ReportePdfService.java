package com.miguel.gastos.service;

import com.miguel.gastos.data.GastoRepository;
import com.miguel.gastos.data.GastoRepositoryFactory;
import com.miguel.gastos.model.Gasto;
import com.miguel.gastos.model.dto.ReporteGastoDTO;
import com.miguel.gastos.util.ConversorFechas;
import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportePdfService {
    private static final DateTimeFormatter FORMATTER_DD_MM_YYYY = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final GastoRepository repository = GastoRepositoryFactory.get();
    private final ReporteCalculator calculator = new ReporteCalculator();

    public void generarResumenGeneral(String rutaDestino) throws JRException {
        List<Gasto> gastos = repository.listarTodos();
        List<ReporteGastoDTO> datos = calculator.convertirADTO(gastos);

        double totalFijos = calculator.totalPorTipo(gastos, GastoTipo.FIJO);
        double totalVariables = calculator.totalPorTipo(gastos, GastoTipo.VARIABLE);
        double totalOcio = calculator.totalPorTipo(gastos, GastoTipo.OCIO);
        double totalGeneral = calculator.totalGeneral(gastos);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("totalFijos", totalFijos);
        parametros.put("totalVariables", totalVariables);
        parametros.put("totalOcio", totalOcio);
        parametros.put("totalGeneral", totalGeneral);
        parametros.put("fechaReporte", LocalDate.now().format(FORMATTER_DD_MM_YYYY));

        generarPDF("/reports/resumen_general.jrxml", datos, parametros, rutaDestino);
    }

    public void generarReportePeriodo(LocalDate desde, LocalDate hasta, String rutaDestino) throws JRException {
        List<Gasto> gastos = repository.listarTodos();
        List<Gasto> filtrados = gastos.stream()
                .filter(g -> {
                    java.time.LocalDate f = ConversorFechas.aJava(g.getFecha());
                    return !f.isBefore(desde) && !f.isAfter(hasta);
                })
                .collect(Collectors.toList());

        List<ReporteGastoDTO> datos = calculator.convertirADTO(filtrados);
        double totalPeriodo = datos.stream().mapToDouble(ReporteGastoDTO::getMonto).sum();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("fechaDesde", desde.format(FORMATTER_DD_MM_YYYY));
        parametros.put("fechaHasta", hasta.format(FORMATTER_DD_MM_YYYY));
        parametros.put("totalPeriodo", totalPeriodo);
        parametros.put("fechaReporte", LocalDate.now().format(FORMATTER_DD_MM_YYYY));

        generarPDF("/reports/reporte_periodo.jrxml", datos, parametros, rutaDestino);
    }

    private void generarPDF(String plantillaPath,
                            List<ReporteGastoDTO> datos,
                            Map<String, Object> parametros,
                            String rutaDestino) throws JRException {
        try (InputStream plantilla = getClass().getResourceAsStream(plantillaPath)) {
            JasperReport jasperReport = JasperCompileManager.compileReport(plantilla);
            JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(datos);
            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parametros, dataSource);
            JasperExportManager.exportReportToPdfFile(jasperPrint, rutaDestino);
        } catch (IOException e) {
            throw new JRException("No se pudo leer la plantilla JasperReports: " + e.getMessage(), e);
        }
    }
}
