package main.java.de.jobCalendar.webApi.manager;


import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class ConfigManager {

    public static final String INI_PATH = "C:/jobCalendar/init.txt";

    public JSONArray iniServersArray;

    public String getInitContent() throws IOException {

        File file = new File(ConfigManager.INI_PATH);

        if (!file.canRead() || !file.isFile())
            System.exit(0);

        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(ConfigManager.INI_PATH));
            String zeile = null;

            StringBuilder iniContent = new StringBuilder(); // or StringBuffer if Java version 5+

            while ((zeile = in.readLine()) != null) {
                iniContent.append(zeile);
                iniContent.append('\r');
            }

            this.iniServersArray = new JSONArray(iniContent.toString());

            return iniContent.toString();

        } finally {
            if (in != null)
                in.close();
        }
    }

    public JSONObject getServerInitData(String serverName){
        for (Object serverObject : this.iniServersArray){
            JSONObject server = (JSONObject)serverObject;

            if (server.getString("name").equals(serverName)){
                return server;
            }
        }
        return null;
    }
}
