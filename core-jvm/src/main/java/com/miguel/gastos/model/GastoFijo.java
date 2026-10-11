package com.miguel.gastos.model;
import java.time.LocalDate;

public class GastoFijo extends Gasto{
    private int diaPago;

    public GastoFijo(String descripcion, double monto, LocalDate fecha, int diaPago) {
        super(descripcion, monto, fecha);
        this.diaPago = diaPago;
    }

    public int getDiaPago() {return diaPago;}
    public void setDiaPago(int diaPago) {this.diaPago = diaPago;}

    @Override
    public String getTipoGasto() {
        return "FIJO";
    }
}