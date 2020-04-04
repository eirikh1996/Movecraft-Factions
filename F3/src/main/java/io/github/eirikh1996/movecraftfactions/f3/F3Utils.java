package io.github.eirikh1996.movecraftfactions.f3;

import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.Faction;
import com.massivecraft.factions.entity.MPerm;
import com.massivecraft.factions.entity.MPlayer;

import java.util.ArrayList;
import java.util.List;

public class F3Utils {

    public boolean hasAccess(MPlayer player, TerritoryAccess tAccess){
        return tAccess.isGranted(player);
    }

    public boolean isPermitted(MPerm mPerm, Faction faction, MPlayer mPlayer){
        return faction.isPlayerPermitted(mPlayer, mPerm);

    }

    public MPerm getCreative(int priority, String id, String name, String desc, boolean territory, boolean editable, boolean visible){
        return MPerm.getCreative(priority, id, name, desc, territory, editable, visible);
    }
}
