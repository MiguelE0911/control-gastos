package com.miguel.gastos.service;

import com.miguel.gastos.dao.GastoDAO;
import com.miguel.gastos.dao.GastoDAOImpl;
import com.miguel.gastos.model.*;
import com.miguel.gastos.model.dto.ReporteGastoDTO;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class ReporteService {

    private final GastoDAO dao = new GastoDAOImpl();
    private final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    //  Reporte 1: Resumen general por tipo
    public void generarResumenGeneral(String rutaDestino) throws JRException {
        List<Gasto> gastos = dao.listarTodos();
        List<ReporteGastoDTO> datos = convertirADTO(gastos);

        // Calcular totales por tipo para los parámetros del encabezado
        double totalFijos     = sumarPorTipo(gastos, GastoFijo.class);
        double totalVariables = sumarPorTipo(gastos, GastoVariable.class);
        double totalOcio      = sumarPorTipo(gastos, GastoOcio.class);
        double totalGeneral   = gastos.stream().mapToDouble(Gasto::getMonto).sum();

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("totalFijos",     totalFijos);
        parametros.put("totalVariables", totalVariables);
        parametros.put("totalOcio",      totalOcio);
        parametros.put("totalGeneral",   totalGeneral);
        parametros.put("fechaReporte",   LocalDate.now().format(formatter));

        generarPDF("/reports/resumen_general.jrxml", datos, parametros, rutaDestino);
    }

    //  Reporte 2: Gastos por período
    public void generarReportePeriodo(LocalDate desde, LocalDate hasta,
                                      String rutaDestino) throws JRException {
        List<Gasto> todos  = dao.listarTodos();

        // Filtrar por rango de fechas con streams
        List<Gasto> filtrados = todos.stream()
                .filter(g -> !g.getFecha().isBefore(desde)
                        && !g.getFecha().isAfter(hasta))
                .collect(Collectors.toList());

        List<ReporteGastoDTO> datos = convertirADTO(filtrados);

        Map<String, Object> parametros = new HashMap<>();
        parametros.put("fechaDesde",   desde.format(formatter));
        parametros.put("fechaHasta",   hasta.format(formatter));
        parametros.put("totalPeriodo", filtrados.stream()
                .mapToDouble(Gasto::getMonto)
                .sum());
        parametros.put("fechaReporte", LocalDate.now().format(formatter));

        generarPDF("/reports/reporte_periodo.jrxml", datos, parametros, rutaDestino);
    }

    //  Método central que genera el PDF
    private void generarPDF(String plantillaPath,
                            List<ReporteGastoDTO> datos,
                            Map<String, Object> parametros,
                            String rutaDestino) throws JRException {

        InputStream plantilla = getClass().getResourceAsStream(plantillaPath);

        JasperReport jasperReport = JasperCompileManager.compileReport(plantilla);
        JRBeanCollectionDataSource dataSource =
                new JRBeanCollectionDataSource(datos);

        JasperPrint jasperPrint = JasperFillManager.fillReport(
                jasperReport, parametros, dataSource);

        JasperExportManager.exportReportToPdfFile(jasperPrint, rutaDestino);
    }

    //  Helpers
    private List<ReporteGastoDTO> convertirADTO(List<Gasto> gastos) {
        return gastos.stream().map(g -> {
            String detalle = switch (g) {
                case GastoFijo gf     -> "Día de pago: " + gf.getDiaPago();
                case GastoVariable gv -> gv.getSubcategoria();
                case GastoOcio go     -> go.getEsRecurrente() ? "Recurrente" : "Única vez";
                default               -> "";
            };
            return new ReporteGastoDTO(
                    g.getTipoGasto(),
                    g.getDescripcion(),
                    g.getMonto(),
                    g.getFecha().format(formatter),
                    detalle
            );
        }).collect(Collectors.toList());
    }

    private <T> double sumarPorTipo(List<Gasto> gastos, Class<T> tipo) {
        return gastos.stream()
                .filter(tipo::isInstance)
                .mapToDouble(Gasto::getMonto)
                .sum();
    }
}