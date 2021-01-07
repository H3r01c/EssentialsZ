package me.darkwinged.Essentials.Events.World;

import me.darkwinged.Essentials.Main;
import me.darkwinged.Essentials.Utils.Lang.ErrorMessages;
import me.darkwinged.Essentials.Utils.Lang.Errors;
import me.darkwinged.Essentials.Utils.Lang.Permissions;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.UUID;

public class EnderPearlCooldown implements Listener {

    private Main plugin;
    public EnderPearlCooldown(Main plugin) {
        this.plugin = plugin;
    }

    public HashMap<UUID, Integer> cooldownTime = new HashMap<>();

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("Enderpearl_Cooldown", true)) {
            Player player = event.getPlayer();
            if (player.getItemInHand().getType().equals(Material.ENDER_PEARL)) {
                if (event.getAction().equals(Action.RIGHT_CLICK_AIR) || event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
                    if (player.hasPermission(Permissions.bypass) || player.hasPermission(Permissions.GlobalOverwrite))
                        return;
                    if (cooldownTime.containsKey(player.getUniqueId())) {
                        Utils.Message(player, Errors.getErrors(Errors.CooldownItem));
                        event.setCancelled(true);
                        player.updateInventory();
                        return;
                    }
                    cooldownTime.put(player.getUniqueId(), plugin.getConfig().getInt("Enderpearl_Cooldown_Time"));
                    new BukkitRunnable() {
                        public void run() {
                            if (!cooldownTime.containsKey(player.getUniqueId())) return;
                            if (cooldownTime.get(player.getUniqueId()) <= 0) {
                                cooldownTime.remove(player.getUniqueId());
                                cancel();
                                return;
                            }
                            // Removing 1 from the count
                            cooldownTime.put(player.getUniqueId(), cooldownTime.get(player.getUniqueId()) - 1);
                        }
                    }.runTaskTimer(plugin, 0L, 20L);
                }
            }
        }
    }

}