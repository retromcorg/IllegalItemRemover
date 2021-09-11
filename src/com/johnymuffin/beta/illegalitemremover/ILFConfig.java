package com.johnymuffin.beta.illegalitemremover;

import org.bukkit.util.config.Configuration;

import java.io.File;
import java.util.Arrays;
import java.util.List;

public class ILFConfig extends Configuration {

    private IllegalItemFinder plugin;
    private static IllegalItemFinder singleton;


    public ILFConfig(IllegalItemFinder plugin) {
        super(new File(plugin.getDataFolder(), "config.yml"));
        this.plugin = plugin;
        this.reload();
    }


    public void reload() {
        this.load();
        this.write();
        this.save();
    }

    private void write() {
        this.getBannedItems();
        this.getDiscordLoggingEnabled();
        this.getDiscordChannelID();
        this.getInventorySearchPeriod();

    }


    public List<Object> getBannedItems() {
        String key = "bannedItems";
        if(this.getList(key) == null) {
            List<String> defaultItems = Arrays.asList("36", "44:4", "44:5", "44:6", "44:7", "44:8", "44:9", "44:10");
            this.setProperty(key, defaultItems);
            defaultItems = null;
        }
        return this.getList(key);
    }

    public Boolean getDiscordLoggingEnabled() {
        String key = "discord.enable";
        if (this.getString(key) == null) {
            this.setProperty(key, false);
        }
        final Boolean roles = this.getBoolean(key, false);
        this.removeProperty(key);
        this.setProperty(key, roles);
        return roles;

    }

    public String getDiscordChannelID() {
        String key = "discord.channelID";
        if (this.getString(key) == null) {
            this.setProperty(key, "addDiscordID");
        }
        final String roles = this.getString(key);
        this.removeProperty(key);
        this.setProperty(key, roles);
        return roles;

    }

    public int getInventorySearchPeriod() {
        String key = "inventoryCheckTicks";
        if (this.getString(key) == null) {
            this.setProperty(key, 1200);
        }
        final int searchPeriod = this.getInt(key, 6000);
        this.removeProperty(key);
        this.setProperty(key, searchPeriod);
        return searchPeriod;
    }


}