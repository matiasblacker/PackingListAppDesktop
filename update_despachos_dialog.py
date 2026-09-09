with open("src/main/java/com/logistics/packinglist/ui/DespachosDialog.java", "r") as f:
    content = f.read()

# Update inputs styling to 11px compact
content = content.replace(
    'txtGuia.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtGuia.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'txtFactura.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtFactura.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'txtTransportista.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtTransportista.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'txtPatente.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtPatente.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'txtComentario.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtComentario.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

# Update labels styling to 11px
content = content.replace('Label lblNP = new Label("Nota Pedido:");\n        lblNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblNP = new Label("Nota Pedido:");\n        lblNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblGuia = new Label("Guía Despacho:");\n        lblGuia.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblGuia = new Label("Guía Despacho:");\n        lblGuia.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblFactura = new Label("Factura:");\n        lblFactura.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblFactura = new Label("Factura:");\n        lblFactura.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblTrans = new Label("Chofer / Transp:");\n        lblTrans.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblTrans = new Label("Chofer / Transp:");\n        lblTrans.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblPat = new Label("Patente:");\n        lblPat.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblPat = new Label("Patente:");\n        lblPat.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblComentario = new Label("Comentario:");\n        lblComentario.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblComentario = new Label("Comentario:");\n        lblComentario.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')

# Move action buttons to footer
old_right_pane_setup = """        rightPane.getChildren().addAll(lblFormHeader, formGrid, lblItemsHeader, detailBox, actionRow);

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit);"""

new_right_pane_setup = """        rightPane.getChildren().addAll(lblFormHeader, formGrid, lblItemsHeader, detailBox);

        actionRow.setAlignment(Pos.CENTER_RIGHT);
        actionRow.setPadding(new Insets(15, 0, 0, 0));

        ScrollPane rightScroll = new ScrollPane(rightPane);
        rightScroll.setFitToWidth(true);
        rightScroll.setFitToHeight(true);
        rightScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        rightScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        rightScroll.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);

        SplitPane mainSplit = new SplitPane();
        mainSplit.setStyle("-fx-background-color: transparent; -fx-box-border: transparent;");
        mainSplit.getItems().addAll(leftPane, rightScroll);
        mainSplit.setDividerPositions(0.5);
        VBox.setVgrow(mainSplit, Priority.ALWAYS);

        root.getChildren().addAll(headerBox, mainSplit, actionRow);"""

content = content.replace(old_right_pane_setup, new_right_pane_setup)

with open("src/main/java/com/logistics/packinglist/ui/DespachosDialog.java", "w") as f:
    f.write(content)

print("Updated DespachosDialog.java successfully!")
