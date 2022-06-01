package io.github.eirikh1996.movecraftfactions.movecraft7;

import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.*;
import com.massivecraft.factions.event.EventFactionsPowerChange;
import com.massivecraft.massivecore.ps.PS;
import io.github.eirikh1996.movecraftfactions.Settings;
import io.github.eirikh1996.movecraftfactions.f3.F3Utils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.craft.Craft;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.CraftRotateEvent;
import net.countercraft.movecraft.events.CraftSinkEvent;
import net.countercraft.movecraft.events.CraftTranslateEvent;
import net.countercraft.movecraft.localisation.I18nSupport;
import net.countercraft.movecraft.utils.HitBox;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.lang.reflect.Method;

public class Movecraft7Listener implements Listener {

    private F3Utils f3Utils;

    public Movecraft7Listener() {
        if (Settings.legacy)
            return;

        f3Utils = new F3Utils();
    }
    @EventHandler
    public void onCraftTranslate(CraftTranslateEvent event){
        final Craft craft = event.getCraft();
        //ignore sinking crafts
        if (event.getCraft().getSinking())
            return;
        HitBox newHitbox = craft.getHitBox();
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
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(Settings.craftsPerm, tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(Settings.craftsPerm, tAccess.getHostFaction(), mPlayer))){
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
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(Settings.craftsPerm, tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(Settings.craftsPerm, tAccess.getHostFaction(), mPlayer))){
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
            else if (faction != FactionColl.get().getNone() && faction != FactionColl.get().getSafezone() && faction != FactionColl.get().getWarzone()){
                TerritoryAccess tAccess = BoardColl.get().getTerritoryAccessAt(ps);
                if (Settings.legacy ? tAccess.getHostFaction().isPermitted(Settings.craftsPerm, tAccess.getHostFaction().getRelationTo(mPlayer)) : (f3Utils != null && f3Utils.isPermitted(Settings.craftsPerm, tAccess.getHostFaction(), mPlayer))){
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
}
