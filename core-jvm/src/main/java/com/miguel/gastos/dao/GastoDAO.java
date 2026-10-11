package com.miguel.gastos.dao;
import com.miguel.gastos.model.Gasto;
import java.util.List;

public interface GastoDAO {

    void insertar(Gasto gasto);
    Gasto buscarPorId(int id);
    List<Gasto> listarTodos();
    void actualizar(Gasto gasto);
    void eliminar(int id);
}