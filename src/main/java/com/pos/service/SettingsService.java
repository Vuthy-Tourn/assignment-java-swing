package com.pos.service;

import com.pos.dao.SettingsDAO;
import com.pos.model.Settings;

public class SettingsService {
    
    private final SettingsDAO settingsDAO;

    // Constructor initializes the database worker (DAO)
    public SettingsService() {
        this.settingsDAO = new SettingsDAO();
    }

    /**
     * Retrieves the current store settings.
     * If no settings exist in the DB, it returns the defaults.
     */
    public Settings getSettings() {
        return settingsDAO.get();
    }

    /**
     * Saves or updates the store settings after verifying business logic.
     */
    public void saveSettings(Settings settings) {
        // Business Rule validation: Make sure the store name is not left empty
        if (settings.getStoreName() == null || settings.getStoreName().trim().isEmpty()) {
            throw new IllegalArgumentException("Store name cannot be blank!");
        }
        
        // Business Rule validation: Tax percentage shouldn't be negative
        if (settings.getTaxPercentage() != null && settings.getTaxPercentage().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Tax percentage cannot be negative!");
        }

        // If everything looks great, save it to the database!
        settingsDAO.save(settings);
    }
}