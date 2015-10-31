package me.HeyAwesomePeople.playerschems;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import java.io.File;

public class Schematics implements Listener {

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        makeSchemsFileForPlayer(e.getPlayer());
    }

    public PlayerSchems plugin = PlayerSchems.instance;

    public void makeSchemsFileForPlayer(Player p) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");
        if (dir.exists()) return;
        dir.mkdirs();
    }

    //TODO limiter
    public Integer howManySchems(Player p) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");
        if (dir.listFiles() == null) {
            return 0;
        }
        return dir.listFiles().length;
    }

    //TODO make it a pageable list
    public void listSchems(Player p, boolean isPublic) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");

        if (isPublic) {
            dir = new File(plugin.getDataFolder(), "/schematics/public/");
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.firstLinePublic").replace("%pre%", plugin.prefix())));
        } else {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.firstLine").replace("%pre%", plugin.prefix())));
        }

        if (dir.listFiles() == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.noneFound")));
            return;
        }
        int count = 1;
        for (File f : dir.listFiles()) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.otherLines").replace("%count%", count + "").replace("%pre%", plugin.prefix()).replace("%schem%", f.getName().replace(".schematic", ""))));
            count++;
        }
    }

    public void deleteSchem(Player p, String fileName) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");

        if (dir.listFiles() == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.alreadyDeleted").replace("%pre%", plugin.prefix()).replace("%schem%", fileName)));
            return;
        }

        for (File f : dir.listFiles()) {
            if (f.getName().equals(fileName + ".schematic")) {
                f.delete();
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.success").replace("%pre%", plugin.prefix()).replace("%schem%", fileName)));
                return;
            }
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.alreadyDeleted").replace("%pre%", plugin.prefix()).replace("%schem%", fileName)));
    }

    public void giveTool(Player p) {
        p.getInventory().addItem(new ItemStack(Material.WOOD_AXE));
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("other.toolGive").replace("%pre%", plugin.prefix())));
    }

}
