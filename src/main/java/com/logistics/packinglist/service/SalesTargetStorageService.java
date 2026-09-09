package com.logistics.packinglist.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.logistics.packinglist.model.SalesTargetModel;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class SalesTargetStorageService {

    private static SalesTargetStorageService instance;
    private final Path filePath;
    private final Gson gson;
    private final Map<String, SalesTargetModel> targetsMap = new HashMap<>();

    private SalesTargetStorageService() {
        Path configDir = Paths.get("config");
        this.filePath = configDir.resolve("sales_targets.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();

        try {
            if (!Files.exists(configDir)) {
                Files.createDirectories(configDir);
            }
            cargarMetas();
        } catch (IOException e) {
            System.err.println("Error inicializando el almacenamiento de metas de ventas: " + e.getMessage());
        }
    }

    public static synchronized SalesTargetStorageService getInstance() {
        if (instance == null) {
            instance = new SalesTargetStorageService();
        }
        return instance;
    }

    public String generateKey(int year, int month) {
        return String.format("%04d-%02d", year, month);
    }

    public synchronized SalesTargetModel getTarget(int year, int month) {
        String key = generateKey(year, month);
        if (targetsMap.containsKey(key)) {
            return targetsMap.get(key);
        }

        // Default target if none configured
        LocalDate date = LocalDate.of(year, month, 1);
        String monthName = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("es", "ES"));
        monthName = monthName.substring(0, 1).toUpperCase() + monthName.substring(1);

        SalesTargetModel defaultTarget = SalesTargetModel.builder()
                .monthKey(key)
                .year(year)
                .month(month)
                .monthName(monthName)
                .targetUsd(2000000.0) // $2,000,000 USD default
                .targetClp(1600000000.0) // $1,600,000,000 CLP default
                .exchangeRate(800.0)
                .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                .build();

        return defaultTarget;
    }

    public synchronized void saveTarget(SalesTargetModel target) throws IOException {
        if (target == null || target.getMonthKey() == null) return;
        
        target.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        targetsMap.put(target.getMonthKey(), target);
        
        guardarEnDisco();
    }

    private void cargarMetas() {
        if (!Files.exists(filePath)) return;

        try (Reader reader = new FileReader(filePath.toFile())) {
            Type mapType = new TypeToken<Map<String, SalesTargetModel>>() {}.getType();
            Map<String, SalesTargetModel> loadedMap = gson.fromJson(reader, mapType);
            if (loadedMap != null) {
                targetsMap.putAll(loadedMap);
            }
        } catch (Exception e) {
            System.err.println("Error al cargar metas de ventas desde disco: " + e.getMessage());
        }
    }

    private void guardarEnDisco() throws IOException {
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(targetsMap, writer);
        }
    }
}
