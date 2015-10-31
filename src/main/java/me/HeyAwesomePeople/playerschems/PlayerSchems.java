package me.HeyAwesomePeople.playerschems;


import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class PlayerSchems extends JavaPlugin implements CommandExecutor {
    public static PlayerSchems instance;

    private SchemsConfig schemsConfig;
    public Schematics schems;

    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File schemsconfig = new File(this.getDataFolder() + File.separator + "schems.yml");

    WorldEditPlugin wep = null;

    @Override
    public void onEnable() {
        instance = this;

        wep = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wep == null) {
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[Schems] Worldedit not found! This plugin will not work!");
        }
        if (!fileconfig.exists()) {
            this.saveDefaultConfig();
        }
        if (!schemsconfig.exists()) {
            try {
                schemsconfig.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        schems = new Schematics();
    }

    public String prefix() {
        return this.getConfig().getString("prefix");
    }

    @Override
    public void onDisable() {
        reloadConfig();
    }

    public boolean onCommand(final CommandSender sender, Command cmd,
                             String commandLabel, final String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(prefix() + ChatColor.RED + "You must be a player to run this command!");
            return false;
        }
        Player p = (Player) sender;
        if (commandLabel.equalsIgnoreCase("playerschems") || commandLabel.equalsIgnoreCase("schems") || commandLabel.equalsIgnoreCase("ps")) {
            if (args.length <= 0) {
                p.sendMessage(ChatColor.RED + "=== Player Schematics ===");
                p.sendMessage(ChatColor.AQUA + "/ps tool - Get a tool to select the schematic");
                p.sendMessage(ChatColor.AQUA + "/ps save <name> [-public] - Save the selected schematic [Save public schematic]");
                p.sendMessage(ChatColor.AQUA + "/ps list <page> [-public] - List your schematics [List public schematics]");
                p.sendMessage(ChatColor.AQUA + "/ps paste <name> [-public] - Paste loaded schematic [Paste public schematic]");
                p.sendMessage(ChatColor.AQUA + "/ps delete <name> - Delete one of your schematics");
                return false;
            } else {
                if (args[0].equalsIgnoreCase("reload")) {
                    if (!p.hasPermission("playerschems.reload")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                    }
                    p.sendMessage(prefix() + ChatColor.GREEN + "Configuration reloaded!");
                    this.reloadConfig();
                    return false;
                }
                if (args[0].equalsIgnoreCase("tool")) {
                    if (!p.hasPermission("playerschems.tool")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    schems.giveTool(p);
                } else if (args[0].equalsIgnoreCase("list")) {
                    if (!p.hasPermission("playerschems.list")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 3) {
                        schems.listSchems(p, args[2].equalsIgnoreCase("-public"));
                    } else {
                        schems.listSchems(p, false);
                    }
                } else if (args[0].equalsIgnoreCase("save")) {
                    if (!p.hasPermission("playerschems.save")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 3) {
                        Schematic.save(p, args[1], args[2].equalsIgnoreCase("-public"));
                    } else {
                        if (schems.howManySchems(p) >= getLimit(p)) {
                            p.sendMessage(ChatColor.translateAlternateColorCodes('&', getConfig().getString("other.atSaveLimit").replace("%pre%", prefix()).replace("%savelimit%", getLimit(p) + "")));
                            return false;
                        }
                        Schematic.save(p, args[1], false);
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (!p.hasPermission("playerschems.delete")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    schems.deleteSchem(p, args[1]);
                } else if (args[0].equalsIgnoreCase("paste")) {
                    if (!p.hasPermission("playerschems.paste")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 3) {
                        Schematic.paste(p, args[1], args[2].equalsIgnoreCase("-public"));
                    } else {
                        Schematic.paste(p, args[1], false);
                    }
                } else {
                    p.sendMessage(prefix() + ChatColor.RED + "Subcommand does not exist.");
                }
            }
        }
        return false;
    }


    public Integer getLimit(Player p) {
        int limit = 1;
        for (String s : getConfig().getConfigurationSection("limiter").getKeys(false)) {
            if (p.hasPermission("playerschems.limiter." + s)) {
                limit = getConfig().getInt("limiter." + s);
            }
        }
        return limit;
    }


    public static String locationToString(Location loc) {
        String s = "";
        s += loc.getBlockX();
        s += "_";
        s += loc.getBlockY();
        s += "_";
        s += loc.getBlockZ();
        s += "_";
        s += loc.getWorld().getName();
        return s;
    }

    public static Location stringToLocation(String s) {
        String[] l = s.split("_");// 0,x 1,y 2,z 3, worldmame
        if (Bukkit.getWorld(l[3]) == null) {
            return null;
        }
        return new Location(Bukkit.getWorld(l[3]), (double) Integer.parseInt(l[0]), (double) Integer.parseInt(l[1]), (double) Integer.parseInt(l[2]));
    }

}
