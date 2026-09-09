import re

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

old_mc_code = """        ColumnConstraints mc1 = new ColumnConstraints();
        mc1.setPrefWidth(135);
        ColumnConstraints mc2 = new ColumnConstraints();
        mc2.setHgrow(Priority.ALWAYS);
        ColumnConstraints mc3 = new ColumnConstraints();
        mc3.setPrefWidth(120);
        itemModifyGrid.getColumnConstraints().addAll(mc1, mc2, mc3);"""

new_mc_code = """        ColumnConstraints mc1 = new ColumnConstraints(110);
        ColumnConstraints mc2 = new ColumnConstraints(280);
        ColumnConstraints mc3 = new ColumnConstraints(40);
        itemModifyGrid.getColumnConstraints().addAll(mc1, mc2, mc3);"""

content = content.replace(old_mc_code, new_mc_code)

with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Updated itemModifyGrid ColumnConstraints successfully")
