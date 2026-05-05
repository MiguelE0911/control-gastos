package com.miguel.gastos.controller;

import com.miguel.gastos.dao.GastoDAO;
import com.miguel.gastos.dao.GastoDAOImpl;
import com.miguel.gastos.model.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class MainController implements Initializable {

    // Conexión con los fx:id del FXML
    @FXML private Label lblTotal;
    @FXML private Label lblFijos;
    @FXML private Label lblVariables;
    @FXML private Label lblOcio;
    @FXML private TableView<Gasto> tablaGastos;
    @FXML private TableColumn<Gasto, String> colTipo;
    @FXML private TableColumn<Gasto, String> colDescripcion;
    @FXML private TableColumn<Gasto, Double> colMonto;
    @FXML private TableColumn<Gasto, String> colFecha;
    @FXML private TableColumn<Gasto, String> colDetalle;

    private final GastoDAO dao = new GastoDAOImpl();
    private ObservableList<Gasto> listaGastos;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        configurarColumnas();
        cargarGastos();
        tablaGastos.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();
                if (seleccionado != null) {
                    abrirFormularioEditar(seleccionado);
                }
            }
        });
    }

    private void configurarColumnas() {
        colTipo.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getTipoGasto()));

        colTipo.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String tipo, boolean empty) {
                super.updateItem(tipo, empty);
                if (empty || tipo == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(tipo);
                    String color = switch (tipo) {
                        case "Fijo"     -> "#89b4fa";
                        case "Variable" -> "#a6e3a1";
                        case "Ocio"     -> "#fab387";
                        default               -> "#cdd6f4";
                    };
                    setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                }
            }
        });

        colDescripcion.setCellValueFactory(
                new PropertyValueFactory<>("descripcion"));

        colMonto.setCellValueFactory(new PropertyValueFactory<>("monto"));
        colMonto.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double monto, boolean empty) {
                super.updateItem(monto, empty);
                if (empty || monto == null) {
                    setText(null);
                } else {
                    setText(String.format("$%.2f", monto));
                }
            }
        });

        colFecha.setCellValueFactory(data ->
                new SimpleStringProperty(
                        data.getValue().getFecha().toString()));

        // Columna detalle: muestra el atributo extra según el tipo
        colDetalle.setCellValueFactory(data -> {
            Gasto g = data.getValue();
            String detalle = switch (g) {
                case GastoFijo gf       -> "Día de pago: " + gf.getDiaPago();
                case GastoVariable gv   -> gv.getSubcategoria();
                case GastoOcio go       -> go.getEsRecurrente() ? "Recurrente" : "Única vez";
                default                 -> "";
            };
            return new SimpleStringProperty(detalle);
        });
    }

    private void cargarGastos() {
        List<Gasto> gastos = dao.listarTodos();
        listaGastos = FXCollections.observableArrayList(gastos);
        tablaGastos.setItems(listaGastos);
        actualizarTarjetas();
    }

    private void actualizarTarjetas() {
        double total     = listaGastos.stream().mapToDouble(Gasto::getMonto).sum();
        double fijos     = listaGastos.stream().filter(g -> g instanceof GastoFijo)
                .mapToDouble(Gasto::getMonto).sum();
        double variables = listaGastos.stream().filter(g -> g instanceof GastoVariable)
                .mapToDouble(Gasto::getMonto).sum();
        double ocio      = listaGastos.stream().filter(g -> g instanceof GastoOcio)
                .mapToDouble(Gasto::getMonto).sum();

        lblTotal.setText(String.format("$%.2f", total));
        lblFijos.setText(String.format("$%.2f", fijos));
        lblVariables.setText(String.format("$%.2f", variables));
        lblOcio.setText(String.format("$%.2f", ocio));
    }

    @FXML
    private void abrirFormularioNuevo() {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FormularioGasto.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nuevo Gasto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            // Al cerrar el formulario, recargamos la tabla
            cargarGastos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void abrirFormularioEditar(Gasto gasto) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/FormularioGasto.fxml"));
            Parent root = loader.load();

            // Pasar el gasto al controlador del formulario
            FormularioController controller = loader.getController();
            controller.cargarGasto(gasto);

            Stage stage = new Stage();
            stage.setTitle("Editar Gasto");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.showAndWait();

            cargarGastos();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void eliminarGasto() {
        Gasto seleccionado = tablaGastos.getSelectionModel().getSelectedItem();

        if (seleccionado == null) {
            mostrarAlerta("Selecciona un gasto de la tabla primero.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Eliminar \"" + seleccionado.getDescripcion() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                dao.eliminar(seleccionado.getId());
                cargarGastos();
            }
        });
    }

    private void mostrarAlerta(String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING, mensaje, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }
}