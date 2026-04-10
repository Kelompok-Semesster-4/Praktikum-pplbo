package com.library.app.util;

import javax.swing.*;
import java.awt.*;

public final class UiUtil {
   private UiUtil() {
   }

   public static void showInfo(Component parent, String message) {
      JOptionPane.showMessageDialog(parent, message, "Informasi", JOptionPane.INFORMATION_MESSAGE);
   }

   public static void showError(Component parent, String message) {
      JOptionPane.showMessageDialog(parent, message, "Kesalahan", JOptionPane.ERROR_MESSAGE);
   }

   public static boolean confirm(Component parent, String message) {
      return JOptionPane.showConfirmDialog(parent, message, "Konfirmasi",
            JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
   }
}
