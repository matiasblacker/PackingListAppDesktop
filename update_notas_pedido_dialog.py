with open("src/main/java/com/logistics/packinglist/ui/NotasPedidoDialog.java", "r") as f:
    content = f.read()

# Replace labels style in formGrid to include -fx-font-size: 11px;
content = content.replace('Label lblBodega = new Label("Bodega:");\n        lblBodega.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblBodega = new Label("Bodega:");\n        lblBodega.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblFolioNP = new Label("Folio NP:");\n        lblFolioNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblFolioNP = new Label("Folio NP:");\n        lblFolioNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblClienteNP = new Label("Cliente:");\n        lblClienteNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblClienteNP = new Label("Cliente:");\n        lblClienteNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblEstadoNP = new Label("Estado:");\n        lblEstadoNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblEstadoNP = new Label("Estado:");\n        lblEstadoNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblZonaNP = new Label("Zona Reserva:");\n        lblZonaNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblZonaNP = new Label("Zona Reserva:");\n        lblZonaNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblProvNP = new Label("Proveedor:");\n        lblProvNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblProvNP = new Label("Proveedor:");\n        lblProvNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')
content = content.replace('Label lblOCNP = new Label("N° Orden Compra:");\n        lblOCNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold;");', 'Label lblOCNP = new Label("N° Orden Compra:");\n        lblOCNP.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-font-size: 11px;");')

# Replace input styles to include -fx-font-size: 11px;
content = content.replace(
    'txtFolio.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9; -fx-text-fill: #0f172a;");',
    'txtFolio.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-background-color: #f1f5f9; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'txtOrdenCompra.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");',
    'txtOrdenCompra.setStyle("-fx-font-size: 11px; -fx-padding: 3px 6px; -fx-background-radius: 4px; -fx-border-radius: 4px; -fx-border-color: #cbd5e1; -fx-text-fill: #0f172a;");'
)

content = content.replace(
    'cbBodega.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbBodega.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");'
)

content = content.replace(
    'cbCliente.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbCliente.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");'
)

content = content.replace(
    'cbEstado.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbEstado.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");'
)

content = content.replace(
    'cbZonaReserva.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbZonaReserva.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");'
)

content = content.replace(
    'cbProveedor.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px;");',
    'cbProveedor.setStyle("-fx-font-size: 11px; -fx-background-radius: 4px;");'
)

with open("src/main/java/com/logistics/packinglist/ui/NotasPedidoDialog.java", "w") as f:
    f.write(content)

print("Updated NotasPedidoDialog.java font size successfully!")
