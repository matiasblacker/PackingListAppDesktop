package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

public class GuiaTabContent extends VBox {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    private final TextField txtBuscar;
    private final Accordion accordion;
    private final List<TitledPaneData> allPanes = new ArrayList<>();

    public GuiaTabContent() {
        setSpacing(15);
        setPadding(new Insets(20));
        setStyle("-fx-background-color: #f8fafc;");

        // --- ENCABEZADO DE LA GUÍA ---
        VBox headerBox = new VBox(4);
        HBox topTitleRow = new HBox(10);
        topTitleRow.setAlignment(Pos.CENTER_LEFT);

        FontAwesomeIconView bookIcon = new FontAwesomeIconView(FontAwesomeIcon.BOOK);
        bookIcon.setSize("24px");
        bookIcon.setFill(Color.web("#1e3a8a"));

        Label lblTitle = new Label("Manual Operativo & Guía del Sistema WMS");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #1e3a8a;");

        topTitleRow.getChildren().addAll(bookIcon, lblTitle);

        Label lblSub = new Label("Documentación integral, guía paso a paso, funcionalidad y matriz de relaciones del flujo logístico.");
        lblSub.setStyle("-fx-font-size: 13px; -fx-text-fill: #64748b;");

        headerBox.getChildren().addAll(topTitleRow, lblSub);

        // --- DIAGRAMA DEL FLUJO LOGÍSTICO WMS (WHITE CARD) ---
        VBox flowCard = new VBox(10);
        flowCard.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0; -fx-border-radius: 8px; -fx-background-radius: 8px; -fx-padding: 14px;");

        Label lblFlowTitle = new Label("Diagrama del Flujo de Datos & Cadena Logística WMS", new FontAwesomeIconView(FontAwesomeIcon.SITEMAP));
        lblFlowTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        HBox flowDiagram = new HBox(8);
        flowDiagram.setAlignment(Pos.CENTER);
        flowDiagram.setPadding(new Insets(8, 0, 8, 0));

        flowDiagram.getChildren().addAll(
            crearStepBadge("1. Proveedores / SKU", "#3b82f6"),
            crearArrow(),
            crearStepBadge("2. Anuncio (ASN)", "#8b5cf6"),
            crearArrow(),
            crearStepBadge("3. Recepción", "#10b981"),
            crearArrow(),
            crearStepBadge("4. Ubicación Fís.", "#06b6d4"),
            crearArrow(),
            crearStepBadge("5. Stock WMS", "#3b82f6"),
            crearArrow(),
            crearStepBadge("6. Nota Pedido", "#f59e0b"),
            crearArrow(),
            crearStepBadge("7. Despacho/Picking", "#ef4444"),
            crearArrow(),
            crearStepBadge("8. Tracking / Packing", "#6366f1")
        );

        flowCard.getChildren().addAll(lblFlowTitle, flowDiagram);

        // --- BARRA DE BÚSQUEDA Y FILTRADO EN TIEMPO REAL ---
        HBox filterBar = new HBox(10);
        filterBar.setAlignment(Pos.CENTER_LEFT);

        Label lblSearchIcon = new Label("", new FontAwesomeIconView(FontAwesomeIcon.SEARCH));
        txtBuscar = new TextField();
        txtBuscar.setPromptText("Buscar por módulo, término o palabra clave (ej: Anuncio, Stock, Relación, Pallet)...");
        txtBuscar.setStyle("-fx-background-radius: 6px; -fx-border-radius: 6px; -fx-border-color: #cbd5e1; -fx-padding: 8 12; -fx-font-size: 12px;");
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        filterBar.getChildren().addAll(lblSearchIcon, txtBuscar);

        // --- ACORDEÓN DE MÓDULOS Y DOCUMENTACIÓN ---
        accordion = new Accordion();
        accordion.setStyle("-fx-background-color: transparent;");

        inicializarSeccionesDocumentacion();

        txtBuscar.textProperty().addListener((obs, oldVal, newVal) -> filtrarSecciones(newVal));

        ScrollPane scrollPane = new ScrollPane(accordion);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-border-color: transparent;");
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        getChildren().addAll(headerBox, flowCard, filterBar, scrollPane);
    }

    private Label crearStepBadge(String texto, String colorHex) {
        Label lbl = new Label(texto);
        lbl.setStyle("-fx-background-color: " + colorHex + "; -fx-text-fill: white; -fx-font-size: 11px; -fx-font-weight: bold; -fx-padding: 4 8; -fx-background-radius: 6px;");
        return lbl;
    }

    private Label crearArrow() {
        Label lbl = new Label("➔");
        lbl.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 13px;");
        return lbl;
    }

    private void inicializarSeccionesDocumentacion() {
        agregarSeccion(
            "1. Mantenedores Base: Empresas, Bodegas, Zonas y Usuarios",
            FontAwesomeIcon.BUILDING,
            "Gestión estructural de la organización, unidades de almacenamiento y control de accesos.",
            "Menú Mantenimiento > Empresas / Bodegas / Usuarios.",
            "Permite definir las empresas del grupo, sus bodegas físicas, los sectores/zonas internas (ej: Zona Comercial, Zona Primaria) y los usuarios con sus roles (ADMINSIS, ADMIN_BODEGA, etc.).",
            "Relación directa con TODOS los módulos WMS. Las bodegas y zonas son requeridas para recepcionar y ubicar stock. Los usuarios quedan registrados en las trazabilidades de auditoría (creado por, actualizado por).",
            "Estructura organizacional configurada e identidades con permisos adecuados."
        );

        agregarSeccion(
            "2. Entidades Comerciales: Clientes, Proveedores, Carriers y Productos (SKU)",
            FontAwesomeIcon.TAGS,
            "Catálogos maestros de actores comerciales, transportistas e inventario de artículos.",
            "Menú Mantenimiento > Clientes / Proveedores / Carriers / Productos.",
            "Registra información obligatoria (RUT/Tax ID, Razón Social, Dirección, Comuna/País) con soporte para extranjeros. En Productos, registra SKU, descripción, Part Number y pesos.",
            "• Proveedores ➔ Requeridos para crear Anuncios de Carga (ASN) y Recepciones.\n• Clientes ➔ Requeridos para emitir Notas de Pedido (NP) y Despachos.\n• Carriers ➔ Requeridos en los Hitos de Despacho y Tracking de Envíos.\n• Productos ➔ Requeridos en las líneas de detalle de todos los documentos operacionales.",
            "Maestros de datos consolidados con soporte de caché Caffeine para consultas en tiempo real."
        );

        agregarSeccion(
            "3. Ubicaciones Físicas (Diseño de Espacios WMS)",
            FontAwesomeIcon.CUBES,
            "Definición matricial de coordenadas de almacenamiento físico en bodega.",
            "Menú Operaciones WMS > Ubicaciones Físicas (o Menú Mantenimiento).",
            "Permite estructurar las bodegas mediante un árbol jerárquico (Bodega ➔ Zona/Sector) y asociar coordenadas compuestas (Pasillo, Posición, Nivel/Altura) junto al tipo de soporte (RACK, ESTANTE, SUELO, CONTENEDOR). Genera automáticamente el código único de ubicación (ej: `A-PA3-N1`).",
            "Relacionado estrechamente con Recepciones de Carga (donde se asigna la ubicación destino), Inventario Visual (donde se grafican las casillas) y Despachos NP (de donde se extrae el stock).",
            "Matriz de ubicaciones etiquetada y lista para el direccionamiento físico de mercadería."
        );

        agregarSeccion(
            "4. Anuncios de Carga (ASN - Advanced Shipping Notice)",
            FontAwesomeIcon.BULLHORN,
            "Registro de alertas previas de llegada de carga desde proveedores.",
            "Menú Operaciones WMS > Anuncios de Carga.",
            "Permite registrar la fecha estimada de arribo, proveedor emisor, bodega destino y el detalle de artículos con sus cantidades anunciadas. Soporta pestañas de consulta por Zona Comercial y Zona Primaria.",
            "Nace a partir de un Proveedor y un Producto. Se relaciona directamente con Recepciones de Carga, ya que al recepcionar mercadería se selecciona el Anuncio de Carga origen para contrastar lo anunciado vs lo recibido físicamente.",
            "Documento ASN registrado que habilita el proceso de recepción rápida en bodega."
        );

        agregarSeccion(
            "5. Recepciones de Carga (Ingreso Físico a Bodega)",
            FontAwesomeIcon.DOWNLOAD,
            "Verificación de llegada física de mercadería e ingreso directo a ubicaciones.",
            "Menú Operaciones WMS > Recepciones de Carga.",
            "Asocia un Anuncio de Carga (ASN), valida las cantidades recibidas por línea de producto y asigna la ubicación física de destino para cada artículo. Al confirmar, incrementa automáticamente el saldo en el Inventario General y notifica en tiempo real por WebSocket.",
            "Consume Anuncios de Carga y Ubicaciones Físicas. Al finalizar, actualiza automáticamente el Stock/Inventario General y el Inventario Visual.",
            "Mercadería recepcionada, stock ingresado en saldo disponible e historial de recepción completo."
        );

        agregarSeccion(
            "6. Stock e Inventario General",
            FontAwesomeIcon.DATABASE,
            "Consulta consolidada de existencias disponible en tiempo real.",
            "Menú Operaciones WMS > Inventario General.",
            "Muestra el saldo de unidades por producto, dividido en tres pestañas operativas: Zona Comercial, Zona Primaria y Total Consolidado. Incluye tarjetas de resumen con total de ítems y unidades.",
            "Se alimenta automáticamente de las Recepciones de Carga. Es consultado por las Notas de Pedido y Despachos NP para validar disponibilidad de picking.",
            "Visibilidad 100% precisa del saldo de inventario disponible en bodega."
        );

        agregarSeccion(
            "7. Inventario Visual (Mapa de Bodega & Reubicación)",
            FontAwesomeIcon.MAP_SIGNS,
            "Navegación gráfica e interactiva de racks, estantes y reubicación de stock.",
            "Menú Operaciones WMS > Inventario Visual.",
            "Despliega una rejilla visual interactiva por pasillos y niveles. Al hacer clic sobre cualquier casilla de ubicación, muestra el stock detallado alojado en ella y permite ejecutar el botón 'Reubicar Stock' para mover productos a otra coordenada de forma transparente.",
            "Conectado dinámicamente con las Ubicaciones Físicas y el Inventario General. Sincronizado en vivo mediante WebSocket.",
            "Gestión física visual de la bodega y movimientos de reubicación interna sin errores de traslape."
        );

        agregarSeccion(
            "8. Notas de Pedido (NP)",
            FontAwesomeIcon.FILE_TEXT_ALT,
            "Solicitudes formales de preparación y salida de mercadería para clientes.",
            "Menú Operaciones WMS > Notas de Pedido.",
            "Permite crear requerimientos de despacho asociados a un Cliente, especificando bodega origen, fecha de despacho y las líneas de producto solicitadas con sus unidades.",
            "Requiere Clientes y Productos. Es la entrada obligatoria para el módulo de Despachos NP.",
            "Nota de Pedido aprobada y en cola de preparación para picking y despacho."
        );

        agregarSeccion(
            "9. Despachos NP (Picking & Salida de Mercadería)",
            FontAwesomeIcon.TRUCK,
            "Preparación de pedidos, picking en ubicación y egreso de inventario.",
            "Menú Operaciones WMS > Despachos NP.",
            "Asocia una Nota de Pedido (NP), selecciona la ubicación física desde donde se retirará el producto (picking), valida el saldo disponible y descuenta las unidades del stock al confirmar la salida.",
            "Consume Notas de Pedido y Ubicaciones Físicas. Al confirmar el despacho, descuenta del Stock General, actualiza el Inventario Visual y genera el registro inicial para el Tracking de Envíos.",
            "Orden de despacho procesada, inventario rebajado y guía de salida generada."
        );

        agregarSeccion(
            "10. Tracking de Envíos",
            FontAwesomeIcon.MAP_MARKER,
            "Monitoreo de transporte, couriers e hitos de entrega al cliente.",
            "Menú Operaciones WMS > Tracking de Envíos.",
            "Permite asociar un Carrier/Courier (ej: Starken, Chilexpress, DHL) a un Despacho, asignar código de seguimiento (tracking number) y actualizar hitos de transporte (EN_TRANSITO, ENTREGADO, RETENIDO, INCIDENCIA).",
            "Nace a partir de un Despacho NP y utiliza la tabla de Carriers/Couriers. Se relaciona con la entrega final al Cliente.",
            "Trazabilidad 360° del transporte de carga hasta su recepción conforme por el cliente."
        );

        agregarSeccion(
            "11. Packing List & Agrupación de Pallets",
            FontAwesomeIcon.ARCHIVE,
            "Creación manual de bultos, estiba y consolidación de carga en pallets.",
            "Menú Archivo > Crear Packing Manual / Menú Operaciones > Agrupar Pallets.",
            "Permite armar listas de empaque definiendo dimensiones de bultos, pesos netos/brutos y contenido por caja/pallet. La herramienta 'Agrupar Pallets' permite apilar y consolidar múltiples bultos pequeños en una gran unidad física.",
            "Conecta los Productos e inventario procesado con los documentos de transporte marítimo/terrestre.",
            "Documento Packing List estructurado, etiquetado y listo para cubicación o exportación."
        );

        agregarSeccion(
            "12. Modelador 2D/3D de Contenedores & Certificados PDF",
            FontAwesomeIcon.SHIP,
            "Simulación gráfica de cubicación en contenedores de 20'/40' y reportes PDF.",
            "Menú Operaciones > Modelador Contenedor / Menú Exportar > Generar PDF.",
            "Visualiza la estiba de pallets dentro de contenedores marítimos en vista de planta y perfil con validación de altura de techo y aprovechamiento cúbico. Exporta certificados formales de estiba en formato PDF con logotipo corporativo.",
            "Utiliza los bultos y pallets armados en el Packing List.",
            "Certificado visual de estiba y documentos PDF listos para aduana e inspección."
        );

        agregarSeccion(
            "13. Dashboard, KPIs & Feed en Tiempo Real",
            FontAwesomeIcon.DASHBOARD,
            "Panel de control ejecutivo con métricas de ocupación, saldos y avisos WebSocket.",
            "Pestaña 'Dashboard WMS' en el workspace principal.",
            "Muestra indicadores clave de rendimiento (KPIs de stock, notas activas, recepciones, packings), barra de porcentaje de ocupación de bodega y alimentación en vivo de notificaciones.",
            "Consolida información de todos los módulos operacionales en tiempo real.",
            "Toma de decisiones estratégica respaldada por datos en vivo."
        );
    }

    private void agregarSeccion(String titulo, FontAwesomeIcon icon, String resumen, String comoHacerlo, String queHace, String relaciones, String resultado) {
        VBox content = new VBox(12);
        content.setPadding(new Insets(16));
        content.setStyle("-fx-background-color: white;");

        Label lblResumen = new Label(resumen);
        lblResumen.setWrapText(true);
        lblResumen.setStyle("-fx-font-size: 13px; -fx-text-fill: #334155; -fx-font-weight: bold;");

        VBox grid = new VBox(10);
        grid.setStyle("-fx-background-color: #f8fafc; -fx-padding: 14; -fx-background-radius: 8px; -fx-border-color: #e2e8f0; -fx-border-radius: 8px;");

        grid.getChildren().addAll(
            crearFilaInfo("⚙️ Dónde encontrarlo y Cómo hacerlo", comoHacerlo),
            crearFilaInfo("💡 Qué hace exactamente", queHace),
            crearFilaInfo("🔗 Con qué otros ítems se relaciona (Flujo WMS)", relaciones),
            crearFilaInfo("✅ Resultado u objetivo final", resultado)
        );

        content.getChildren().addAll(lblResumen, grid);

        HBox headerBox = new HBox(8);
        headerBox.setAlignment(Pos.CENTER_LEFT);
        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("14px");
        iconView.setFill(Color.web("#1e3a8a"));

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        headerBox.getChildren().addAll(iconView, lblTitle);

        TitledPane pane = new TitledPane();
        pane.setGraphic(headerBox);
        pane.setContent(content);
        pane.setStyle("-fx-font-size: 13px; -fx-font-weight: bold;");

        accordion.getPanes().add(pane);
        allPanes.add(new TitledPaneData(pane, titulo, resumen, comoHacerlo, queHace, relaciones, resultado));
    }

    private VBox crearFilaInfo(String subtitulo, String texto) {
        Label lblT = new Label(subtitulo);
        lblT.setStyle("-fx-font-weight: bold; -fx-text-fill: #1e3a8a; -fx-font-size: 12px;");

        Label lblC = new Label(texto);
        lblC.setWrapText(true);
        lblC.setStyle("-fx-text-fill: #334155; -fx-padding: 2 0 0 16; -fx-line-spacing: 1.3; -fx-font-size: 12px;");

        VBox box = new VBox(3, lblT, lblC);
        box.setPadding(new Insets(2, 0, 2, 0));
        return box;
    }

    private void filtrarSecciones(String query) {
        if (query == null || query.trim().isEmpty()) {
            accordion.getPanes().clear();
            for (TitledPaneData data : allPanes) {
                accordion.getPanes().add(data.pane);
            }
            return;
        }

        String q = query.toLowerCase().trim();
        accordion.getPanes().clear();
        for (TitledPaneData data : allPanes) {
            if (data.matches(q)) {
                accordion.getPanes().add(data.pane);
            }
        }
    }

    private static class TitledPaneData {
        final TitledPane pane;
        final String titulo;
        final String resumen;
        final String comoHacerlo;
        final String queHace;
        final String relaciones;
        final String resultado;

        TitledPaneData(TitledPane pane, String titulo, String resumen, String comoHacerlo, String queHace, String relaciones, String resultado) {
            this.pane = pane;
            this.titulo = titulo.toLowerCase();
            this.resumen = resumen.toLowerCase();
            this.comoHacerlo = comoHacerlo.toLowerCase();
            this.queHace = queHace.toLowerCase();
            this.relaciones = relaciones.toLowerCase();
            this.resultado = resultado.toLowerCase();
        }

        boolean matches(String q) {
            return titulo.contains(q) || resumen.contains(q) || comoHacerlo.contains(q) || queHace.contains(q) || relaciones.contains(q) || resultado.contains(q);
        }
    }
}
