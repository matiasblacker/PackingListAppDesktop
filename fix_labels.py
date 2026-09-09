import re

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "r") as f:
    content = f.read()

old_labels = """        infoGrid.add(new Label("Folio BO:"), 0, 0);
        infoGrid.add(lblFolio, 1, 0);
        infoGrid.add(new Label("Cliente:"), 2, 0);
        infoGrid.add(lblCliente, 3, 0);

        infoGrid.add(new Label("Bodega:"), 0, 1);
        infoGrid.add(lblBodega, 1, 1);
        infoGrid.add(new Label("Zona Reserva:"), 2, 1);
        infoGrid.add(lblZonaReserva, 3, 1);

        infoGrid.add(new Label("Fecha:"), 0, 2);
        infoGrid.add(lblFecha, 1, 2);
        infoGrid.add(new Label("Estado BO:"), 2, 2);
        infoGrid.add(lblEstado, 3, 2);"""

new_labels = """        Label l1 = new Label("Folio BO:"); l1.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l2 = new Label("Cliente:"); l2.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l1, 0, 0);
        infoGrid.add(lblFolio, 1, 0);
        infoGrid.add(l2, 2, 0);
        infoGrid.add(lblCliente, 3, 0);

        Label l3 = new Label("Bodega:"); l3.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l4 = new Label("Zona Reserva:"); l4.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l3, 0, 1);
        infoGrid.add(lblBodega, 1, 1);
        infoGrid.add(l4, 2, 1);
        infoGrid.add(lblZonaReserva, 3, 1);

        Label l5 = new Label("Fecha:"); l5.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        Label l6 = new Label("Estado BO:"); l6.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");
        infoGrid.add(l5, 0, 2);
        infoGrid.add(lblFecha, 1, 2);
        infoGrid.add(l6, 2, 2);
        infoGrid.add(lblEstado, 3, 2);"""

content = content.replace(old_labels, new_labels)

with open("src/main/java/com/logistics/packinglist/ui/BackOrdersDialog.java", "w") as f:
    f.write(content)
print("Updated labels")
