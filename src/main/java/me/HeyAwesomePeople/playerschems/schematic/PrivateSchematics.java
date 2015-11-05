package me.HeyAwesomePeople.playerschems.schematic;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import me.HeyAwesomePeople.playerschems.PlayerSchems;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

public class PrivateSchematics {
    public static PlayerSchems plugin = PlayerSchems.instance;

    public PrivateSchematics() {
    }

    public String getString(String s) {
        return ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString(s).replace("%pre%", plugin.prefix()));
    }

    public void save(Player player, String schematicName) {
        try {
            if (canPlayerUpload(player)) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("save.private.limit").replace("%schem%", schematicName.toLowerCase()).replace("%savelimit%", "" + playerMaxPublicUploads(player))));
                return;
            }

            File dir = new File(plugin.getDataFolder(), "/schematics/" + player.getName() + "/");
            if (!dir.exists()) dir.mkdirs();
            File sch = new File(plugin.getDataFolder(), "/schematics/" + player.getName() + "/" + schematicName.toLowerCase() + ".schematic");

            if (sch.exists()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("save.private.exists").replace("%schem%", schematicName.toLowerCase())));
                return;
            }

            WorldEditPlugin wep = (WorldEditPlugin) Bukkit.getServer().getPluginManager().getPlugin("WorldEdit");
            WorldEdit we = wep.getWorldEdit();
            LocalPlayer localPlayer = wep.wrapPlayer(player);
            LocalSession localSession = we.getSession(localPlayer);
            ClipboardHolder selection = localSession.getClipboard();
            EditSession editSession = localSession.createEditSession(localPlayer);
            Vector min = selection.getClipboard().getMinimumPoint();
            Vector max = selection.getClipboard().getMaximumPoint();
            editSession.enableQueue();
            CuboidClipboard clipboard = new CuboidClipboard(max.subtract(min).add(new Vector(1, 1, 1)), min);

            if (clipboard.getSize().getBlockX() * clipboard.getSize().getBlockY() * clipboard.getSize().getBlockZ() > plugin.getConfig().getInt("blockSaveLimit")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("other.overSaveLimit").replace("%schem%", schematicName.toLowerCase()).replace("%pre%", plugin.prefix()).replace("%blocklimit%", "" + plugin.getConfig().getInt("blockSaveLimit"))));
                editSession.flushQueue();
                return;
            }

            clipboard.copy(editSession);
            SchematicFormat.MCEDIT.save(clipboard, sch);
            editSession.flushQueue();
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("save.private.success").replace("%schem%", schematicName.toLowerCase())));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (EmptyClipboardException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("other.clipboardEmpty").replace("%schem%", schematicName.toLowerCase())));
        }
    }

    public void paste(Player p, String schematicName) {
        try {
            File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");
            if (dir.listFiles() == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("paste.private.notFound").replace("%schem%", schematicName.toLowerCase())));
                return;
            }

            boolean s = false;
            for (File f : dir.listFiles()) {
                if (f.getName().equals(schematicName.toLowerCase() + ".schematic")) {
                    dir = new File(dir, schematicName.toLowerCase() + ".schematic");
                    s = true;
                    break;
                }
            }

            if (!s) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("paste.private.notFound").replace("%schem%", schematicName.toLowerCase())));
                return;
            }

            EditSession editSession = new EditSession(new BukkitWorld(p.getWorld()), plugin.getConfig().getInt("blockSaveLimit"));
            editSession.enableQueue();
            SchematicFormat schematic = SchematicFormat.getFormat(dir);
            CuboidClipboard clipboard = schematic.load(dir);

            if (clipboard.getSize().getBlockX() * clipboard.getSize().getBlockY() * clipboard.getSize().getBlockZ() > plugin.getConfig().getInt("blockSaveLimit")) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("other.overSaveLimit").replace("%schem%", schematicName.toLowerCase()).replace("%pre%", plugin.prefix()).replace("%blocklimit%", "" + plugin.getConfig().getInt("blockSaveLimit"))));
                editSession.flushQueue();
                return;
            }

            clipboard.paste(editSession, BukkitUtil.toVector(p.getLocation()), true);

            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("paste.private.success").replace("%schem%", schematicName.toLowerCase())));
            editSession.flushQueue();
        } catch (DataException ex) {
            ex.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getString("other.overSaveLimit").replace("%schem%", schematicName.toLowerCase()).replace("%blocklimit%", "" + plugin.getConfig().getInt("blockSaveLimit"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void delete(Player p, String fileName) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");

        if (dir.listFiles() == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.private.alreadyDeleted").replace("%pre%", plugin.prefix()).replace("%schem%", fileName.toLowerCase())));
            return;
        }

        for (File f : dir.listFiles()) {
            if (f.getName().equals(fileName.toLowerCase() + ".schematic")) {
                f.delete();
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.private.success").replace("%pre%", plugin.prefix()).replace("%schem%", fileName.toLowerCase())));
                return;
            }
        }
        p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("delete.private.alreadyDeleted").replace("%pre%", plugin.prefix()).replace("%schem%", fileName.toLowerCase())));
    }


    public void list(Player p, Integer page) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");
        p.sendMessage(getString("list.pagesWarning"));
        p.sendMessage(getString("list.firstLinePublic"));

        if (dir.listFiles() == null) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.noneFound")));
            return;
        }

        //TODO test
        int max = page * 10;
        int count = (max - 10) + 1;
        int realCount = (max - 10);
        for (File f : dir.listFiles()) {
            if (realCount != 0) {
                realCount--;
                continue;
            }
            if (count <= max) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', plugin.getConfig().getString("list.otherLines").replace("%count%", count + "").replace("%pre%", plugin.prefix()).replace("%schem%", f.getName().toLowerCase().replace(".schematic", ""))));
            }
            count++;
        }
    }


    /* UTIL */

    public Integer playerMaxPublicUploads(Player p) {
        int limit = 1;
        for (String s : plugin.getConfig().getConfigurationSection("limiter").getKeys(false)) {
            if (p.hasPermission("playerschems.save.limit." + s)) {
                limit = plugin.getConfig().getInt("limiter." + s);
            }
        }
        return limit;
    }

    public boolean canPlayerUpload(Player p) {
        return (getPlayerUploads(p) >= playerMaxPublicUploads(p));
    }

    public Integer getPlayerUploads(Player p) {
        File dir = new File(plugin.getDataFolder(), "/schematics/" + p.getName() + "/");
        if (dir.listFiles() == null) {
            return 0;
        }
        return dir.listFiles().length;
    }

}
