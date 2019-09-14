package io.github.eirikh1996.movecraftfactions.f3;

import com.massivecraft.factions.TerritoryAccess;
import com.massivecraft.factions.entity.MPlayer;

public class F3Utils {
    public boolean hasAccess(MPlayer player, TerritoryAccess tAccess){
        return tAccess.isGranted(player);
    }
}
