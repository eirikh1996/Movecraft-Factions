package io.github.eirikh1996.movecraftfactions;

import com.massivecraft.factions.Factions;
import com.massivecraft.factions.Rel;
import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.cmd.CmdFactions;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.massivecore.ps.PS;
import com.massivecraft.massivecore.util.MUtil;
import io.github.eirikh1996.movecraftfactions.f3.F3Utils;
import io.github.eirikh1996.movecraftfactions.movecraft7.Movecraft7Listener;
import io.github.eirikh1996.movecraftfactions.movecraft8.Movecraft8Listener;
import net.countercraft.movecraft.Movecraft;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.*;
import net.countercraft.movecraft.utils.HashHitBox;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class MovecraftFactions extends JavaPlugin implements Listener {
    private static MovecraftFactions instance;
    private static Movecraft movecraftPlugin;
    private static Factions factionsPlugin;
    private F3Utils f3Utils;

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        String version = getServer().getClass().getPackage().getName().substring(getServer().getClass().getPackage().getName().lastIndexOf(".") + 1);
        Settings.legacy = Integer.parseInt(version.split("_")[1]) <= 12;
        String[] localisations = {"en", "no", "de", "fr"};

        for (String locale : localisations){
            File langFile = new File(getDataFolder(),"localisation/mflang_" + locale + ".properties");
            if (!langFile.exists()){
                saveResource("localisation/mflang_" + locale + ".properties", false);
            }
        }
        saveDefaultConfig();
        Settings.locale = getConfig().getString("locale", "en");
        I18nSupport.initialize();
        Plugin tempFactionsPlugin = getServer().getPluginManager().getPlugin("Factions");
        if (tempFactionsPlugin instanceof Factions){

                getLogger().info(I18nSupport.getInternationalisedString("Startup - Factions found"));
                factionsPlugin = (Factions) tempFactionsPlugin;

        }
        if (factionsPlugin == null || !factionsPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Factions not found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        boolean movecraft8 = false;
        Plugin tempMovecraftPlugin = getServer().getPluginManager().getPlugin("Movecraft");
        if (tempMovecraftPlugin instanceof Movecraft){

                getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft found"));
                movecraftPlugin = (Movecraft) tempMovecraftPlugin;

                try {
                    Class.forName("net.countercraft.movecraft.craft.BaseCraft");
                    movecraft8 = true;
                } catch (ClassNotFoundException ignored) {
                }
                getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft version detected").replace("%MOVECRAFT_VERSION%", movecraftPlugin.getDescription().getVersion()));

        }
        if (movecraftPlugin == null || !movecraftPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Movecraft not found"));
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        if (!Settings.legacy){
            f3Utils = new F3Utils();
            try {
                Field p2dField = MConf.class.getField("perm2default");
                Map<String, Set<String>> p2d = (Map<String, Set<String>>) p2dField.get(MConf.get());
                Set<String> defaults = new HashSet<>();
                if (getConfig().contains("craftPermDefaults")) {
                    defaults.addAll(getConfig().getStringList("craftPermDefaults"));
                } else {
                    defaults.add("LEADER");
                    defaults.add("OFFICER");
                    defaults.add("MEMBER");
                    defaults.add("RECRUIT");
                    defaults.add("ALLY");
                }
                p2d.put("crafts", defaults);
                p2dField.set(MConf.get(), p2d);
            } catch (NoSuchFieldException | IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        Settings.allowMovementInSafezone = getConfig().getBoolean("allowMovementInSafezone", true);
        Settings.allowMovementInWarzone = getConfig().getBoolean("allowMovementInWarzone", true);
        Settings.allowPilotInSafezone = getConfig().getBoolean("allowPilotInSafezone", true);
        Settings.allowPilotInWarzone = getConfig().getBoolean("allowPilotInWarzone", true);
        Settings.allowSinkInSafezone = getConfig().getBoolean("allowSinkInSafezone", false);
        Settings.allowSinkInWarzone = getConfig().getBoolean("allowSinkInWarzone", true);
        Settings.reduceStrengthOnCraftSink = getConfig().getBoolean("reduceStrengthOnCraftSink", true);
        Settings.minY = getConfig().getInt("minY", 0);
        Settings.maxY = getConfig().getInt("maxY", 255);
        if (Settings.legacy) {
            Settings.craftsPerm = MPerm.getCreative(1050, "crafts", "crafts", "Allows movement of piloted crafts in the territory", MUtil.set(Rel.ALLY, Rel.LEADER, Rel.OFFICER, Rel.MEMBER), true, true, true);
        } else {
            Settings.craftsPerm = f3Utils.getCreative(1050, "crafts", "crafts", "Allows movement of piloted crafts in the territory", true, true, true);
        }
        getServer().getPluginManager().registerEvents(movecraft8 ? new Movecraft8Listener() : new Movecraft7Listener(), this);
        getServer().getPluginManager().registerEvents(UpdateManager.getInstance(), this);
    }



    public static MovecraftFactions getInstance() {
        return instance;
    }

    public Factions getFactionsPlugin() {
        return factionsPlugin;
    }

    public Movecraft getMovecraftPlugin() {
        return movecraftPlugin;
    }

    public F3Utils getF3Utils() {
        return f3Utils;
    }
}
