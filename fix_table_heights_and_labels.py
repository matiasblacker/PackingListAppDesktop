import re

# 1. Update RecepcionesDialog.java
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    rec_content = f.read()

# Increase table height for tblItems
rec_content = rec_content.replace(
    "tblItems.setPrefHeight(180);",
    "tblItems.setMinHeight(240);\n        tblItems.setPrefHeight(280);"
)

# Adjust formGrid ColumnConstraints in RecepcionesDialog to fit labels cleanly
rec_content = rec_content.replace(
    "ColumnConstraints gc1 = new ColumnConstraints(75);",
    "ColumnConstraints gc1 = new ColumnConstraints(85);"
)

# Adjust itemModifyGrid ColumnConstraints in RecepcionesDialog to fit "Con Stock Previo" cleanly
rec_content = rec_content.replace(
    "ColumnConstraints mc1 = new ColumnConstraints(110);",
    "ColumnConstraints mc1 = new ColumnConstraints(125);"
)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(rec_content)

print("Updated RecepcionesDialog table height & label column widths")

# 2. Update AnunciosDialog.java
with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    anun_content = f.read()

# Increase table height for tblDetails
anun_content = anun_content.replace(
    "tblDetails.setPrefHeight(180);",
    "tblDetails.setMinHeight(240);\n        tblDetails.setPrefHeight(280);"
)

# Adjust formGrid ColumnConstraints in AnunciosDialog to fit labels cleanly
anun_content = anun_content.replace(
    "ColumnConstraints gc1 = new ColumnConstraints(75);",
    "ColumnConstraints gc1 = new ColumnConstraints(85);"
)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(anun_content)

print("Updated AnunciosDialog table height & label column widths")
