package me.darkwinged.Essentials.Events.Economy;

import me.darkwinged.Essentials.Main;
import me.darkwinged.Essentials.Utils.EconomyManager;
import me.darkwinged.Essentials.Utils.Lang.ErrorMessages;
import me.darkwinged.Essentials.Utils.Lang.Errors;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;

import static java.lang.Math.round;

public class PlayerHeads implements Listener {

    private Main plugin;
    public PlayerHeads(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void PlayerHeadSell(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("Economy.enabled", true)) {
            if (plugin.getConfig().getBoolean("Economy.Settings.Money Heads.enabled", true)) {
                if (plugin.getConfig().getString("Economy.API").equalsIgnoreCase("Vault")) {
                } else if (plugin.getConfig().getString("Economy.API").equalsIgnoreCase("EssentialsZ")) {
                    Player player = event.getPlayer();
                    if (player.getItemInHand().getType().equals(Material.PLAYER_HEAD)) {
                        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                            String target = player.getItemInHand().getItemMeta().getDisplayName();
                            // Player Account
                            if (!EconomyManager.hasAccount(player.getName())) {
                                return;
                            }
                            // Target Account
                            if (!EconomyManager.hasAccount(target)) {
                                return;
                            }

                            // Getting the amount
                            double amount;
                            try {
                                amount = round((EconomyManager.getBalance(target) / 100) * plugin.getConfig().getInt("Economy.Settings.Money Heads.Player Heads Sell Amount"));
                            } catch(Exception e) {
                                Utils.Message(player, Errors.getErrors(Errors.InvalidAmount));
                                return;
                            }
                            // Removing the amount from the senders balance
                            EconomyManager.setBalance(player.getName(), EconomyManager.getBalance(player.getName()) + amount);
                            // Adding the amount to the target balance
                            EconomyManager.setBalance(target, EconomyManager.getBalance(target) - amount);
                            player.setItemInHand(new ItemStack(Material.AIR));
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void PlayerHeadChance(PlayerDeathEvent event) {
        if (plugin.getConfig().getBoolean("Economy.enabled", true)) {
            Player player = event.getEntity();
            Random rand = new Random();
            int  n = rand.nextInt(100) + 1;
            if (n <= plugin.getConfig().getInt("Economy.Settings.Money Heads.Player Heads Drop Chance")) {
                ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
                SkullMeta meta = (SkullMeta) skull.getItemMeta();
                meta.setOwner(player.getName());
                meta.setDisplayName(player.getName());
                skull.setItemMeta(meta);

                event.getDrops().add(skull);
            }
        }
    }

    @EventHandler
    public void PlacePlayerHead(BlockPlaceEvent event) {
        if (plugin.getConfig().getBoolean("Economy.enabled", true)) {
            Player player = event.getPlayer();
            if (event.getBlock().getType().equals(Material.PLAYER_HEAD)) {
                Utils.Message(player, Errors.getErrors(Errors.NoPlacePlayerHead));
                event.setCancelled(true);
            }
        }
    }

}