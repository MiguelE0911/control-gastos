package com.miguel.gastos.model;
import java.time.LocalDate;

public abstract class Gasto {
    private int id;
    private String descripcion;
    private double monto;
    private LocalDate fecha;
    private String categoria; // "FIJO", "VARIABLE", "OCIO", etc.

    // Constructor
    public Gasto(String descripcion, double monto, LocalDate fecha) {
        this.descripcion = descripcion;
        this.monto = monto;
        this.fecha = fecha;
    }

    public String getCategoria() {return categoria;}
    public void setCategoria(String categoria) {this.categoria = categoria;}

    public LocalDate getFecha() {return fecha;}
    public void setFecha(LocalDate fecha) {this.fecha = fecha;}

    public double getMonto() {return monto;}
    public void setMonto(double monto) {this.monto = monto;}

    public String getDescripcion() {return descripcion;}
    public void setDescripcion(String descripcion) {this.descripcion = descripcion;}

    public int getId() {return id;}
    public void setId(int id) {this.id = id;}

    public abstract String getTipoGasto();

    // toString() para debug
    @Override
    public String toString() {
        return String.format("[%s] %s - Q%.2f (%s)",
                getTipoGasto(), descripcion, monto, fecha);
    }
}