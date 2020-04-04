package io.github.eirikh1996.movecraftfactions;

import com.google.gson.Gson;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.scheduler.BukkitRunnable;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

public class UpdateManager extends BukkitRunnable implements Listener {
    private static UpdateManager instance;
    private boolean running = false;
    //Prevents more than one instance being created
    private UpdateManager(){
        runTaskTimerAsynchronously(MovecraftFactions.getInstance(), 0, 1000000);
    }

    @Override
    public void run() {
        final String newVersion = newUpdateAvailable();
        MovecraftFactions.getInstance().getLogger().info("Checking for updates");
        new BukkitRunnable() {
            @Override
            public void run() {
                if (newVersion != null){
                    Bukkit.broadcast("An update of Movecraft-Factions is now available. Download from https://dev.bukkit.org/projects/movecraft-factions", "movecraftfactions.update");
                    MovecraftFactions.getInstance().getLogger().warning("An update of Movecraft-Factions is now available.");
                    MovecraftFactions.getInstance().getLogger().warning("Download from https://dev.bukkit.org/projects/movecraft-factions");
                    return;
                }
                MovecraftFactions.getInstance().getLogger().info("You are up to date");
            }
        }.runTaskLaterAsynchronously(MovecraftFactions.getInstance(), 100);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        new BukkitRunnable() {
            @Override
            public void run() {
                final String newVer =  newUpdateAvailable();
                if (newVer == null) {
                    return;
                }
                event.getPlayer().sendMessage("An update of Movecraft-Factions is now available. Download from https://dev.bukkit.org/projects/movecraft-factions");
            }
        }.runTaskLaterAsynchronously(MovecraftFactions.getInstance(), 60);
    }

    public static UpdateManager getInstance(){
        if (instance == null) {
            instance = new UpdateManager();
        }
        return instance;
    }


    public String newUpdateAvailable(){
        try {
            URL url = new URL("https://servermods.forgesvc.net/servermods/files?projectids=342136");
            URLConnection conn = url.openConnection();
            conn.setReadTimeout(5000);
            conn.addRequestProperty("User-Agent", "Movecraft-Factions Update Checker");
            conn.setDoOutput(true);
            final BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            final String response = reader.readLine();
            final Gson gson = new Gson();
            List objList = gson.fromJson(response, List.class);
            if (objList.size() == 0) {
                MovecraftFactions.getInstance().getLogger().warning("No files found, or Feed URL is bad.");
                return null;
            }
            Map<String, Object> data = (Map<String, Object>) objList.get(objList.size() - 1);
            String versionName = ((String) data.get("name"));
            String newVersion = versionName.substring(versionName.lastIndexOf("v") + 1);
            int currVer = Integer.parseInt(MovecraftFactions.getInstance().getDescription().getVersion().replace("v", "").replace(".", ""));
            int newVer = Integer.parseInt(newVersion.replace(".", ""));
            if (newVer > currVer)
                return newVersion;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
