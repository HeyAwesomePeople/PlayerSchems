package me.HeyAwesomePeople.playerschems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class Schematics {
    public PlayerSchems plugin = PlayerSchems.instance;

    public void giveTool(Player p) {
        p.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("other.toolGive").replace("%pre%", plugin.prefix())));
    }

}
