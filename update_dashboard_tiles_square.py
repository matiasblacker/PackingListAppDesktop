with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "r") as f:
    content = f.read()

# 1. Update crearCardKpi to square style
old_kpi = """    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(8, 12, 8, 12));
        card.setMinHeight(62);
        card.setMaxHeight(62);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("16px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white;");

        HBox topHeader = new HBox(6, iconView, lblTitle);
        topHeader.setAlignment(Pos.CENTER_LEFT);

        lblValor.setStyle("-fx-font-size: 15px; -fx-font-weight: bold; -fx-text-fill: white;");
        lblSubtext.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95);");

        card.getChildren().addAll(topHeader, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        ));

        return card;
    }"""

new_kpi = """    private VBox crearCardKpi(String titulo, FontAwesomeIcon icon, String colorHex, Label lblValor, Label lblSubtext) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMinHeight(130);
        card.setPrefHeight(130);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("28px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        lblValor.setStyle("-fx-font-size: 17px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");
        lblSubtext.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
        lblSubtext.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblValor, lblSubtext);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 8, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        ));

        return card;
    }"""

content = content.replace(old_kpi, new_kpi)

# 2. Update crearCardAccesoRapido to square style
old_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(2);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(8, 10, 8, 10));
        card.setMinHeight(65);
        card.setMaxHeight(65);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("20px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 9px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblSub);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 6, 0, 0, 3);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 4px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.1), 3, 0, 0, 1);"
        ));
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

new_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(6);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(16, 12, 16, 12));
        card.setMinHeight(130);
        card.setPrefHeight(130);
        card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("32px");
        iconView.setFill(Color.WHITE);

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: white; -fx-alignment: center;");

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 10px; -fx-font-weight: bold; -fx-text-fill: rgba(255, 255, 255, 0.95); -fx-alignment: center;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(iconView, lblTitle, lblSub);

        card.setOnMouseEntered(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 0.92;" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.22), 8, 0, 0, 4);"
        ));
        card.setOnMouseExited(e -> card.setStyle(
                "-fx-background-color: " + colorHex + ";" +
                "-fx-background-radius: 6px;" +
                "-fx-opacity: 1.0;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 4, 0, 0, 2);"
        ));
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

content = content.replace(old_acc, new_acc)

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "w") as f:
    f.write(content)

print("Updated tiles to square style successfully!")
