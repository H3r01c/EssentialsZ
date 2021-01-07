package me.darkwinged.Essentials.Commands.Chat;

import me.darkwinged.Essentials.Main;
import me.darkwinged.Essentials.Utils.Lang.ErrorMessages;
import me.darkwinged.Essentials.Utils.Lang.Errors;
import me.darkwinged.Essentials.Utils.Lang.Permissions;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class cmd_Staffchat implements CommandExecutor {

    private Main plugin;
    public cmd_Staffchat(Main plugin) { this.plugin = plugin; }

    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (cmd.getName().equalsIgnoreCase("staffchat")) {
            if (plugin.getConfig().getBoolean("Chat", true)) {
                if (plugin.getConfig().getBoolean("Staff_Chat", true)) {
                    if (!(sender instanceof Player)) {
                        if (!(args.length >= 1)) {
                            Utils.Message(sender, Errors.getErrors(Errors.MessageEmpty));
                            return true;
                        }
                        String msg = Utils.chat(plugin.getConfig().getString("Staff_Chat_Format").replaceAll("%player%", sender.getName()));
                        for (String s : args) {
                            msg = msg + " " + s;
                        }
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            if (player.hasPermission(Permissions.StaffChat) || player.hasPermission(Permissions.GlobalOverwrite)) {
                                if (!Utils.staff_chat.contains(player))
                                    player.sendMessage(msg);
                            }
                        }
                        return true;
                    }
                    Player player = (Player)sender;
                    if (player.hasPermission(Permissions.StaffChat) || player.hasPermission(Permissions.GlobalOverwrite)) {
                        if (!(args.length >= 1)) {
                            Utils.Message(sender, Errors.getErrors(Errors.MessageEmpty));
                            return true;
                        }

                        String msg = Utils.chat(plugin.getConfig().getString("Staff_Chat_Format").replaceAll("%player%", player.getName()));
                        for (String s : args) {
                            msg = msg + " " + s;
                        }
                        for (Player online : Bukkit.getOnlinePlayers()) {
                            if (online.hasPermission(Permissions.StaffChat) || online.hasPermission(Permissions.GlobalOverwrite)) {
                                if (!Utils.staff_chat.contains(online))
                                    online.sendMessage(msg);
                            }
                        }
                    } else {
                        Utils.Message(sender, Errors.getErrors(Errors.NoPermission));
                    }
                }
            }
        }
        return false;
    }

}