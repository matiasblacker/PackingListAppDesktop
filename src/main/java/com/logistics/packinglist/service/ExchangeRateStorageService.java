package com.logistics.packinglist.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logistics.packinglist.model.ExchangeRateModel;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ExchangeRateStorageService {

    private static ExchangeRateStorageService instance;
    private final File configFile;
    private final Gson gson;
    private ExchangeRateModel cachedModel;
    private final List<Consumer<ExchangeRateModel>> listeners = new ArrayList<>();

    private ExchangeRateStorageService() {
        File dir = new File("config");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        this.configFile = new File(dir, "exchange_rate.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        load();
    }

    public static synchronized ExchangeRateStorageService getInstance() {
        if (instance == null) {
            instance = new ExchangeRateStorageService();
        }
        return instance;
    }

    private synchronized void load() {
        if (configFile.exists()) {
            try (FileReader reader = new FileReader(configFile)) {
                cachedModel = gson.fromJson(reader, ExchangeRateModel.class);
            } catch (Exception e) {
                System.err.println("Error al cargar tipo de cambio local: " + e.getMessage());
            }
        }
        if (cachedModel == null) {
            cachedModel = ExchangeRateModel.builder()
                    .exchangeRate(800.0)
                    .currencyFrom("USD")
                    .currencyTo("CLP")
                    .updatedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .updatedBy("Sistema")
                    .build();
            saveInternal();
        }
    }

    private synchronized void saveInternal() {
        try (FileWriter writer = new FileWriter(configFile)) {
            gson.toJson(cachedModel, writer);
        } catch (Exception e) {
            System.err.println("Error al guardar tipo de cambio local: " + e.getMessage());
        }
    }

    public synchronized ExchangeRateModel getModel() {
        if (cachedModel == null) {
            load();
        }
        return cachedModel;
    }

    public synchronized double getExchangeRate() {
        return getModel().getExchangeRate();
    }

    public synchronized void updateExchangeRate(double newRate, String user) {
        if (newRate <= 0) return;
        cachedModel.setExchangeRate(newRate);
        cachedModel.setUpdatedAt(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        cachedModel.setUpdatedBy(user != null ? user : "Usuario");
        saveInternal();
        notifyListeners();
    }

    public synchronized void addChangeListener(Consumer<ExchangeRateModel> listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void removeChangeListener(Consumer<ExchangeRateModel> listener) {
        listeners.remove(listener);
    }

    private void notifyListeners() {
        List<Consumer<ExchangeRateModel>> copy;
        synchronized (this) {
            copy = new ArrayList<>(listeners);
        }
        for (Consumer<ExchangeRateModel> listener : copy) {
            try {
                listener.accept(cachedModel);
            } catch (Exception e) {
                System.err.println("Error en listener de tipo de cambio: " + e.getMessage());
            }
        }
    }
}
