with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

target = """        itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);

        // Botones de acción inferior idénticos a Anuncios"""

replacement = """        itemsBox.getChildren().addAll(lblItems, tblItems, itemModifyGrid);

        rightPane.getChildren().addAll(formHeaderBox, formGrid, customFieldsPane, itemsBox);

        // Botones de acción inferior idénticos a Anuncios"""

if target in content:
    content = content.replace(target, replacement)
    with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
        f.write(content)
    print("Successfully added rightPane.getChildren().addAll(...)!")
else:
    print("Target not found!")
