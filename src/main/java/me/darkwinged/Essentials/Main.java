package me.darkwinged.Essentials;

import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import me.darkwinged.Essentials.Commands.Chat.*;
import me.darkwinged.Essentials.Commands.Economy.*;
import me.darkwinged.Essentials.Commands.Teleport.Staff.*;
import me.darkwinged.Essentials.Commands.Teleport.*;
import me.darkwinged.Essentials.Commands.World.Gamemodes.*;
import me.darkwinged.Essentials.Commands.World.*;
import me.darkwinged.Essentials.Commands.cmd_Reload;
import me.darkwinged.Essentials.Events.Chat.ChatControl;
import me.darkwinged.Essentials.Events.Chat.Color;
import me.darkwinged.Essentials.Events.Chat.JoinAndLeaveMessage;
import me.darkwinged.Essentials.Events.Economy.AccountSetup;
import me.darkwinged.Essentials.Events.Economy.BankNotes;
import me.darkwinged.Essentials.Events.Economy.MoneyPouchesEvent;
import me.darkwinged.Essentials.Events.Economy.PlayerHeads;
import me.darkwinged.Essentials.Events.Signs.Sign_Balance;
import me.darkwinged.Essentials.Events.Signs.Sign_Gamemode;
import me.darkwinged.Essentials.Events.Teleport.Back;
import me.darkwinged.Essentials.Events.Teleport.NoVoid;
import me.darkwinged.Essentials.Events.Teleport.OnRespawn;
import me.darkwinged.Essentials.Events.Teleport.SpawnOnJoin;
import me.darkwinged.Essentials.Events.World.*;
import me.darkwinged.Essentials.Utils.EssentialsZEconomy.EconomyManager;
import me.darkwinged.Essentials.Utils.EssentialsZEconomy.Economy_EssentialsZ;
import me.darkwinged.Essentials.Utils.EssentialsZEconomy.VaultHook;
import me.darkwinged.Essentials.Utils.Lang.CustomConfig;
import me.darkwinged.Essentials.Utils.Lang.MetricsLite;
import me.darkwinged.Essentials.Utils.Lang.Utils;
import me.darkwinged.Essentials.Utils.PlaceHolders;
import me.darkwinged.Essentials.Utils.TeleportUtils;
import me.darkwinged.EssentialsZ.EssentialsZAPI;
import me.darkwinged.EssentialsZSpawn.EssentialsZSpawn;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

import static org.bukkit.Material.getMaterial;

public final class Main extends JavaPlugin implements Listener {

    public static Main getInstance;
    public EconomyManager economyManager;
    public ProtocolManager protocolManager;
    public Economy_EssentialsZ economy_essentialsZ;
    private VaultHook vaultHook;
    public EssentialsZAPI essentialsZAPI;
    public EssentialsZSpawn essentialsZSpawn;

    public int Delay = getConfig().getInt("Teleportation_Delay");
    public boolean Module_Economy = false;

    public CustomConfig MoneyPouchesFile = new CustomConfig(this, "Economy/Money Pouches");
    public CustomConfig MessagesFile = new CustomConfig(this, "Messages");
    public CustomConfig BlockedCommandsFile = new CustomConfig(this, "Chat/Blocked Commands");
    public CustomConfig ChatFilterFile = new CustomConfig(this, "Chat/Chat Filter");
    public CustomConfig AutoMessagesFile = new CustomConfig(this, "Chat/Auto Messages");
    public CustomConfig AccountsFile = new CustomConfig(this, "Economy/Accounts");
    public CustomConfig WorthFile = new CustomConfig(this, "Economy/Worth");


    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("EssentialsZAPI") == null) {
            getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&cDisabling plugin, No EssentialsZ API Found."));
            getServer().getPluginManager().disablePlugin(this);
        }
        getInstance = this;

        // Loading Files
        loadConfig();
        loadCustomFiles();

        // Getting ProtocolLib, Teleport Utils, MetricsLite, PlaceholderAPI, Vault, EssentialsZ APi, Bungeecord, Adding EconomyManager
        TeleportUtils teleportUtils = new TeleportUtils(this);
        MetricsLite metricsLite = new MetricsLite(this, 9811);
        Bukkit.getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");


        // Vault hook ================================================
        economyManager = new EconomyManager();
        economy_essentialsZ = new Economy_EssentialsZ();
        vaultHook = new VaultHook();
        vaultHook.hook();
        //=============================================================

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PlaceHolders().register();
        }
        if (Bukkit.getPluginManager().getPlugin("ProtocolLib") != null) {
            this.protocolManager = ProtocolLibrary.getProtocolManager();
        }

        // Loading content to ArrayList(s)
        loadMoneyPouches();
        loadAutoMessages();
        loadAccounts();

        // Registering Commands / Events / Loops
        registerCommands();
        registerEvents();
        AutoMessage();
        vanishCheck();

        // Console Start Message
        getServer().getConsoleSender().sendMessage(Utils.chat("&aEssentialsZ Core plugin has been enabled!"));
    }

    public void onDisable() {
        // Saving the accounts for all of the players
        saveAccounts();
        // Saving the accounts file
        AccountsFile.saveConfig();

        // Unhooking vault
        vaultHook.unhook();

        // Console Stop Message
        getServer().getConsoleSender().sendMessage(Utils.chat("&cEssentialsZ Core plugin has been disabled!"));
    }

    /* Message Command

      message:
        aliases:
          - pm
          - msg
          - dm
          - to
      reply:
        aliases:
          - r

     */

    @SuppressWarnings("ConstantConditions")
    public void registerCommands() {
        getCommand("essentials").setExecutor(new cmd_Reload(this));

        // Economy
        getCommand("economy").setExecutor(new cmd_Economy());
        getCommand("balance").setExecutor(new cmd_Balance());
        getCommand("pay").setExecutor(new cmd_Pay());
        getCommand("withdraw").setExecutor(new cmd_Withdraw());
        //getCommand("pouches").setExecutor(new cmd_MoneyPouches(this));
        //getCommand("autosell").setExecutor(new cmd_Autosell(this));
        //getCommand("sellhand").setExecutor(new cmd_Sellhand(this));
        //getCommand("sell").setExecutor(new cmd_Sell(this));

        // Chat
        getCommand("staffchat").setExecutor(new cmd_Staffchat(this));
        getCommand("clearchat").setExecutor(new cmd_Clearchat(this));
        getCommand("broadcast").setExecutor(new cmd_Broadcast(this));
        getCommand("mutechat").setExecutor(new cmd_Mutechat(this));
        getCommand("motd").setExecutor(new cmd_MOTD(this));
        getCommand("sudo").setExecutor(new cmd_Sudo(this));

        // Teleportation
        getCommand("tp").setExecutor(new cmd_TP(this));
        getCommand("tphere").setExecutor(new cmd_TPhere(this));
        getCommand("hub").setExecutor(new cmd_Hub(this));
        getCommand("rtp").setExecutor(new cmd_RandomTeleport(this));
        getCommand("top").setExecutor(new cmd_Top(this));
        getCommand("back").setExecutor(new cmd_Back(this));
        getCommand("setwarp").setExecutor(new cmd_SetWarp(this));
        getCommand("delwarp").setExecutor(new cmd_DelWarp(this));
        getCommand("warp").setExecutor(new cmd_Warp(this));
        getCommand("warps").setExecutor(new cmd_Warps(this));
        getCommand("sethome").setExecutor(new cmd_SetHome(this));
        getCommand("delhome").setExecutor(new cmd_DelHome(this));
        getCommand("home").setExecutor(new cmd_Home(this));
        getCommand("homes").setExecutor(new cmd_Homes(this));

        // World
        getCommand("hat").setExecutor(new cmd_Hat(this));
        getCommand("ping").setExecutor(new cmd_Ping(this));
        getCommand("gma").setExecutor(new cmd_AdventureMode(this));
        getCommand("gmc").setExecutor(new cmd_CreativeMode(this));
        getCommand("gms").setExecutor(new cmd_SurvivalMode(this));
        getCommand("gmsp").setExecutor(new cmd_SpectatorMode(this));
        getCommand("gamemode").setExecutor(new cmd_Gamemode(this));
        getCommand("invsee").setExecutor(new cmd_Invsee(this));
        getCommand("vanish").setExecutor(new cmd_Vanish(this));
        getCommand("reward").setExecutor(new cmd_Reward(this));
        getCommand("repair").setExecutor(new cmd_Repair(this));
        getCommand("world").setExecutor(new cmd_WorldGenerator(this));
        getCommand("enderchest").setExecutor(new cmd_Enderchest(this));
        getCommand("disposal").setExecutor(new cmd_Disposal(this));
        getCommand("craft").setExecutor(new cmd_Craft(this));
        getCommand("kill").setExecutor(new cmd_Kill(this));
        getCommand("god").setExecutor(new cmd_God(this));
        getCommand("heal").setExecutor(new cmd_Heal(this));
        getCommand("fly").setExecutor(new cmd_Fly(this));
        getCommand("feed").setExecutor(new cmd_Feed(this));

    }

    public void registerEvents() {
        // Registering events in this class
        getServer().getPluginManager().registerEvents(this, this);

        // Chat Events
        getServer().getPluginManager().registerEvents(new ChatControl(this), this);
        getServer().getPluginManager().registerEvents(new JoinAndLeaveMessage(this), this);
        getServer().getPluginManager().registerEvents(new Color(this), this);

        // Economy Events
        getServer().getPluginManager().registerEvents(new PlayerHeads(), this);
        getServer().getPluginManager().registerEvents(new AccountSetup(), this);
        getServer().getPluginManager().registerEvents(new BankNotes(), this);
        getServer().getPluginManager().registerEvents(new MoneyPouchesEvent(), this);

        // SIGN EVENTS
        getServer().getPluginManager().registerEvents(new Sign_Balance(this), this);
        getServer().getPluginManager().registerEvents(new Sign_Gamemode(this), this);

        // TELEPORTATION EVENTS
        getServer().getPluginManager().registerEvents(new NoVoid(this), this);
        getServer().getPluginManager().registerEvents(new OnRespawn(this), this);
        getServer().getPluginManager().registerEvents(new SpawnOnJoin(this), this);
        getServer().getPluginManager().registerEvents(new Back(this), this);

        // WORLD EVENTS
        getServer().getPluginManager().registerEvents(new NoItemDropAndPickup(this), this);
        getServer().getPluginManager().registerEvents(new NoPlaceBreakBlocks(this), this);
        getServer().getPluginManager().registerEvents(new ChangeMOTD(this), this);
        getServer().getPluginManager().registerEvents(new DoubleJump(this), this);
        getServer().getPluginManager().registerEvents(new cmd_Invsee(this), this);
        getServer().getPluginManager().registerEvents(new cmd_God(this), this);
        getServer().getPluginManager().registerEvents(new Tablist(), this);
        getServer().getPluginManager().registerEvents(new WorldControl(this), this);
    }

    // Economy Account Manager
    public void saveAccounts() {
        for (Map.Entry<UUID, Double> entry : economyManager.getAccountMap().entrySet()) {
            AccountsFile.getConfig().set("Accounts." + entry.getKey(), entry.getValue());
        }
    }
    public void loadAccounts() {
        if (!AccountsFile.getConfig().contains("Accounts.")) return;
        for (String key : AccountsFile.getConfig().getConfigurationSection("Accounts.").getKeys(false)) {
            economyManager.BankAccounts.put(UUID.fromString(key), AccountsFile.getConfig().getDouble("Accounts." + key));
        }
    }

    // Loading the config and custom files to the server
    private FileConfiguration config;
    private File cfile;
    public void loadConfig() {
        config = getConfig();
        config.options().copyDefaults(true);
        saveDefaultConfig();
        cfile = new File(getDataFolder(), "config.yml");
    }
    public void loadCustomFiles() {
        // Economy
        AccountsFile.saveDefaultConfig();
        MoneyPouchesFile.saveDefaultConfig();
        WorthFile.saveDefaultConfig();

        // Chat
        MessagesFile.saveDefaultConfig();
        BlockedCommandsFile.saveDefaultConfig();
        ChatFilterFile.saveDefaultConfig();
        AutoMessagesFile.saveDefaultConfig();
    }

    // Getting money pouches
    public List<String> getConvertedLore(String path) {
        List<String> oldList = MoneyPouchesFile.getConfig().getStringList(path + ".lore");
        List<String> newList = new ArrayList<>();
        for (String a : oldList)
            newList.add(ChatColor.translateAlternateColorCodes('&', a));
        return newList;
    }
    public void loadMoneyPouches() {
        if (!MoneyPouchesFile.getConfig().contains("Tiers.")) return;
        for (String key : MoneyPouchesFile.getConfig().getConfigurationSection("Tiers.").getKeys(false)) {
            ItemStack item = new ItemStack(getMaterial(MoneyPouchesFile.getConfig().getString("Tiers." + key + ".material")));
            ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(Utils.chat(MoneyPouchesFile.getConfig().getString("Tiers." + key + ".name")));
            meta.setLore(getConvertedLore("Tiers." + key));
            item.setItemMeta(meta);

            Utils.MoneyPouches_max.put(item.getItemMeta().getDisplayName(), MoneyPouchesFile.getConfig().getInt("Tiers." + key + ".max"));
            Utils.MoneyPouches_min.put(item.getItemMeta().getDisplayName(), MoneyPouchesFile.getConfig().getInt("Tiers." + key + ".min"));
            Utils.MoneyPouches.put(item.getItemMeta().getDisplayName(), item);
        }
    }

    // Managing auto messages
    public void loadAutoMessages() {
        if (!AutoMessagesFile.getConfig().contains("Messages.")) return;
        Utils.AutoMessages.addAll(AutoMessagesFile.getConfig().getStringList("Messages"));
    }
    public void AutoMessage() {
        if (getConfig().getBoolean("Chat.enabled", false)) return;
        if (getConfig().getBoolean("Chat.Auto Messages.enabled", true)) {
            for (Iterator<String> iterator = AutoMessagesFile.getConfig().getConfigurationSection("Messages").getKeys(false).iterator(); iterator.hasNext(); ) {
                String key = iterator.next();
                String message = AutoMessagesFile.getConfig().getString("Messages." + key + ".content").replaceAll("%n", "\n");
                long interval = AutoMessagesFile.getConfig().getInt("Messages." + key + ".interval") * 60;
                Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
                    public void run() {
                        Bukkit.broadcastMessage(Utils.chat(message));
                    }
                }, 20L * interval, 20L * interval);
            }
        }
    }

    // File creation =====================================================================================
    // Warps File
    public FileConfiguration warps;
    public File warpsfile;

    public void LoadWarpsFile() {
        warpsfile = new File(getDataFolder(), "World/Warps.yml");
        warps = YamlConfiguration.loadConfiguration(warpsfile);
        saveWarps();
    }

    public void saveWarps() {
        try {
            warps.save(warpsfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getWarpsfile() {
        return warpsfile;
    }

    public FileConfiguration getWarps() {
        return warps;
    }

    // Homes file
    public FileConfiguration homes;
    public File homesfile;

    @EventHandler
    public void homes(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        homesfile = new File(getDataFolder(), "Homes.yml");
        homes = YamlConfiguration.loadConfiguration(homesfile);
        if (!homes.contains("Homes.Owner's Name " + player.getName())) {
            homes.set("Homes.Owner's Name " + player.getName(), "");
            saveHomes();
        }
    }

    public void saveHomes() {
        try {
            homes.save(homesfile);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public File getHomesFile() {
        return homesfile;
    }

    public FileConfiguration getHomes() {
        return homes;
    }

    public void vanishCheck() {
        Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
            public void run() {
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (Utils.invisible_list.contains(player.getUniqueId())) {
                        essentialsZAPI.utils.sendActionBar(player, "&fYou are in &cVanish");
                    }
                }
            }
        }, 0L, 20L);
    }

}
