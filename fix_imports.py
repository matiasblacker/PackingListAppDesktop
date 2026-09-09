with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "r") as f:
    content = f.read()

target = "import com.logistics.packinglist.service.ExcelDiferenciasService;"
replacement = """import com.logistics.packinglist.service.ExcelDiferenciasService;
import com.logistics.packinglist.service.PdfAnuncioService;
import com.logistics.packinglist.model.CompanyModel;"""

content = content.replace(target, replacement)
with open("src/main/java/com/logistics/packinglist/ui/RecepcionesDialog.java", "w") as f:
    f.write(content)

print("Added imports to RecepcionesDialog.java")
