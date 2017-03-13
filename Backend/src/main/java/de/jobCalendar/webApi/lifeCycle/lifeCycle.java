package main.java.de.jobCalendar.webApi.lifeCycle;

import main.java.de.jobCalendar.webApi.manager.InstanceManager;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;
import java.util.concurrent.ScheduledExecutorService;

@WebListener
public class lifeCycle implements ServletContextListener{

    // Logic for Container StartUp/Shutdown goes here

    @Override
    public void contextDestroyed(ServletContextEvent arg0)
    {
        //shutdown logic
        // Hallo Roland

    }

    @Override
    public void contextInitialized(ServletContextEvent arg0)
    {
        //startup logic
        try {
            InstanceManager.initialize();
        } catch (Exception e) {
            System.out.println("FEHLER: Der InstanceManager hat beim " +
                    "Initialisieren einen Fehler gebracht.\nFehlermeldung: "
                    + e);
        }
    }
}
