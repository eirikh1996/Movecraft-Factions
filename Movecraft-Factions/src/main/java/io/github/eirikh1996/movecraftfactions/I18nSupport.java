package io.github.eirikh1996.movecraftfactions;

import java.io.*;
import java.util.Properties;

public class I18nSupport {
    private static Properties languageFile;
    public static void initialize(){
        languageFile = new Properties();
        File locDir = new File(MovecraftFactions.getInstance().getDataFolder(), "localisation");
        if (!locDir.exists()){
            locDir.mkdirs();
        }
        File locFile = new File(locDir, String.format("mflang_%s.properties", Settings.locale));
        InputStream is;
        try {
            is = new FileInputStream(locFile);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            is = null;
        }
        if (is == null){

            MovecraftFactions.getInstance().getServer().shutdown();
        }
        try {
            languageFile.load(is);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String getInternationalisedString(String key){
        String ret = languageFile.getProperty(key);
        return ret != null ? ret : key;
    }
}
