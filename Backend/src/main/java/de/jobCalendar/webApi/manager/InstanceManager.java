package main.java.de.jobCalendar.webApi.manager;

/**
 * Created by Stefan on 12.03.2017.
 */
public class InstanceManager {

    private static RequestManager requestManager;
    private static ConfigManager configManager;

    public static RequestManager getRequestManager() {
        return requestManager;
    }
    public static ConfigManager getConfigManager() {
        return configManager;
    }

    public static void initialize() throws Exception {

        requestManager = new RequestManager();
        configManager = new ConfigManager();
    }
}
