package com.miguel.gastos.model;
import java.time.LocalDate;

public class GastoVariable extends Gasto {
    private String subcategoria;

    public GastoVariable(String descripcion, double monto, LocalDate fecha, String subcategoria) {
        super(descripcion, monto, fecha);
        this.subcategoria = subcategoria;
    }

    public String getSubcategoria() {return subcategoria;}
    public void setSubcategoria(String subcategoria) {this.subcategoria = subcategoria;}

    @Override
    public String getTipoGasto() {
        return "Lonche, materiales universidad";
    }
}
