package me.HeyAwesomePeople.playerschems;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;

public class SchemsConfig {

    private PlayerSchems plugin = PlayerSchems.instance;

    private FileConfiguration customConfig = null;
    private File customConfigFile = null;

    public void reloadSchemsConfig() {
        if (customConfigFile == null) {
            customConfigFile = new File(plugin.getDataFolder(), "schems.yml");
        }
        customConfig = YamlConfiguration.loadConfiguration(customConfigFile);

        // Look for defaults in the jar
        InputStream defConfigStream = plugin.getResource("schems.yml");
        if (defConfigStream != null) {
            YamlConfiguration defConfig = YamlConfiguration.loadConfiguration(defConfigStream);
            customConfig.setDefaults(defConfig);
        }
    }

    public FileConfiguration getSchemsConfig() {
        if (customConfig == null) {
            reloadSchemsConfig();
        }
        return customConfig;
    }

    public void saveSchemsConfig() {
        if (customConfig == null || customConfigFile == null) {
            return;
        }
        try {
            getSchemsConfig().save(customConfigFile);
        } catch (IOException ex) {
           plugin.getLogger().log(Level.SEVERE, "Could not save config to " + customConfigFile, ex);
        }
    }

}
