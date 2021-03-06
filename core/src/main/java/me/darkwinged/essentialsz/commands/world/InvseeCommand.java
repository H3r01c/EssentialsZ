package me.darkwinged.essentialsz.commands.world;

import me.darkwinged.essentialsz.Main;
import me.darkwinged.essentialsz.libaries.lang.Messages.ErrorManager;
import me.darkwinged.essentialsz.libaries.lang.Messages.Errors;
import me.darkwinged.essentialsz.libaries.lang.Permissions;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    private final Main plugin = Main.getInstance;

    public boolean onCommand(CommandSender sender, Command cmd, String string, String[] args) {
        if (cmd.getName().equalsIgnoreCase("invsee")) {
            if (plugin.getConfig().getBoolean("Commands.Invsee", true)) {
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ErrorManager.getErrors(Errors.Console));
                    return true;
                }
                Player player = (Player)sender;
                if (player.hasPermission(Permissions.Invsee) || player.hasPermission(Permissions.GlobalOverwrite)) {
                    if (args.length != 1) {
                        sender.sendMessage(ErrorManager.getErrors(Errors.NoPlayerFound));
                        return true;
                    }
                    Player target = Bukkit.getPlayer(args[0]);
                    if (target == null) {
                        sender.sendMessage(ErrorManager.getErrors(Errors.NoPlayerFound));
                        return true;
                    }
                    player.openInventory(target.getInventory());
                } else {
                    sender.sendMessage(ErrorManager.getErrors(Errors.NoPermission));
                }
            }
        }
        return false;
    }

}
