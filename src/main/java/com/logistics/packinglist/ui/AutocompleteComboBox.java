package com.logistics.packinglist.ui;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.skin.ComboBoxListViewSkin;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.util.StringConverter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiPredicate;

public class AutocompleteComboBox<T> extends ComboBox<T> {
    private final ObservableList<T> originalItems = FXCollections.observableArrayList();
    private BiPredicate<T, String> filterPredicate;
    private boolean isUpdating = false;

    public AutocompleteComboBox() {
        this.setEditable(true);

        // Restore list and re-filter when popup is about to show
        this.setOnShowing(e -> {
            if (!isUpdating) {
                String currentText = this.getEditor().getText();
                this.getItems().setAll(originalItems);
                if (currentText != null && !currentText.isEmpty()) {
                    filterItems(currentText);
                }
            }
        });

        // Trigger show popup when editor is focused
        this.getEditor().focusedProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal) {
                Platform.runLater(this::show);
            }
        });

        // Listen for filter text typing
        this.getEditor().textProperty().addListener((obs, oldVal, newVal) -> {
            if (isUpdating) return;
            if (this.getEditor().isFocused()) {
                filterItems(newVal);
            }
        });

        // Handle enter key to confirm selection
        this.addEventHandler(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ENTER) {
                T selected = this.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    isUpdating = true;
                    this.setValue(selected);
                    this.getEditor().setText(selected.toString());
                    isUpdating = false;
                }
                this.hide();
                event.consume();
            }
        });

        // Custom string converter to match object text with user input safely
        this.setConverter(new StringConverter<T>() {
            @Override
            public String toString(T object) {
                return object != null ? object.toString() : "";
            }

            @Override
            public T fromString(String string) {
                T sel = getSelectionModel().getSelectedItem();
                if (sel != null && sel.toString().equals(string)) {
                    return sel;
                }
                for (T item : originalItems) {
                    if (item.toString().equals(string)) {
                        return item;
                    }
                }
                return null;
            }
        });
    }

    public void setFilterPredicate(BiPredicate<T, String> filterPredicate) {
        this.filterPredicate = filterPredicate;
    }

    public void setAllItems(List<T> items) {
        isUpdating = true;
        this.originalItems.setAll(items);
        this.getItems().setAll(items);
        isUpdating = false;
    }

    public void selectItem(T item) {
        isUpdating = true;
        this.getSelectionModel().select(item);
        if (item != null) {
            this.getEditor().setText(item.toString());
        } else {
            this.getEditor().clear();
        }
        isUpdating = false;
    }

    private void filterItems(String filterText) {
        if (filterPredicate == null || filterText == null || filterText.isEmpty()) {
            isUpdating = true;
            this.getItems().setAll(originalItems);
            isUpdating = false;
            return;
        }

        List<T> filtered = new ArrayList<>();
        String lowerFilter = filterText.toLowerCase();
        for (T item : originalItems) {
            if (filterPredicate.test(item, lowerFilter)) {
                filtered.add(item);
            }
        }

        isUpdating = true;
        this.getItems().setAll(filtered);
        isUpdating = false;

        if (!filtered.isEmpty()) {
            if (!this.isShowing()) {
                this.show();
            }
        } else {
            this.hide();
        }
    }
}
