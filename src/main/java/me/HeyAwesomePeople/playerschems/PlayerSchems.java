package me.HeyAwesomePeople.playerschems;


import com.sk89q.worldedit.bukkit.WorldEditPlugin;
import me.HeyAwesomePeople.playerschems.schematic.PrivateSchematics;
import me.HeyAwesomePeople.playerschems.schematic.PublicSchematics;
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

    public PublicSchematics publicSchems;
    public PrivateSchematics privateSchematics;

    public SchemsConfig schemsConfig;
    public Schematics schems;

    private File fileconfig = new File(this.getDataFolder() + File.separator + "config.yml");
    private File schemsconfig = new File(this.getDataFolder() + File.separator + "schems.yml");

    WorldEditPlugin wep = null;

    @Override
    public void onEnable() {
        instance = this;
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

        wep = (WorldEditPlugin) Bukkit.getPluginManager().getPlugin("WorldEdit");
        if (wep == null) {
            Bukkit.getConsoleSender().sendMessage(prefix() + ChatColor.RED + "[Schems] WorldEdit not found! This plugin will not work and will now shut down!");
            Bukkit.getPluginManager().disablePlugin(this);
        }

        publicSchems = new PublicSchematics();
        privateSchematics = new PrivateSchematics();
        schems = new Schematics();
        schemsConfig = new SchemsConfig();
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
                p.sendMessage(ChatColor.AQUA + "/ps list [page] [-public] - List your schematics [List public schematics]");
                p.sendMessage(ChatColor.AQUA + "/ps paste <name> [-public] - Paste loaded schematic [Paste public schematic]");
                p.sendMessage(ChatColor.AQUA + "/ps delete <name> [-public] - Delete one of your schematics [Delete public schematic]");
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
                        if (args[2].equalsIgnoreCase("-public")) {
                            if (!p.hasPermission("playerschems.list.public")) {
                                p.sendMessage(prefix() + ChatColor.RED + "No permission");
                                return false;
                            }
                            publicSchems.list(p, Integer.parseInt(args[1]));
                        } else {
                            privateSchematics.list(p, Integer.parseInt(args[1]));
                        }
                    } else if (args.length == 2) {
                        if (args[1].equalsIgnoreCase("-public")) {
                            if (!p.hasPermission("playerschems.list.public")) {
                                p.sendMessage(prefix() + ChatColor.RED + "No permission");
                                return false;
                            }
                            publicSchems.list(p, 1);
                        } else {
                            privateSchematics.list(p, Integer.parseInt(args[1]));
                        }
                    } else {
                        privateSchematics.list(p, 1);
                    }
                } else if (args[0].equalsIgnoreCase("save")) {
                    if (!p.hasPermission("playerschems.save")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 1) return false;
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("-public")) {
                            if (!p.hasPermission("playerschems.save.public")) {
                                p.sendMessage(prefix() + ChatColor.RED + "No permission");
                                return false;
                            }
                            publicSchems.save(p, args[1]);
                        } else {
                            privateSchematics.save(p, args[1]);
                        }
                    } else {
                        privateSchematics.save(p, args[1]);
                    }
                } else if (args[0].equalsIgnoreCase("delete")) {
                    if (!p.hasPermission("playerschems.delete")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 1) return false;
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("-public")) {
                            if (!p.hasPermission("playerschems.delete.public")) {
                                p.sendMessage(prefix() + ChatColor.RED + "No permission");
                                return false;
                            }
                            publicSchems.delete(p, args[1]);
                        } else {
                            privateSchematics.delete(p, args[1]);
                        }
                    } else {
                        privateSchematics.delete(p, args[1]);
                    }
                } else if (args[0].equalsIgnoreCase("paste")) {
                    if (!p.hasPermission("playerschems.paste")) {
                        p.sendMessage(prefix() + ChatColor.RED + "No permission");
                        return false;
                    }
                    if (args.length == 1) return false;
                    if (args.length == 3) {
                        if (args[2].equalsIgnoreCase("-public")) {
                            if (!p.hasPermission("playerschems.paste.public")) {
                                p.sendMessage(prefix() + ChatColor.RED + "No permission");
                                return false;
                            }
                            publicSchems.paste(p, args[1]);
                        } else {
                            privateSchematics.paste(p, args[1]);
                        }
                    } else {
                        privateSchematics.paste(p, args[1]);
                    }
                } else {
                    p.sendMessage(prefix() + ChatColor.RED + "Subcommand does not exist.");
                }
            }
        }
        return false;
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
