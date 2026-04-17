package com.library.app.ui;

import javafx.application.Application;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.stage.Stage;

import javafx.application.Platform;
import javafx.stage.Stage;

import javax.swing.*;

public class LoginFrame{
   private Stage stage;
   String Judul_halaman="Login Layanan Perpustakaan"; 
   public LoginFrame(){
      
   }

   public void showOn(Stage stage){
      this.stage=stage;
      Group root=new Group();
      Scene scene=new Scene(root);
      stage.setWidth(1024);
      stage.setHeight(650);
      stage.setScene(scene);
      stage.show();
  }
}
