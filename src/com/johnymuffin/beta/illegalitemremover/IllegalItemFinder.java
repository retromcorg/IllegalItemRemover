package com.johnymuffin.beta.illegalitemremover;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class IllegalItemFinder extends JavaPlugin implements Listener {
    //Basic Plugin Info
    private static IllegalItemFinder plugin;
    private Logger log;
    private String pluginName;
    private PluginDescriptionFile pdf;
    private ILFMaterial[] blockedMaterials;


    @Override
    public void onEnable() {
        plugin = this;
        log = this.getServer().getLogger();
        pdf = this.getDescription();
        pluginName = pdf.getName();
        log.info("[" + pluginName + "] Is Loading, Version: " + pdf.getVersion());
        Bukkit.getServer().getPluginManager().registerEvents(this, this);

        ILFConfig config = new ILFConfig(plugin);

        ArrayList<ILFMaterial> materials = new ArrayList<>();

        for (Object s : config.getBannedItems()) {
            String item = String.valueOf(s);
            if (!item.contains(":")) {
                if (!isInteger(item)) {
                    log.warning("[" + pdf.getName() + "] Invalid Input, Please Use Item ID: " + item);
                } else if (Material.getMaterial(Integer.valueOf(item)) == null) {
                    log.warning("[" + pdf.getName() + "] Invalid Item ID: " + item);
                } else {
                    log.info("[" + pdf.getName() + "] Looking for item: " + Material.getMaterial(Integer.valueOf(item)).name());
                    materials.add(new ILFMaterial(Integer.valueOf(item), 0, false));
                }
            } else {
                String[] itemDetails = item.split(":");
                if (!isInteger(itemDetails[0]) || !isInteger(itemDetails[1])) {
                    log.warning("[" + pdf.getName() + "] Invalid Input, Please Use Item ID: " + item);
                } else if (Material.getMaterial(Integer.valueOf(itemDetails[0])) == null) {
                    log.warning("[" + pdf.getName() + "] Invalid Item ID: " + item);
                } else {
                    log.info("[" + pdf.getName() + "] Looking for item: " + Material.getMaterial(Integer.valueOf(item)).name());
                    materials.add(new ILFMaterial(Integer.valueOf(itemDetails[0]), Integer.valueOf(itemDetails[1]), true));
                }
            }
        }


        //Timer to periodically scan players inventories. This will offset each scan by 1.5 seconds to minimize any performance impact.
        Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            int tickCount = 30;
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, () -> {
                    for (Player player1 : Bukkit.getOnlinePlayers()) {
                        if (player1.getUniqueId().equals(uuid)) {
                            scanPlayer(player1);
                            break;
                        }
                    }
                }, tickCount);
                tickCount = tickCount + 30;
            }
        }, 0, config.getInventorySearchPeriod());

    }


    @Override
    public void onDisable() {
        log.info("Plugin has been disabled.");
    }

    private static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

    public void removedItem(Player player, ItemStack itemStack, boolean notifyPlayer) {
        String damageValue = "0";
        if (itemStack.getData() != null) {
            damageValue = String.valueOf(itemStack.getData().getData());
        }

        logger(Level.INFO, itemStack.getAmount() + "x" + itemStack.getTypeId() + ":" + damageValue + " has been removed from the inventory of " + player.getName() + " (" + player.getUniqueId() + ")");
        if (notifyPlayer) {
            player.sendMessage(ChatColor.RED + "An illegal item " + itemStack.getTypeId() + ":" + damageValue + " has been removed from your inventory.");
        }
    }

    public boolean scanPlayer(Player player) {
        boolean removedItem = false;
        //Scan normal inventory
        for (ItemStack itemStack : player.getInventory().getContents()) {
            if (itemStack != null) {
                for (ILFMaterial bannedItem : blockedMaterials) {
                    if (bannedItem.matches(itemStack)) {
                        removedItem(player, itemStack, true);
                        player.getInventory().remove(itemStack);
                        removedItem = true;
                    }
                }
            }
        }
        //Scan armour slots
        for (ItemStack itemStack : player.getInventory().getArmorContents()) {
            if (itemStack != null) {
                for (ILFMaterial bannedItem : blockedMaterials) {
                    if (bannedItem.matches(itemStack)) {
                        removedItem(player, itemStack, true);
                        player.getInventory().remove(itemStack);
                        removedItem = true;
                    }
                }
            }
        }
        player.updateInventory(); //Might as well incase changes where made.
        return removedItem;
    }

    public void logger(Level level, String message) {
        Bukkit.getLogger().log(level, "[" + pluginName + "] " + message);
    }

    //Events

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        //Because of shitty plugins
        if (event.getPlayer() == null) {
            return;
        }

        scanPlayer(event.getPlayer());

    }

    @EventHandler
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.isCancelled() || event.getPlayer() == null) {
            return;
        }

        boolean illegalItem = false;
        for (ILFMaterial bannedMaterial : blockedMaterials) {
            if (bannedMaterial.matches(event.getItemDrop().getItemStack())) {
                illegalItem = true;
            }
        }

        if (illegalItem) {
            event.setCancelled(true);
        }


    }
}