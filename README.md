# Packing List & Container Planner App

Aplicación de escritorio profesional construida con JavaFX para la gestión logística avanzada. Permite procesar listas de empaque (Packing Lists), agrupar ítems en bultos/pallets, y planificar visualmente la estiba en contenedores de 20 y 40 pies, culminando en la generación de certificados PDF de carga.

---

## 🚀 Características Principales

### 📦 Gestión de Packing List
- **Procesamiento de Excel:** Importación automática de archivos Excel (`.xlsx`), detectando y agrupando ítems por bulto/pallet de forma inteligente.
- **Normalización de SKUs:** Sistema robusto de detección y limpieza automática de lectura de escáneres, corrigiendo comillas, espacios y guiones para un cruce perfecto con la base de datos de inventario.
- **Gestión Avanzada de Bultos:** Posibilidad de fusionar pallets, asignarles "Alias" para identificación interna (sin afectar los nombres de exportación) y manejar grandes volúmenes de datos con una interfaz virtualizada y ágil.

### 🚢 Planificador de Contenedores (Módulo Estiba)
- **Gestión de Flota:** Crea y gestiona múltiples contenedores simultáneamente, asignándoles nombres personalizados.
- **Interfaz Drag & Drop:** Visualiza e interactúa con la carga mediante una interfaz gráfica que permite arrastrar pallets a contenedores de 20' o 40'.
- **Eficiencia y Estabilidad:** Cálculos matemáticos de distribución procesados en hilos de fondo para mantener una experiencia de usuario (UX) fluida y sin interrupciones, incluso con miles de ítems.

### 📄 Generación de Reportes PDF
- **Packing List Tradicional:** Generación de PDF en formato Carta (con logo corporativo) por bulto y un resumen final global.
- **Certificación de Contenedores:** Exportación de reportes detallados y multipágina que documentan el layout exacto de la carga en cada contenedor y sus contenidos específicos (ej. "Pallet X sobre Pallet Y").

### 🎨 Experiencia de Usuario (UI/UX)
- Diseño responsivo y moderno.
- Diálogos profesionales ("Acerca de", "Guía de Usuario").
- Sistema de prevención de pérdida de datos: confirmación de salida con autoguardado del trabajo actual.

---

## 🛠 Requisitos del Sistema

| Herramienta | Versión Recomendada |
|-------------|---------------------|
| Java JDK    | 17 o superior       |
| Maven       | 3.8 o superior      |

---

## 💻 Ejecución y Desarrollo

Para ejecutar el proyecto en modo desarrollo desde el código fuente usando Maven:

```bash
# Ejecutar la aplicación (no requiere configuraciones adicionales de VM)
mvn javafx:run
```

---

## 📦 Empaquetado y Distribución

El proyecto está configurado para generar instaladores nativos utilizando la herramienta `jpackage` integrada en el ciclo de vida de Maven.

Se incluyen scripts para facilitar la compilación multiplataforma:

- **Windows (.exe):** Ejecutar `build-windows.bat`
- **Linux (.deb):** Ejecutar `./build-linux.sh`
- **macOS (.app/.dmg):** Ejecutar `./build-mac.sh`

*Los instaladores generados se depositarán en el directorio `installer/`.*

---

## 🏗 Arquitectura del Proyecto

```text
PackingListApp/
├── pom.xml
├── build-*.sh/bat               ← Scripts de generación de instaladores nativos
├── inventario.csv               ← Base de datos base para cruce de SKUs
└── src/main/java/com/logistics/packinglist/
    ├── Launcher.java            ← Punto de entrada (workaround para JavaFX)
    ├── MainApp.java             ← Clase principal JavaFX
    ├── model/                   ← Entidades: PackingItem, Bulto, Contenedor, etc.
    ├── service/                 ← Lógica de negocio, hilos de fondo y exportación PDF
    └── ui/                      ← Controladores visuales, vistas virtualizadas y Drag&Drop
```
