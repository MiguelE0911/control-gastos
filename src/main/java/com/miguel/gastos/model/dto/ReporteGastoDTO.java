package com.miguel.gastos.model.dto;

public class ReporteGastoDTO {

    private String tipo;
    private String descripcion;
    private Double monto;
    private String fecha;
    private String detalle;

    public ReporteGastoDTO(String tipo, String descripcion,
                           Double monto, String fecha, String detalle) {
        this.tipo        = tipo;
        this.descripcion = descripcion;
        this.monto       = monto;
        this.fecha       = fecha;
        this.detalle     = detalle;
    }

    // Getters
    public String getTipo()        { return tipo; }
    public String getDescripcion() { return descripcion; }
    public Double getMonto()       { return monto; }
    public String getFecha()       { return fecha; }
    public String getDetalle()     { return detalle; }
}
