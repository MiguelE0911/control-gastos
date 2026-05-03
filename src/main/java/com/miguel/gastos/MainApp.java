package com.miguel.gastos;
import com.miguel.gastos.dao.GastoDAO;
import com.miguel.gastos.dao.GastoDAOImpl;
import com.miguel.gastos.model.Gasto;
import java.util.List;

public class MainApp {
        public static void main(String[] args) {
            GastoDAO dao = new GastoDAOImpl();
            List<Gasto> gastos = dao.listarTodos();
            gastos.forEach(System.out::println);
        }
    }