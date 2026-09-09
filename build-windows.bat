@echo off
echo ⚙️ Empaquetando JAR con Maven...
if exist "installer" rmdir /s /q "installer"
mkdir "installer"
call mvn clean package -DskipTests

echo 🪟 Creando instalador (.exe) para Windows con jpackage...
REM Nota: jpackage en Windows a veces requiere que el icono sea .ico en lugar de .png
REM Si recibes un error, convierte tu icon.png a icon.ico y cambia la siguiente linea.
jpackage --type exe ^
  --name "PackingList" ^
  --input target/ ^
  --main-jar packing-list-app-2.0.0.jar ^
  --main-class com.logistics.packinglist.Launcher ^
  --icon src/main/resources/icon.png ^
  --app-version 2.0.0 ^
  --dest installer ^
  --win-shortcut ^
  --win-menu ^
  --java-options "-Xmx2G"

echo ✅ Instalador creado exitosamente en la carpeta /installer
pause
