package com.logistics.packinglist.service;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.logistics.packinglist.model.PackingList;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class PackingStorageService {

    private final Path storageDir;
    private final Gson gson;

    public PackingStorageService() {
        AuthService auth = AuthService.getInstance();
        Path base = Paths.get("packings");
        if ("ADMINSIS".equalsIgnoreCase(auth.getRole())) {
            this.storageDir = base.resolve("admin_global");
        } else {
            String company = auth.getCompanyId() != null ? auth.getCompanyId() : "default_company";
            String warehouse = auth.getWarehouseId() != null ? auth.getWarehouseId() : "default_warehouse";
            this.storageDir = base.resolve(company).resolve(warehouse);
        }
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        
        try {
            if (!Files.exists(storageDir)) {
                Files.createDirectories(storageDir);
            }
        } catch (IOException e) {
            System.err.println("Error creando directorio de packings local: " + e.getMessage());
        }
    }

    public void guardar(PackingList pl) throws IOException {
        if (pl == null) return;
        
        String nombre = pl.getNumeroOrden();
        if (nombre == null || nombre.trim().isEmpty()) {
            if (pl.getNombreArchivo() != null && !pl.getNombreArchivo().isEmpty()) {
                 nombre = pl.getNombreArchivo().replace(".xlsx", "");
            } else {
                 nombre = "Generico_" + System.currentTimeMillis();
            }
        }
        
        // Sanitize filename
        nombre = nombre.replaceAll("[^a-zA-Z0-9.-]", "_");
        if (!nombre.endsWith(".json")) {
            nombre += ".json";
        }
        
        Path filePath = storageDir.resolve(nombre);
        
        try (Writer writer = new FileWriter(filePath.toFile())) {
            gson.toJson(pl, writer);
        }
    }

    public List<PackingList> listarTodos() {
        List<PackingList> lista = new ArrayList<>();
        if (!Files.exists(storageDir)) return lista;
        
        try (Stream<Path> paths = Files.walk(storageDir)) {
            paths.filter(Files::isRegularFile)
                 .filter(p -> p.toString().endsWith(".json"))
                 .forEach(p -> {
                     try (Reader reader = new FileReader(p.toFile())) {
                         PackingList pl = gson.fromJson(reader, PackingList.class);
                         if (pl != null) {
                             if(pl.getNombreArchivo() == null || pl.getNombreArchivo().isEmpty()) {
                                 pl.setNombreArchivo(p.getFileName().toString());
                             }
                             lista.add(pl);
                         }
                     } catch (Exception e) {
                         System.err.println("Error leyendo archivo json: " + p.toString());
                     }
                 });
        } catch (IOException e) {
            System.err.println("Error leyendo directorio packings: " + e.getMessage());
        }
        
        return lista;
    }
    
    public void eliminar(PackingList pl) {
         if (pl == null) return;
         String nombre = pl.getNumeroOrden();
         if (nombre == null || nombre.trim().isEmpty()) return;
         nombre = nombre.replaceAll("[^a-zA-Z0-9.-]", "_");
         if (!nombre.endsWith(".json")) nombre += ".json";
         
         Path filePath = storageDir.resolve(nombre);
         try {
             Files.deleteIfExists(filePath);
         } catch(IOException e) {
             System.err.println("No se pudo eliminar " + filePath.toString());
         }
    }
}
