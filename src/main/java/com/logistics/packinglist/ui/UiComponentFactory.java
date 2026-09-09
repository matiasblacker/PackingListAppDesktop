package com.logistics.packinglist.ui;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.paint.Color;

public class UiComponentFactory {

    public static Button createPdfButton(Runnable onAction) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.FILE_PDF_ALT);
        icon.setFill(Color.WHITE);
        icon.setSize("16px");
        Button btn = new Button(null, icon);
        btn.setStyle("-fx-background-color: #dc3545; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btn.setOnAction(e -> onAction.run());
        Tooltip.install(btn, new Tooltip("Exportar PDF"));
        return btn;
    }

    public static Button createPrintButton(Runnable onAction) {
        FontAwesomeIconView icon = new FontAwesomeIconView(FontAwesomeIcon.PRINT);
        icon.setFill(Color.WHITE);
        icon.setSize("16px");
        Button btn = new Button(null, icon);
        btn.setStyle("-fx-background-color: #4f46e5; -fx-cursor: hand; -fx-padding: 6px 12px; -fx-background-radius: 4px;");
        btn.setOnAction(e -> onAction.run());
        Tooltip.install(btn, new Tooltip("Imprimir"));
        return btn;
    }
}
