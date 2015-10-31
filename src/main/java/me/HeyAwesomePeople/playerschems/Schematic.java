package me.HeyAwesomePeople.playerschems;

import com.sk89q.worldedit.*;
import com.sk89q.worldedit.bukkit.BukkitUtil;
import com.sk89q.worldedit.bukkit.BukkitWorld;
import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import com.sk89q.worldedit.data.DataException;
import com.sk89q.worldedit.schematic.SchematicFormat;
import com.sk89q.worldedit.session.ClipboardHolder;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;

/**
 * @author BlahBerrys (eballer48) - ericballard7@gmail.com
 *         <p>
 *         An easy-to-use API for saving, loading, and pasting WorldEdit/MCEdit
 *         schematics. (Built against WorldEdit 6.1)
 */

@SuppressWarnings("deprecation")
public class Schematic {

    public static PlayerSchems PLUGIN = PlayerSchems.instance;

    static File publicDir = new File(PLUGIN.getDataFolder(), "/schematics/" + "public" + "/");

    public static void save(Player player, String schematicName, boolean publicSave) {
        try {
            File sch = new File(PLUGIN.getDataFolder(), "/schematics/" + player.getName() + "/" + schematicName + ".schematic");
            if (publicSave) {
                sch = new File(PLUGIN.getDataFolder(), "/schematics/" + "public" + "/" + schematicName + ".schematic");
            }
            File dir = new File(PLUGIN.getDataFolder(), "/schematics/" + player.getName() + "/");
            if (publicSave) {
                dir = publicDir;
            }
            if (sch.exists()) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("save.exists").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
                return;
            }
            if (!dir.exists())
                //noinspection ResultOfMethodCallIgnored
                dir.mkdirs();

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

            //TODO figure out
            if (editSession.getBlockChangeCount() > PLUGIN.getConfig().getInt("blockSaveLimit")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("other.overSaveLimit").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix()).replace("%blocklimit%", "" + PLUGIN.getConfig().getInt("blockSaveLimit"))));
                editSession.flushQueue();
                return;
            }
            clipboard.copy(editSession);
            SchematicFormat.MCEDIT.save(clipboard, sch);
            editSession.flushQueue();

            if (publicSave) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("save.success").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
            } else {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("save.publicSuccess").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (EmptyClipboardException e) {
            player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("other.clipboardEmpty").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
        }
    }


    public static void paste(Player p, String schematicName, boolean publicPaste) {
        try {
            File dir = new File(PLUGIN.getDataFolder(), "/schematics/" + p.getName() + "/");
            if (publicPaste) {
                dir = publicDir;
            }
            if (dir.listFiles() == null) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("paste.notFound").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
                return;
            }


            boolean s = false;
            for (File f : dir.listFiles()) {
                if (f.getName().equals(schematicName + ".schematic")) {
                    //TODO test
                    dir = new File(dir.getAbsolutePath() + schematicName + ".schematic");
                    s = true;
                    break;
                }
            }

            if (!s) {
                p.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("paste.notFound").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
                return;
            }

            //TODO max blocks?
            EditSession editSession = new EditSession(new BukkitWorld(p.getWorld()), PLUGIN.getConfig().getInt("blockSaveLimit"));
            editSession.enableQueue();

            SchematicFormat schematic = SchematicFormat.getFormat(dir);
            CuboidClipboard clipboard = schematic.load(dir);

            clipboard.paste(editSession, BukkitUtil.toVector(p.getLocation()), true);
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("paste.success").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix())));
            editSession.flushQueue();
        } catch (DataException ex) {
            ex.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            p.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("other.overSaveLimit").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix()).replace("%blocklimit%", "" + PLUGIN.getConfig().getInt("blockSaveLimit"))));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}