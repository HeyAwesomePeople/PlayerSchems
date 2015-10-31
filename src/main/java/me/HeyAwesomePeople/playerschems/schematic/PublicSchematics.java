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

public class PublicSchematics {

    public static PlayerSchems PLUGIN = PlayerSchems.instance;

    static File publicDir = new File(PLUGIN.getDataFolder(), "/schematics/" + "public" + "/");

    public static String getString(String s) {
        return ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString(s)).replace("%pre%", PLUGIN.prefix());
    }

    public static void save(Player player, String schematicName) {
        try {
            File sch = new File(PLUGIN.getDataFolder(), "/schematics/" + player.getName() + "/" + schematicName + ".schematic");
            File dir = new File(PLUGIN.getDataFolder(), "/schematics/" + player.getName() + "/");


            if (sch.exists()) {
                player.sendMessage(getString("save.public.exists").replace("%schem%", schematicName));
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

            //TODO figure out
            if (editSession.getBlockChangeCount() > PLUGIN.getConfig().getInt("blockSaveLimit")) {
                player.sendMessage(ChatColor.translateAlternateColorCodes('&', PLUGIN.getConfig().getString("other.overSaveLimit").replace("%schem%", schematicName).replace("%pre%", PLUGIN.prefix()).replace("%blocklimit%", "" + PLUGIN.getConfig().getInt("blockSaveLimit"))));
                editSession.flushQueue();
                return;
            }

            clipboard.copy(editSession);
            SchematicFormat.MCEDIT.save(clipboard, sch);
            editSession.flushQueue();
            player.sendMessage(getString("save.public.success").replace("%schem%", schematicName));
        } catch (IOException ex) {
            ex.printStackTrace();
        } catch (DataException e) {
            e.printStackTrace();
        } catch (EmptyClipboardException e) {
            player.sendMessage(getString("other.clipboardEmpty").replace("%schem%", schematicName));
        }
    }


    public static void paste(Player p, String schematicName, boolean publicPaste) {
        try {
            File dir = null;
            if (publicDir.listFiles() == null) {
                p.sendMessage(getString("paste.public.notFound").replace("%schem%", schematicName));
                return;
            }


            boolean s = false;
            for (File f : publicDir.listFiles()) {
                if (f.getName().equals(schematicName + ".schematic")) {
                    dir = new File(publicDir, schematicName + ".schematic");
                    s = true;
                    break;
                }
            }

            if (!s) {
                p.sendMessage(getString("paste.public.notFound").replace("%schem%", schematicName));
                return;
            }

            EditSession editSession = new EditSession(new BukkitWorld(p.getWorld()), PLUGIN.getConfig().getInt("blockSaveLimit"));
            editSession.enableQueue();
            SchematicFormat schematic = SchematicFormat.getFormat(dir);
            CuboidClipboard clipboard = schematic.load(dir);
            clipboard.paste(editSession, BukkitUtil.toVector(p.getLocation()), true);

            p.sendMessage(getString("paste.public.success").replace("%schem%", schematicName));
            editSession.flushQueue();
        } catch (DataException ex) {
            ex.printStackTrace();
        } catch (MaxChangedBlocksException e) {
            p.sendMessage(getString("other.overSaveLimit").replace("%schem%", schematicName).replace("%blocklimit%", "" + PLUGIN.getConfig().getInt("blockSaveLimit")));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
