package com.pos.service;

import com.pos.dao.SettingsDAO;
import com.pos.model.Settings;

public class SettingsService {

    private final SettingsDAO settingsDAO;

    public SettingsService() {
        this.settingsDAO = new SettingsDAO();
    }

    /** Returns current settings (never null – falls back to defaults). */
    public Settings getSettings() {
        return settingsDAO.get();
    }

    /** Persists the settings to the database. */
    public void saveSettings(Settings settings) {
        settingsDAO.save(settings);
    }
}