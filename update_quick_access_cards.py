with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "r") as f:
    content = f.read()

old_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
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

new_acc = """    private VBox crearCardAccesoRapido(String titulo, String subtext, FontAwesomeIcon icon, String colorHex,
            Runnable action) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(14, 16, 14, 16));
        card.setMinHeight(115);
        card.setStyle(
                "-fx-background-color: white;" +
                "-fx-background-radius: 10px;" +
                "-fx-border-radius: 10px;" +
                "-fx-border-width: 0px 0px 0px 4px;" +
                "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                "-fx-cursor: hand;" +
                "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
        );

        FontAwesomeIconView iconView = new FontAwesomeIconView(icon);
        iconView.setSize("18px");
        iconView.setFill(Color.web(colorHex));

        String pastelBg = "#eff6ff";
        if ("#FF8A65".equalsIgnoreCase(colorHex)) pastelBg = "#fff7ed";
        else if ("#38a169".equalsIgnoreCase(colorHex)) pastelBg = "#f0fdf4";
        else if ("#805ad5".equalsIgnoreCase(colorHex)) pastelBg = "#faf5ff";

        StackPane iconBg = new StackPane(iconView);
        iconBg.setPrefSize(38, 38);
        iconBg.setMaxSize(38, 38);
        iconBg.setStyle("-fx-background-color: " + pastelBg + "; -fx-background-radius: 10px;");

        Label lblTitle = new Label(titulo);
        lblTitle.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #0f172a;");

        FontAwesomeIconView iconArrow = new FontAwesomeIconView(FontAwesomeIcon.ARROW_RIGHT);
        iconArrow.setSize("13px");
        iconArrow.setFill(Color.web("#94a3b8"));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox topRow = new HBox(10, iconBg, lblTitle, spacer, iconArrow);
        topRow.setAlignment(Pos.CENTER_LEFT);

        Label lblSub = new Label(subtext);
        lblSub.setStyle("-fx-font-size: 11px; -fx-font-weight: bold; -fx-text-fill: #64748b;");
        lblSub.setWrapText(true);

        card.getChildren().addAll(topRow, lblSub);

        card.setOnMouseEntered(e -> {
            card.setStyle(
                    "-fx-background-color: #f8fafc;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 0px 0px 0px 4px;" +
                    "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                    "-fx-cursor: hand;" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.12), 12, 0, 0, 4);"
            );
            iconArrow.setTranslateX(4);
            iconArrow.setFill(Color.web(colorHex));
        });
        card.setOnMouseExited(e -> {
            card.setStyle(
                    "-fx-background-color: white;" +
                    "-fx-background-radius: 10px;" +
                    "-fx-border-radius: 10px;" +
                    "-fx-border-width: 0px 0px 0px 4px;" +
                    "-fx-border-color: transparent transparent transparent " + colorHex + ";" +
                    "-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.05), 8, 0, 0, 2);"
            );
            iconArrow.setTranslateX(0);
            iconArrow.setFill(Color.web("#94a3b8"));
        });
        card.setOnMouseClicked(e -> action.run());

        return card;
    }"""

content = content.replace(old_acc, new_acc)

with open("src/main/java/com/logistics/packinglist/ui/MainController.java", "w") as f:
    f.write(content)

print("Updated Quick Access cards successfully!")
