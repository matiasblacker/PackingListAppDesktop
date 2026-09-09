import re

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "r") as f:
    content = f.read()

old_add = "root.getChildren().addAll(headerBox, mainSplit);"
new_add = "root.getChildren().addAll(headerBox, mainSplit, actionButtons);"
content = content.replace(old_add, new_add)

with open("src/main/java/com/logistics/packinglist/ui/AnunciosDialog.java", "w") as f:
    f.write(content)

print("Fixed actionButtons placement")
