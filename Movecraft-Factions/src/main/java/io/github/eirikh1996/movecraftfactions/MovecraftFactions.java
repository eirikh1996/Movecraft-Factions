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
    private MPerm craftsPerm;
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
        }
        Plugin tempMovecraftPlugin = getServer().getPluginManager().getPlugin("Movecraft");
        if (tempMovecraftPlugin instanceof Movecraft){

                getLogger().info(I18nSupport.getInternationalisedString("Startup - Movecraft found"));
                movecraftPlugin = (Movecraft) tempMovecraftPlugin;

        }
        if (movecraftPlugin == null || !movecraftPlugin.isEnabled()){
            getLogger().severe(I18nSupport.getInternationalisedString("Startup - Movecraft not found"));
            getServer().getPluginManager().disablePlugin(this);
        }
        if (!Settings.legacy){
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
            f3Utils = new F3Utils();
        }
        Settings.allowMovementInSafezone = getConfig().getBoolean("allowMovementInSafezone", true);
        Settings.allowMovementInWarzone = getConfig().getBoolean("allowMovementInWarzone", true);
        Settings.allowSinkInSafezone = getConfig().getBoolean("allowSinkInSafezone", false);
        Settings.allowSinkInWarzone = getConfig().getBoolean("allowSinkInWarzone", true);
        Settings.reduceStrengthOnCraftSink = getConfig().getBoolean("reduceStrengthOnCraftSink", true);
        Settings.minY = getConfig().getInt("minY", 0);
        Settings.maxY = getConfig().getInt("maxY", 255);
        if (Settings.legacy) {
            craftsPerm = MPerm.getCreative(1050, "crafts", "crafts", "Allows movement of piloted crafts in the territory", MUtil.set(Rel.ALLY, Rel.LEADER, Rel.OFFICER, Rel.MEMBER), true, true, true);
        } else {
            craftsPerm = f3Utils.getCreative(1050, "crafts", "crafts", "Allows movement of piloted crafts in the territory", true, true, true);
        }
        getServer().getPluginManager().registerEvents(this, this);
        getServer().getPluginManager().registerEvents(UpdateManager.getInstance(), this);
    }

    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event){
        //ignore sinking crafts
        if (event.getCraft().getSinking())
            return;
        HitBox newHitbox;
        try {
            Method getNewHitBox = CraftTranslateEvent.class.getDeclaredMethod("getNewHitBox");
            newHitbox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction;
        for (MovecraftLocation moveLoc : newHitbox){
            if (moveLoc.getY() > Settings.maxY || moveLoc.getY() < Settings.minY){
                continue;
            }
            PS ps = PS.valueOf(moveLoc.toBukkit(event.getCraft().getW()));
            faction = BoardColl.get().getFactionAt(ps);
            if (faction == FactionColl.get().getSafezone()){
                if (!Settings.allowMovementInSafezone && event.getCraft().getNotificationPlayer() != null && !event.getCraft().getNotificationPlayer().hasPermission("movecraftfactions.safezone.move")){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in safezone"));
                    event.setCancelled(true);
                }
                return;
            }

            else if (faction == FactionColl.get().getWarzone()){
                if (!Settings.allowMovementInWarzone && event.getCraft().getNotificationPlayer() != null && !event.getCraft().getNotificationPlayer().hasPermission("movecraftfactions.warzone.move")){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed Cannot move in warzone"));
                    event.setCancelled(true);
                }
                return;
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(getCraftsPerm(), tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(getCraftsPerm(), tAccess.getHostFaction(), mPlayer))){
                    return;
                }
                if (!mPlayer.isOverriding() && (Settings.legacy ? !tAccess.isMPlayerGranted(mPlayer) : (f3Utils != null && !f3Utils.hasAccess(mPlayer, tAccess)))){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Translation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction()) + ChatColor.RESET));
                    event.setCancelled(true);
                }
            }
        }

    }

    @EventHandler
    public void onCraftRotate(CraftRotateEvent event){
        //ignore sinking crafts
        if (event.getCraft().getSinking())
            return;

        HitBox newHitbox;
        try {
            Method getNewHitBox = CraftRotateEvent.class.getDeclaredMethod("getNewHitBox");
            newHitbox = (HitBox) getNewHitBox.invoke(event);
        } catch (Exception e) {
            return;
        }
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction;
        for (MovecraftLocation moveLoc : newHitbox){
            if (moveLoc.getY() > Settings.maxY || moveLoc.getY() < Settings.minY){
                continue;
            }
            PS ps = PS.valueOf(moveLoc.toBukkit(event.getCraft().getW()));
            faction = BoardColl.get().getFactionAt(ps);
            if (faction == FactionColl.get().getSafezone() && !Settings.allowMovementInSafezone){
                event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Cannot move in safezone"));
                event.setCancelled(true);
                return;
            }

            else if (faction == FactionColl.get().getWarzone() && !Settings.allowMovementInWarzone){
                event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed Cannot move in warzone"));
                event.setCancelled(true);
                return;
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(getCraftsPerm(), tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(getCraftsPerm(), tAccess.getHostFaction(), mPlayer))){
                    return;
                }
                if (Settings.legacy ? !tAccess.isMPlayerGranted(mPlayer) : (f3Utils != null && !f3Utils.hasAccess(mPlayer, tAccess))){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Rotation - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction()) + ChatColor.RESET));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCraftDetect(CraftDetectEvent event) {
        HitBox newHitbox;
        try {
            Method getNewHitBox = Craft.class.getDeclaredMethod("getHitBox");
            newHitbox = (HitBox) getNewHitBox.invoke(event.getCraft());
        } catch (Exception e) {
            return;
        }
        MPlayer mPlayer = MPlayer.get(event.getCraft().getNotificationPlayer());
        Faction faction;
        for (MovecraftLocation moveLoc : newHitbox){
            if (moveLoc.getY() > Settings.maxY || moveLoc.getY() < Settings.minY){
                continue;
            }
            PS ps = PS.valueOf(moveLoc.toBukkit(event.getCraft().getW()));
            faction = BoardColl.get().getFactionAt(ps);
            if (faction == FactionColl.get().getSafezone() && !Settings.allowPilotInSafezone){

                event.setFailMessage(I18nSupport.getInternationalisedString("Detection - Failed Cannot pilot in safezone"));
                event.setCancelled(true);

                return;
            }

            else if (faction == FactionColl.get().getWarzone() && !Settings.allowPilotInWarzone){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Detection - Failed Cannot pilot in warzone"));
                    event.setCancelled(true);

                return;
            }
            else if (faction != FactionColl.get().getNone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(getCraftsPerm(), tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(getCraftsPerm(), tAccess.getHostFaction(), mPlayer))){
                    return;
                }
                if (Settings.legacy ? !tAccess.isMPlayerGranted(mPlayer) : (f3Utils != null && !f3Utils.hasAccess(mPlayer, tAccess))){
                    event.setFailMessage(I18nSupport.getInternationalisedString("Detection - Failed No access to faction").replace("{FACTION}", faction.getName(mPlayer.getFaction()) + ChatColor.RESET));
                    event.setCancelled(true);
                }
            }
        }
    }

    @EventHandler
    public void onCraftSink(CraftSinkEvent event){

        Craft craft = event.getCraft();
        MPlayer mp = MPlayer.get(craft.getNotificationPlayer());
        double powerOnDeath = mp.getPowerPerDeath();
        double power = mp.getPower();
        double newPower = power + powerOnDeath;
        Faction faction = FactionColl.get().getNone();
        HitBox hitbox;
        try {
            Method getNewHitBox = Craft.class.getDeclaredMethod("getHitBox");
            hitbox = (HitBox) getNewHitBox.invoke(event.getCraft());
        } catch (Exception e) {
            return;
        }
        for (MovecraftLocation ml : hitbox){
            faction = BoardColl.get().getFactionAt(PS.valueOf(ml.toBukkit(craft.getW())));
            if (faction != FactionColl.get().getNone()){
                break;
            }
        }
        if (!Settings.allowSinkInSafezone && faction == FactionColl.get().getSafezone()){
            mp.msg(I18nSupport.getInternationalisedString("Sink - Cannot sink in Safezone"));
            event.setCancelled(true);
            return;
        } else if (!Settings.allowSinkInWarzone && faction == FactionColl.get().getWarzone()){
            event.setCancelled(true);
            mp.msg(I18nSupport.getInternationalisedString("Sink - Cannot sink in Warzone"));
            return;
        }
        if (!Settings.reduceStrengthOnCraftSink){
            return;
        }
        if (!faction.getFlag(MFlag.getFlagPowerloss())){
            mp.msg(I18nSupport.getInternationalisedString("Sink - No Lost Power Territory"));
            return;
        }
        if (!MConf.get().worldsPowerLossEnabled.contains(craft.getW())){
            mp.msg(I18nSupport.getInternationalisedString("Sink - No Lost Power World"));
            return;
        }
        mp.msg(String.format(I18nSupport.getInternationalisedString("Sink - Power Lost"), newPower, mp.getPowerMax()));
        EventFactionsPowerChange powerChangeEvent = new EventFactionsPowerChange(null, mp, EventFactionsPowerChange.PowerChangeReason.UNDEFINED, newPower);
        powerChangeEvent.run();
        mp.setPower(newPower);

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

    public MPerm getCraftsPerm() {
        return craftsPerm;
    }

    public F3Utils getF3Utils() {
        return f3Utils;
    }
}
