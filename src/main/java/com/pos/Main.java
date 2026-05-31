package com.pos;

import com.formdev.flatlaf.FlatLightLaf;
import com.pos.ui.MainFrame;
import com.pos.ui.dialogs.LoginDialog;

import javax.swing.*;

public class Main {

    public static void main(String[] args) {
        // Apply modern FlatLaf look and feel
        try {
            FlatLightLaf.setup();
            UIManager.put("Button.arc", 8);
            UIManager.put("Component.arc", 8);
            UIManager.put("TextComponent.arc", 6);
            UIManager.put("ScrollBar.thumbArc", 999);
            UIManager.put("ScrollBar.width", 8);
            UIManager.put("Table.rowHeight", 32);
        } catch (Exception e) {
            System.err.println("Could not apply FlatLaf: " + e.getMessage());
        }

        SwingUtilities.invokeLater(() -> {
            // Show login dialog first
            LoginDialog loginDialog = new LoginDialog(null);
            loginDialog.setVisible(true);

            if (!loginDialog.isLoggedIn()) {
                System.exit(0);
                return;
            }

            // Show main frame
            MainFrame frame = new MainFrame();
            frame.setVisible(true);
        });
    }
}
