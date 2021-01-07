package me.darkwinged.Essentials.Events.Signs;

import me.darkwinged.Essentials.Main;
import me.darkwinged.Essentials.Utils.Lang.Permissions;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class Sign_Chestsell implements Listener {

    private Main plugin;
    public Sign_Chestsell(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void SignCreate(SignChangeEvent event) {
        if (plugin.getConfig().getBoolean("Economy.Settings.Sell.Chestsell Sign", true)) {
            Player player = event.getPlayer();
            Block block = event.getBlock();
            if (player.hasPermission(Permissions.GamemodeSign) || player.hasPermission(Permissions.GlobalOverwrite)) {
                String line0 = event.getLine(0);
                String line1 = event.getLine(1);
                if (line0 == null) return;
                if (line1 == null) return;
                if (line0.equalsIgnoreCase("[chestsell]")) {
                    if (!isSign(block))
                    if (!block.getType().equals(Material.CHEST)) {
                        player.sendMessage("No chest found, what i found was, " + block.getType().name());
                        event.setLine(0, Utils.chat("&4ERROR!"));
                        return;
                    }
                    event.setLine(0, Utils.chat("&e[!] &6Sell Sign"));
                    event.setLine(2, Utils.chat("&8(Right Click or"));
                    event.setLine(3, Utils.chat("&8Punch to sell.)"));
                }
            }
        }
    }

    @EventHandler
    public void SignInteract(PlayerInteractEvent event) {
        if (plugin.getConfig().getBoolean("Economy.Settings.Sell.Chestsell Sign", true)) {
            if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                Block block = event.getClickedBlock();
                if (block == null)
                    return;
                if (!isSign(block)) {
                    Sign sign = (Sign)block.getState();
                    String line0 = sign.getLine(0);
                    Player player = event.getPlayer();
                    if (line0.contains(Utils.chat("&e[!] &6Sell Sign"))) {

                    }
                }
            }
        }

    }

    private boolean isSign(Block block) {
        switch (block.getType()) {
            case SPRUCE_WALL_SIGN:
            case ACACIA_WALL_SIGN:
            case BIRCH_WALL_SIGN:
            case CRIMSON_WALL_SIGN:
            case DARK_OAK_WALL_SIGN:
            case JUNGLE_WALL_SIGN:
            case OAK_WALL_SIGN:
            case WARPED_WALL_SIGN:
                return false;
        }
        return true;
    }

}