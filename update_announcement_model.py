with open("src/main/java/com/logistics/packinglist/model/ReceptionAnnouncementModel.java", "r") as f:
    content = f.read()

target = """    private String zonaDestino;"""

replacement = """    private String zonaDestino;
    private Boolean esContenedor;
    private String numeroContenedor;
    private String digitoContenedor;"""

content = content.replace(target, replacement)
with open("src/main/java/com/logistics/packinglist/model/ReceptionAnnouncementModel.java", "w") as f:
    f.write(content)

print("Updated ReceptionAnnouncementModel.java successfully")
