package com.library.app;

import com.library.app.bootstrap.DatabaseInitializer;

public class Main {
    public static void main(String[] args) {
        DatabaseInitializer.initialize();
        System.out.println("Inisialisasi database selesai. Aplikasi siap digunakan.");
    }
}
