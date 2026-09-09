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
}
