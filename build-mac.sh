#!/bin/bash
echo "⚙️  Empaquetando JAR con Maven..."
rm -rf installer/
mkdir -p installer/
mvn clean package -DskipTests

echo "🍏 Creando instalador (.dmg/.app) para macOS con jpackage..."
jpackage --type dmg \
  --name "PackingList" \
  --input target/ \
  --main-jar packing-list-app-2.0.0.jar \
  --main-class com.logistics.packinglist.Launcher \
  --icon src/main/resources/icon.png \
  --app-version 2.0.0 \
  --dest installer \
  --mac-package-name "PackingList" \
  --java-options "-Xmx2G"

echo "✅ Instalador creado exitosamente en la carpeta /installer"
