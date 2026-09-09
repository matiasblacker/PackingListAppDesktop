package com.logistics.packinglist;

public class Launcher {
    public static void main(String[] args) {
        // Al ejecutar MainApp desde una clase que NO hereda de Application,
        // evitamos que la Máquina Virtual de Java exija los módulos nativos de JavaFX en la ruta de módulos,
        // permitiendo que levante desde el JAR sombreado o empacado (Fat JAR).
        MainApp.main(args);
    }
}
