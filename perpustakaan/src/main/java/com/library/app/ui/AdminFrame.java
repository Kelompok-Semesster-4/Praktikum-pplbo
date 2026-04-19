package com.library.app.ui;

import javafx.application.Application;
import javafx.stage.Stage;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

public class AdminFrame extends Application {
   @Override
   public void start(Stage stage) {
      try {
         Class<?> dashboardClass = Class.forName("com.library.app.ui.panel.AdminDashboardFxApp");
         Constructor<?> constructor = dashboardClass.getDeclaredConstructor();
         constructor.setAccessible(true);
         Object dashboardApp = constructor.newInstance();
         Method showDashboard = dashboardClass.getDeclaredMethod("showDashboard", Stage.class);
         showDashboard.setAccessible(true);
         showDashboard.invoke(dashboardApp, stage);
      } catch (Exception exception) {
         throw new RuntimeException("Gagal membuka dashboard admin JavaFX.", exception);
      }
   }

   public void showDashboard(Stage stage) {
      try {
         start(stage);
      } catch (Exception exception) {
         throw new RuntimeException("Gagal membuka dashboard admin JavaFX.", exception);
      }
   }
}
