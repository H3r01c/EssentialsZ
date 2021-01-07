package me.darkwinged.Essentials.Events.World;

import me.darkwinged.Essentials.Main;
import me.darkwinged.Essentials.Utils.Lang.ErrorMessages;
import me.darkwinged.Essentials.Utils.Lang.Errors;
import me.darkwinged.Essentials.Utils.Lang.Permissions;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class CommandCooldown implements Listener {

    private Main plugin;
    public CommandCooldown(Main plugin) {
        this.plugin = plugin;
    }

    HashMap<UUID, Integer> cooldown = new HashMap<>();
    HashMap<UUID, Integer> amount = new HashMap<>();

    @EventHandler
    public void commands(PlayerCommandPreprocessEvent event) {
        if (plugin.getConfig().getBoolean("Command_Cooldown")) {
            Player player = event.getPlayer();
            if (player.hasPermission(Permissions.bypass) || player.hasPermission(Permissions.GlobalOverwrite)) return;
            if (!amount.containsKey(player.getUniqueId())) {
                amount.put(player.getUniqueId(), 1);
                return;
            }
            if (amount.get(player.getUniqueId()) != plugin.getConfig().getInt("Command_Amount")) {
                amount.put(player.getUniqueId(), amount.get(player.getUniqueId()) + 1);
                return;
            }
            if (!cooldown.containsKey(player.getUniqueId())) {
                cooldown.put(player.getUniqueId(), plugin.getConfig().getInt("Command_Cooldown_Time"));
                new BukkitRunnable() {
                    public void run() {
                        if (!cooldown.containsKey(player.getUniqueId())) return;
                        if (cooldown.get(player.getUniqueId()) <= 0) {
                            cooldown.remove(player.getUniqueId());
                            amount.remove(player.getUniqueId());
                            cancel();
                            return;
                        }
                        // Removing 1 from the count
                        cooldown.put(player.getUniqueId(), cooldown.get(player.getUniqueId()) - 1);
                    }
                }.runTaskTimer(plugin, 0L, 20L);
                return;
            }
            Utils.Message(player, Errors.getErrors(Errors.Cooldown));
            event.setCancelled(true);
        }

    }

}
