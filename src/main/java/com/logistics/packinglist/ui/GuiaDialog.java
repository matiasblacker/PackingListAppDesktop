package com.logistics.packinglist.ui;

import com.logistics.packinglist.service.MantenimientoService;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import com.logistics.packinglist.utils.ScreenUtil;

public class GuiaDialog extends Stage {
    private final MantenimientoService service = MantenimientoService.getInstance();

    

    public GuiaDialog(Window owner) {
        setTitle("Guía de Usuario - PackingList APP");
        if (owner != null) {
            initOwner(owner);
        }
        initModality(Modality.NONE);
        setMinWidth(700);
        setMinHeight(480);
        
        // Root container con fondo moderno
        VBox root = new VBox(20);
        root.setPadding(new Insets(30));
        root.setStyle("-fx-background-color: #f8fafc;");

        // Cabecera
        HBox header = new HBox(15);
        header.setAlignment(Pos.CENTER_LEFT);
        
        Label lblIcon = new Label("📖");
        lblIcon.setStyle("-fx-font-size: 38px; -fx-effect: dropshadow(two-pass-box, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        VBox titleBox = new VBox(4);
        Label titulo = new Label("Guía y Manual de Usuario");
        titulo.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");
        
        Label subtitulo = new Label("Aprende a utilizar todas las herramientas operativas de PackingList APP.");
        subtitulo.setStyle("-fx-font-size: 14px; -fx-text-fill: #64748b;");
        
        titleBox.getChildren().addAll(titulo, subtitulo);
        header.getChildren().addAll(lblIcon, titleBox);

        // Contenido Principal (Acordeón)
        Accordion accordion = new Accordion();
        accordion.setStyle("-fx-background-color: transparent;");

        accordion.getPanes().addAll(
            crearSeccion("Creación Manual de Packing", "✍️",
                "Diseña y gestiona un nuevo Packing List directamente en el sistema de manera secuencial y estructurada.",
                "Menú Archivo > Crear Packing Manual...",
                "Abre un editor secuencial donde puedes definir la cabecera comercial del documento, añadir progresivamente pallets o cajas, y registrar los productos en su interior a mano o mediante lector de código de barras.",
                "Obtendrás un documento Packing List estructurado, guardado en el sistema y listo para ser agrupado o exportado a PDF."
            ),

            crearSeccion("Agrupación de Pallets", "🔗",
                "Consolida múltiples bultos pequeños en una única gran unidad de carga física (apilamiento real sobre una misma base).",
                "Con un Packing List abierto > Menú Operaciones > Agrupar Pallets.",
                "Suma los pesos brutos y netos automáticamente, e incrementa la altura total (al apilar), conservando el área base del primer pallet seleccionado y unificando el inventario.",
                "Reduce el volumen de bultos separados, optimiza el registro de la estiba y emite una sola etiqueta identificativa."
            ),

            crearSeccion("Modelador de Contenedores", "🚢",
                "Herramienta gráfica y profesional para planificar, estibar y validar la carga física dentro de contenedores marítimos.",
                "Con un Packing List abierto > Menú Operaciones > Modelador Contenedor.",
                "Te permite arrastrar y soltar la carga en una simulación de vista aérea (planta) y lateral (perfil). Puedes rotar los pallets, apilarlos (el sistema valida la altura del techo) y gestionar flotas de contenedores de 20' o 40'. Todo el progreso se guarda solo.",
                "Podrás exportar un 'Certificado Visual de Estiba' totalmente trazable, que demuestra a clientes y aduanas el máximo aprovechamiento del espacio cúbico."
            ),

            crearSeccion("Mantenedor de Inventario", "📋",
                "Base de datos persistente para agilizar el reconocimiento de productos de uso frecuente.",
                "Menú Inventario > Mantenedor General.",
                "Permite registrar o importar SKU (Cód. Barras), descripciones corporativas, Part Numbers y pesos unitarios de los artículos que frecuentas.",
                "Aceleración de trabajo: Al crear un packing manual, escanea o escribe el SKU y el sistema autocompletará toda la descripción al instante."
            ),

            crearSeccion("Generación de Documentos PDF", "📄",
                "Creación de certificados formales para el respaldo de despacho, transportistas y auditoría.",
                "Menú Exportar > Seleccionar la modalidad deseada (Bulto actual o Todos los bultos).",
                "Recopila metadatos del bulto seleccionado, la tabla detallada de ítems, cálculo de subtotales, márgenes y formatea todo junto con el logotipo de tu empresa.",
                "Un archivo .pdf en tu ordenador con diseño estrictamente profesional, limpio e higiénico, listo para imprimir en bodega o adjuntar por correo electrónico."
            )
        );

        ScrollPane scroll = new ScrollPane(accordion);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent; -fx-padding: 10 0;");
        VBox.setVgrow(scroll, Priority.ALWAYS);

        // Footer / Botón de Acción
        Button btnCerrar = new Button("Comprendido, cerrar guía");
        btnCerrar.setStyle(
            "-fx-background-color: #0F3E6E; " +
            "-fx-text-fill: white; " +
            "-fx-font-weight: bold; " +
            "-fx-font-size: 14px; " +
            "-fx-padding: 10 24; " +
            "-fx-background-radius: 6; " +
            "-fx-cursor: hand;"
        );
        btnCerrar.setOnMouseEntered(e -> btnCerrar.setStyle("-fx-background-color: #1a5c9e; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnCerrar.setOnMouseExited(e -> btnCerrar.setStyle("-fx-background-color: #0F3E6E; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px; -fx-padding: 10 24; -fx-background-radius: 6; -fx-cursor: hand;"));
        btnCerrar.setOnAction(e -> close());
        
        HBox bottom = new HBox(btnCerrar);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(10, 0, 0, 0));

        root.getChildren().addAll(header, new Separator(), scroll, new Separator(), bottom);
        
        Scene scene = new Scene(root, 750, 560);
        if (getClass().getResource("/styles.css") != null) {
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
        }
        setScene(scene);
        ScreenUtil.fitDialogToScreen(this, getOwner(), 750, 560, 680, 480);
    }

    private TitledPane crearSeccion(String titulo, String icon, String descripcion, String comoHacerlo, String funcion, String resultado) {
        VBox content = new VBox(16);
        content.setPadding(new Insets(20));
        content.setStyle("-fx-background-color: white;");

        Label lblDesc = new Label(descripcion);
        lblDesc.setWrapText(true);
        lblDesc.setStyle("-fx-font-size: 14px; -fx-text-fill: #334155; -fx-line-spacing: 1.4;");

        VBox grid = new VBox(12);
        grid.setStyle(
            "-fx-background-color: #f8fafc; " +
            "-fx-padding: 16; " +
            "-fx-background-radius: 8; " +
            "-fx-border-color: #e2e8f0; " +
            "-fx-border-radius: 8;"
        );

        grid.getChildren().addAll(
            crearFilaInfo("⚙️ Dónde encontrarlo", comoHacerlo),
            crearFilaInfo("💡 Qué hace exactamente", funcion),
            crearFilaInfo("✅ Cuál es el resultado", resultado)
        );

        content.getChildren().addAll(lblDesc, grid);

        TitledPane pane = new TitledPane(icon + "  " + titulo, content);
        pane.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: #1e293b; -fx-cursor: hand;");
        return pane;
    }

    private VBox crearFilaInfo(String subtitulo, String texto) {
        Label lblT = new Label(subtitulo);
        lblT.setStyle("-fx-font-weight: bold; -fx-text-fill: #0f172a; -fx-font-size: 13px;");
        
        Label lblC = new Label(texto);
        lblC.setWrapText(true);
        lblC.setStyle("-fx-text-fill: #475569; -fx-padding: 2 0 0 24; -fx-line-spacing: 1.4; -fx-font-size: 13px;");
        
        VBox box = new VBox(4, lblT, lblC);
        box.setPadding(new Insets(4, 0, 4, 0));
        return box;
    }
}
