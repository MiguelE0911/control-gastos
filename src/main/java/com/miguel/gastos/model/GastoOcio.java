package com.miguel.gastos.model;
import java.time.LocalDate;

public class GastoOcio extends Gasto {
    private boolean esRecurrente;

    public GastoOcio(String descripcion, double monto, LocalDate fecha, boolean esRecurrente) {
        super(descripcion, monto, fecha);
        this.esRecurrente = esRecurrente;
    }
    public boolean getEsRecurrente() {return esRecurrente;}
    public void setEsRecurrente(boolean esRecurrente) {this.esRecurrente = esRecurrente;}

    @Override
    public String getTipoGasto() {
        return "Ocio";
    }
}