with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

target = "pdfService.generarPdfAnuncio(announcement, supplier, warehouse, compObj, productMap, fieldDefs, file);"
replacement = "pdfService.exportar(announcement, compObj, supplier, warehouse, productMap, null, null, fieldDefs, file);"

content = content.replace(target, replacement)
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated descargarPDF method call in RecepcionesDialog.java")
