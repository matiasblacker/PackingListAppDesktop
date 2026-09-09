package com.logistics.packinglist.ui;

import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.util.Callback;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.layout.HBox;
import javafx.scene.control.Label;
import javafx.geometry.Pos;

public class StatusColumnHelper {

    public static <S> void applyRowFactory(TableView<S> tableView) {
        applyRowFactory(tableView, null);
    }

    public static <S> void applyRowFactory(TableView<S> tableView, java.util.function.Consumer<TableRow<S>> customRowInitializer) {
        tableView.setRowFactory(tv -> {
            TableRow<S> row = new TableRow<>();
            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem != null) {
                    String status = getStatusOf(newItem);
                    if ("ANULADO".equalsIgnoreCase(status) || "ANULADA".equalsIgnoreCase(status)) {
                        row.setStyle("-fx-opacity: 0.65;");
                    } else {
                        row.setStyle("");
                    }
                } else {
                    row.setStyle("");
                }
            });
            if (customRowInitializer != null) {
                customRowInitializer.accept(row);
            }
            return row;
        });
    }

    public static <S, T> void applyStatusStyling(TableColumn<S, T> column, boolean isPrimaryFolioColumn) {
        column.setCellFactory(new Callback<TableColumn<S, T>, TableCell<S, T>>() {
            @Override
            public TableCell<S, T> call(TableColumn<S, T> param) {
                return new TableCell<S, T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                            setGraphic(null);
                            setStyle("");
                        } else {
                            TableRow<S> row = getTableRow();
                            S rowItem = (row != null) ? row.getItem() : null;
                            String status = getStatusOf(rowItem);
                            String valStr = item.toString();

                            if ("ANULADO".equalsIgnoreCase(status) || "ANULADA".equalsIgnoreCase(status)) {
                                Text textNode = new Text(valStr);
                                textNode.setStrikethrough(true);
                                textNode.setFill(Color.web("#94a3b8"));
                                setGraphic(textNode);
                                setText(null);
                                setStyle("-fx-text-fill: #94a3b8;");
                            } else if (isCompletedStatus(status)) {
                                HBox box = new HBox(5);
                                box.setAlignment(Pos.CENTER_LEFT);
                                Label icon = new Label("🟢 ✔");
                                icon.setStyle("-fx-text-fill: #22c55e; -fx-font-weight: bold;");
                                Label lbl = new Label(valStr);
                                lbl.setStyle("-fx-text-fill: #0f172a;");
                                box.getChildren().addAll(icon, lbl);
                                setGraphic(box);
                                setText(null);
                                setStyle("");
                            } else {
                                setGraphic(null);
                                setText(valStr);
                                setStyle("");
                            }
                        }
                    }
                };
            }
        });
    }

    private static String getStatusOf(Object item) {
        if (item == null) return null;
        try {
            return (String) item.getClass().getMethod("getEstado").invoke(item);
        } catch (Exception e) {
            return null;
        }
    }

    private static boolean isCompletedStatus(String status) {
        if (status == null) return false;
        String s = status.toUpperCase();
        return "COMPLETADO".equals(s) || 
               "DESPACHADO".equals(s) || 
               "RECIBIDO".equals(s) || 
               "PROCESADO".equals(s) || 
               "ENTREGADO".equals(s) || 
               "FINALIZADO".equals(s) ||
               "COMPLETADA".equals(s);
    }
}
