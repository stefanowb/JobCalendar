package main.java.de.jobCalendar.webApi.manager;

/**
 * Created by Stefan on 12.03.2017.
 */
public class InstanceManager {

    private static RequestManager requestManager;

    public static RequestManager getRequestManager() {
        return requestManager;
    }

    public static void initialize() throws Exception {

        requestManager = new RequestManager();
    }
}
