package io.github.eirikh1996.movecraftfactions;

import com.massivecraft.factions.entity.BoardColl;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPlayer;
import com.massivecraft.massivecore.ps.PS;
import io.github.eirikh1996.movecraftfactions.f3.F3Utils;
import net.countercraft.movecraft.MovecraftLocation;
import net.countercraft.movecraft.events.CraftDetectEvent;
import net.countercraft.movecraft.events.SignTranslateEvent;
import org.bukkit.block.Sign;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FactionsSign implements Listener {
    private final String HEADER = "Factions:";

    @EventHandler
    public void onSignTranslate(SignTranslateEvent event) {
        if (!event.getLine(0).equalsIgnoreCase(HEADER))
            return;
        final List<Faction> factions = new ArrayList<>();
        for (MovecraftLocation ml : event.getCraft().getHitBox()) {
            final Faction faction = BoardColl.get().getFactionAt(PS.valueOf(ml.toBukkit(event.getCraft().getW())));
            if (factions.contains(faction) || factions.size() >= 3)
                continue;
            factions.add(faction);
        }
        for (int i = 1 ; i <= factions.size() ; i++) {
            event.setLine(i, factions.get(i).getName(MPlayer.get(event.getCraft().getNotificationPlayer())));
        }
    }
}
