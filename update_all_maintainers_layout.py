import os
import re

files = [
    "src/main/java/com/logistics/packinglist/ui/EmpresasDialog.java",
    "src/main/java/com/logistics/packinglist/ui/BodegasDialog.java",
    "src/main/java/com/logistics/packinglist/ui/ProveedoresDialog.java",
    "src/main/java/com/logistics/packinglist/ui/ClientesDialog.java",
    "src/main/java/com/logistics/packinglist/ui/UbicacionesDialog.java",
    "src/main/java/com/logistics/packinglist/ui/UsuariosDialog.java",
    "src/main/java/com/logistics/packinglist/ui/CarriersDialog.java",
    "src/main/java/com/logistics/packinglist/ui/ProductosDialog.java"
]

for filepath in files:
    if not os.path.exists(filepath):
        print(f"File not found: {filepath}")
        continue

    with open(filepath, "r") as f:
        content = f.read()

    # Add Tooltip import if missing
    if "import javafx.scene.control.Tooltip;" not in content:
        content = content.replace("import javafx.scene.control.*;", "import javafx.scene.control.*;\nimport javafx.scene.control.Tooltip;")

    # 1. Update SplitPane orientation to HORIZONTAL and dividerPosition to 0.5
    content = re.sub(
        r'SplitPane\s+mainSplit\s*=\s*new\s+SplitPane\(\);\s*mainSplit\.setOrientation\([^)]+\);',
        'SplitPane mainSplit = new SplitPane();\n        mainSplit.setOrientation(javafx.geometry.Orientation.HORIZONTAL);',
        content
    )
    content = re.sub(
        r'mainSplit\.setDividerPositions\([^)]+\);',
        'mainSplit.setDividerPositions(0.5);',
        content
    )

    # 2. Update button definitions to Icon-Only, White Icons, cornflowerblue for Save, and tooltips
    # Handle btnAdd
    old_btn_add_patterns = [
        r'btnAdd\s*=\s*new\s+Button\([^)]+\);[^;]*btnAdd\.setStyle\([^)]+\);',
        r'btnAdd\s*=\s*new\s+Button\([^)]+\);'
    ]

    # Replace btnAdd style
    content = re.sub(
        r'FontAwesomeIconView\s+iconSave\s*=\s*new\s+FontAwesomeIconView\(FontAwesomeIcon\.SAVE\);\s*iconSave\.setFill\(Color\.WHITE\);\s*btnAdd\s*=\s*new\s+Button\([^)]*\);\s*(?:btnAdd\.getStyleClass\(\)\.add\("[^"]+"\);\s*)?btnAdd\.setStyle\([^)]+\);\s*btnAdd\.setOnAction\(e\s*->\s*guardar\(\)\);',
        'FontAwesomeIconView iconSave = new FontAwesomeIconView(FontAwesomeIcon.SAVE);\n        iconSave.setFill(Color.WHITE);\n        btnAdd = new Button("", iconSave);\n        btnAdd.setStyle("-fx-background-color: cornflowerblue; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");\n        btnAdd.setOnAction(e -> guardar());\n        Tooltip.install(btnAdd, new Tooltip("Guardar"));',
        content
    )

    # Replace btnDelete
    content = re.sub(
        r'FontAwesomeIconView\s+iconDel\s*=\s*new\s+FontAwesomeIconView\(FontAwesomeIcon\.TRASH\);\s*iconDel\.setFill\(Color\.WHITE\);\s*btnDelete\s*=\s*new\s+Button\([^)]*\);\s*btnDelete\.setStyle\([^)]+\);\s*btnDelete\.setOnAction\(e\s*->\s*eliminar\(\)\);',
        'FontAwesomeIconView iconDel = new FontAwesomeIconView(FontAwesomeIcon.TRASH);\n        iconDel.setFill(Color.WHITE);\n        btnDelete = new Button("", iconDel);\n        btnDelete.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");\n        btnDelete.setOnAction(e -> eliminar());\n        Tooltip.install(btnDelete, new Tooltip("Eliminar"));',
        content
    )

    # Replace btnClear
    content = re.sub(
        r'FontAwesomeIconView\s+iconClear\s*=\s*new\s+FontAwesomeIconView\(FontAwesomeIcon\.(?:ERASER|UNDO)\);\s*iconClear\.setFill\(Color\.WHITE\);\s*btnClear\s*=\s*new\s+Button\([^)]*\);\s*(?:btnClear\.getStyleClass\(\)\.add\("[^"]+"\);\s*)?btnClear\.setStyle\([^)]+\);\s*btnClear\.setOnAction\(e\s*->\s*limpiarFormulario\(\)\);',
        'FontAwesomeIconView iconClear = new FontAwesomeIconView(FontAwesomeIcon.UNDO);\n        iconClear.setFill(Color.WHITE);\n        btnClear = new Button("", iconClear);\n        btnClear.setStyle("-fx-background-color: #6c757d; -fx-cursor: hand; -fx-padding: 6px 14px; -fx-background-radius: 4px;");\n        btnClear.setOnAction(e -> limpiarFormulario());\n        Tooltip.install(btnClear, new Tooltip("Limpiar"));',
        content
    )

    # 3. Move btnRow / btnBox / actionRow to root footer if inside rightPane
    if "rightPane.getChildren().addAll(" in content and ("btnRow" in content or "btnBox" in content):
        content = content.replace("rightPane.getChildren().addAll(lblFormTitle, grid, btnRow);", "rightPane.getChildren().addAll(lblFormTitle, grid);")
        content = content.replace("rightPane.getChildren().addAll(lblFormTitle, grid, btnBox);", "rightPane.getChildren().addAll(lblFormTitle, grid);")
        content = content.replace("rightPane.getChildren().addAll(lblFormTitle, formContainer, btnRow);", "rightPane.getChildren().addAll(lblFormTitle, formContainer);")

        # Rename btnRow/btnBox to actionRow
        content = re.sub(r'HBox\s+(?:btnRow|btnBox)\s*=\s*new\s+HBox\([^)]+\);', 'HBox actionRow = new HBox(8, btnAdd, btnClear, btnDelete);', content)
        content = re.sub(r'(?:btnRow|btnBox)\.setAlignment\(Pos\.CENTER_RIGHT\);', 'actionRow.setAlignment(Pos.CENTER_RIGHT);', content)
        content = re.sub(r'(?:btnRow|btnBox)\.setPadding\(new\s+Insets\([^)]+\)\);', 'actionRow.setPadding(new Insets(15, 0, 0, 0));', content)

        if "root.getChildren().addAll(headerBox, mainSplit);" in content:
            content = content.replace("root.getChildren().addAll(headerBox, mainSplit);", "root.getChildren().addAll(headerBox, mainSplit, actionRow);")

    with open(filepath, "w") as f:
        f.write(content)

    print(f"Processed {os.path.basename(filepath)}")

print("Completed maintainer layout update script!")
