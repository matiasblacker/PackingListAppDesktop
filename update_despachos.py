import re

with open("src/main/java/com/logistics/packinglist/ui/DespachosDialog.java", "r") as f:
    content = f.read()

# Variables additions
content = content.replace("private TableView<DispatchModel> tablaDespachos;", 
"private TableView<DispatchModel> tablaDespachos;\n    private TableView<OrderNoteModel> tablaNPsConfirmadas;\n    private OrderNoteModel selectedNP = null;")
content = content.replace("private final ObservableList<OrderNoteDetailModel> itemsToDispatchList;",
"private final ObservableList<OrderNoteDetailModel> itemsToDispatchList;\n    private final javafx.collections.ObservableList<OrderNoteModel> npConfirmadasList;")
content = content.replace("this.itemsToDispatchList = FXCollections.observableArrayList();",
"this.itemsToDispatchList = FXCollections.observableArrayList();\n        this.npConfirmadasList = FXCollections.observableArrayList();")

# Remove cbNotaPedido
content = content.replace("private ComboBox<OrderNoteModel> cbNotaPedido;", "private Label lblNPSelected;")

# Left Pane TabPane
tab_pane_code = """
        TabPane leftTabPane = new TabPane();
        leftTabPane.setStyle("-fx-background-color: transparent;");

        // Tab 1: NPs Confirmadas
        Tab tabNPs = new Tab("NPs por Despachar");
        tabNPs.setClosable(false);
        VBox boxNPs = new VBox(10);
        boxNPs.setPadding(new Insets(10,0,0,0));
        
        tablaNPsConfirmadas = new TableView<>(npConfirmadasList);
        tablaNPsConfirmadas.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        tablaNPsConfirmadas.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");
        
        TableColumn<OrderNoteModel, String> colNpFolio = new TableColumn<>("Folio");
        colNpFolio.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFolio()));
        
        TableColumn<OrderNoteModel, String> colNpFecha = new TableColumn<>("Fecha");
        colNpFecha.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFecha()));
        
        tablaNPsConfirmadas.getColumns().addAll(colNpFolio, colNpFecha);
        VBox.setVgrow(tablaNPsConfirmadas, Priority.ALWAYS);
        boxNPs.getChildren().add(tablaNPsConfirmadas);
        tabNPs.setContent(boxNPs);

        // Tab 2: Historial de Despachos
        Tab tabHistorial = new Tab("Historial Despachos");
        tabHistorial.setClosable(false);
        VBox boxHistorial = new VBox(10);
        boxHistorial.setPadding(new Insets(10,0,0,0));
"""
content = content.replace("tablaDespachos = new TableView<>(filteredList);", tab_pane_code + "\n        tablaDespachos = new TableView<>(filteredList);")

content = content.replace("leftPane.getChildren().addAll(lblLeftTitle, tablaDespachos);",
"""
        boxHistorial.getChildren().add(tablaDespachos);
        tabHistorial.setContent(boxHistorial);
        leftTabPane.getTabs().addAll(tabNPs, tabHistorial);
        leftPane.getChildren().addAll(lblLeftTitle, leftTabPane);
""")

# Form grid (right)
content = re.sub(r'cbNotaPedido = new ComboBox<>\(\);.*?\}\);', 
"""lblNPSelected = new Label("Seleccione una NP de la izquierda");
        lblNPSelected.setStyle("-fx-font-weight: bold; -fx-text-fill: #2563eb;");
""", content, flags=re.DOTALL)

content = content.replace("formGrid.add(cbNotaPedido, 1, 0);", "formGrid.add(lblNPSelected, 1, 0);")
content = content.replace("cbNotaPedido.setDisable(!editable);", "")
content = content.replace("cbNotaPedido.getSelectionModel().clearSelection();", "lblNPSelected.setText(\"Seleccione una NP de la izquierda\");\n        selectedNP = null;")
content = content.replace("cbNotaPedido.getItems().clear();", "")
content = content.replace("cbNotaPedido.getItems().add(n);", "")
content = content.replace("OrderNoteModel note = cbNotaPedido.getValue();", "OrderNoteModel note = selectedNP;")

# Icons
content = content.replace('new FontAwesomeIconView(FontAwesomeIcon.TRUCK)', 'new FontAwesomeIconView(FontAwesomeIcon.SAVE)')
content = content.replace('new FontAwesomeIconView(FontAwesomeIcon.ERASER)', 'new FontAwesomeIconView(FontAwesomeIcon.UNDO)')

# Remove editable column
content = re.sub(r'TableColumn<OrderNoteDetailModel, Integer> colItemDes = new TableColumn<>\(.*?colItemDes\);', 
'tablaItems.getColumns().addAll(colItemProd, colItemPed);', content, flags=re.DOTALL)

content = content.replace('tablaItems.getColumns().addAll(colItemProd, colItemPed, colItemDes);', '')
content = content.replace('colItemPed.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidadPedida())));',
'colItemPed.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCantidadDespachada())));')
content = content.replace('colItemPed.setPrefWidth(120);', 'colItemPed.setPrefWidth(160);')
content = content.replace('"Cant. Efectiva a Despachar\\n(Sin BO)"', '"Cant. a Despachar"')

# Listeners
content = content.replace('// Listeners selección de tabla despacho',
"""
        tablaNPsConfirmadas.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                selectedDispatch = null;
                selectedNP = newSel;
                tablaDespachos.getSelectionModel().clearSelection();
                lblNPSelected.setText(newSel.getFolio());
                txtGuia.clear();
                txtFactura.clear();
                txtTransportista.clear();
                txtPatente.clear();
                txtComentario.clear();
                
                itemsToDispatchList.clear();
                if (newSel.getDetails() != null) {
                    for (OrderNoteDetailModel ond : newSel.getDetails()) {
                        int bo = ond.getCantidadBackOrder() != null ? ond.getCantidadBackOrder() : 0;
                        int efectiva = ond.getCantidadPedida() - bo;
                        int pendiente = efectiva - (ond.getCantidadDespachada() != null ? ond.getCantidadDespachada() : 0);
                        if (pendiente > 0) {
                            itemsToDispatchList.add(OrderNoteDetailModel.builder()
                                    .productId(ond.getProductId())
                                    .cantidadDespachada(pendiente) // Use this field for what we WILL dispatch
                                    .precioUnitario(ond.getPrecioUnitario())
                                    .build());
                        }
                    }
                }
                toggleEditingFields(true);
            }
        });

        // Listeners selección de tabla despacho
""")

content = content.replace('cbNotaPedido.setValue(noteMap.get(newSel.getOrderNoteId()));',
"""
                OrderNoteModel note = noteMap.get(newSel.getOrderNoteId());
                selectedNP = note;
                lblNPSelected.setText(note != null ? note.getFolio() : "-");
""")

content = content.replace('tablaDespachos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {',
"""tablaDespachos.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                tablaNPsConfirmadas.getSelectionModel().clearSelection();
""")

# Cargar Datos filtering
content = content.replace('observableList.addAll(despachos);',
"""observableList.addAll(despachos);
                    
                    npConfirmadasList.clear();
                    for (OrderNoteModel n : notas) {
                        if ("CONFIRMADA".equalsIgnoreCase(n.getEstado())) {
                            // Check if pending to dispatch
                            boolean pending = false;
                            if (n.getDetails() != null) {
                                for (OrderNoteDetailModel det : n.getDetails()) {
                                    int bo = det.getCantidadBackOrder() != null ? det.getCantidadBackOrder() : 0;
                                    int req = det.getCantidadPedida() != null ? det.getCantidadPedida() : 0;
                                    int efec = req - bo;
                                    int des = det.getCantidadDespachada() != null ? det.getCantidadDespachada() : 0;
                                    if (efec > des) { pending = true; break; }
                                }
                            }
                            if (pending) {
                                npConfirmadasList.add(n);
                            }
                        }
                    }
""")


with open("src/main/java/com/logistics/packinglist/ui/DespachosDialog.java", "w") as f:
    f.write(content)
