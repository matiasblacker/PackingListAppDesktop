package com.logistics.packinglist.utils;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

public class ScreenUtil {

    /**
     * Centers the given stage on the screen where the mouse cursor is currently located.
     * Falls back to the primary screen if any exception occurs.
     */
    public static void centerOnActiveScreen(Stage stage) {
        if (stage.getWidth() > 0 && stage.getHeight() > 0 && !Double.isNaN(stage.getWidth())) {
            performCenter(stage);
        } else {
            stage.setOnShown(e -> performCenter(stage));
        }
    }

    private static void performCenter(Stage stage) {
        Screen activeScreen = Screen.getPrimary();
        try {
            java.awt.Point mouseLoc = java.awt.MouseInfo.getPointerInfo().getLocation();
            for (Screen s : Screen.getScreens()) {
                Rectangle2D bounds = s.getVisualBounds();
                if (bounds.contains(mouseLoc.x, mouseLoc.y)) {
                    activeScreen = s;
                    break;
                }
            }
        } catch (Exception ignored) {
        }

        Rectangle2D bounds = activeScreen.getVisualBounds();
        double x = bounds.getMinX() + (bounds.getWidth() - stage.getWidth()) / 2;
        double y = bounds.getMinY() + (bounds.getHeight() - stage.getHeight()) / 2;
        stage.setX(x);
        stage.setY(y);
    }

    /**
     * Centers a stage relative to its owner window. If the owner window is null,
     * it centers the stage on the active screen.
     */
    public static void centerOnOwner(Stage stage, Window owner) {
        if (owner != null) {
            stage.setOnShown(e -> {
                double x = owner.getX() + (owner.getWidth() - stage.getWidth()) / 2;
                double y = owner.getY() + (owner.getHeight() - stage.getHeight()) / 2;
                stage.setX(x);
                stage.setY(y);
            });
        } else {
            centerOnActiveScreen(stage);
        }
    }

    /**
     * Calcula la escala responsiva recomendada según la resolución de pantalla activa.
     * Soporta 1366x768 (o H <= 800), 1080p, 2K (1440p) y 4K (2160p),
     * compensando si el sistema operativo ya aplica escalamiento HiDPI.
     */
    public static double getResponsiveScaleFactor() {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double w = bounds.getWidth();
        double h = bounds.getHeight();
        double outputScale = Screen.getPrimary().getOutputScaleY();

        // Si el SO ya escaló (ej: 2.0x en 4K), el tamaño lógico en JavaFX ya está escalado
        if (outputScale > 1.2) {
            if (h <= 800) return 0.90;
            return 1.0;
        }

        if (w <= 1366 || h <= 800) {
            return 0.88; // Modo compacto para laptops 1366x768
        } else if (w <= 1920 && h <= 1080) {
            return 1.0;  // Estándar Full HD 1080p
        } else if (w <= 2560 && h <= 1440) {
            return 1.25; // 2K QHD
        } else {
            return 1.60; // 4K UHD
        }
    }

    /**
     * Obtiene el tamaño base de fuente en px para el nodo raíz según la resolución.
     */
    public static double getResponsiveBaseFontSize() {
        double scale = getResponsiveScaleFactor();
        return Math.round(11.5 * scale * 10.0) / 10.0;
    }

    /**
     * Obtiene el tamaño de fuente en px para los títulos/etiquetas de inputs en formularios.
     * Diseñado para ser visiblemente más pequeño y estilizado.
     */
    public static double getResponsiveFormLabelFontSize() {
        double scale = getResponsiveScaleFactor();
        return Math.round(9.5 * scale * 10.0) / 10.0;
    }

    /**
     * Obtiene el tamaño de fuente en px para los campos de entrada (TextField, ComboBox, etc.).
     */
    public static double getResponsiveInputFontSize() {
        double scale = getResponsiveScaleFactor();
        return Math.round(10.0 * scale * 10.0) / 10.0;
    }

    /**
     * Aplica la hoja de estilos global y la escala tipográfica responsiva a una Scene.
     */
    public static void applyResponsiveTheme(javafx.scene.Scene scene) {
        if (scene == null || scene.getRoot() == null) return;

        try {
            String cssUrl = ScreenUtil.class.getResource("/styles.css").toExternalForm();
            if (!scene.getStylesheets().contains(cssUrl)) {
                scene.getStylesheets().add(cssUrl);
            }
        } catch (Exception ignored) {
        }

        double baseFont = getResponsiveBaseFontSize();
        double labelFont = getResponsiveFormLabelFontSize();
        double inputFont = getResponsiveInputFontSize();

        // Inyectar clase o estilo dinámico en el root si no está fijado
        String existingStyle = scene.getRoot().getStyle();
        if (existingStyle == null) existingStyle = "";
        
        // Agregar propiedades dinámicas al root
        StringBuilder sb = new StringBuilder(existingStyle);
        if (!existingStyle.endsWith(";") && !existingStyle.isEmpty()) {
            sb.append("; ");
        }
        sb.append(String.format(java.util.Locale.US,
                "-fx-font-size: %.1fpx;",
                baseFont));
        scene.getRoot().setStyle(sb.toString());
    }

    /**
     * Ajusta las dimensiones de un diálogo de forma segura para no desbordar
     * pantallas de 1366x768 u otras resoluciones, y centra la ventana.
     */
    public static void fitDialogToScreen(Stage stage, Window owner, double prefW, double prefH, double minW, double minH) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxAllowedW = bounds.getWidth() * 0.96;
        double maxAllowedH = bounds.getHeight() * 0.92;

        stage.setMinWidth(Math.min(minW, maxAllowedW));
        stage.setMinHeight(Math.min(minH, maxAllowedH));
        stage.setWidth(Math.min(prefW, maxAllowedW));
        stage.setHeight(Math.min(prefH, maxAllowedH));

        if (stage.getScene() != null) {
            applyResponsiveTheme(stage.getScene());
        }

        centerOnOwner(stage, owner);
    }
}
