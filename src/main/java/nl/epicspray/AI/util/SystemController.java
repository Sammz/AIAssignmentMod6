package nl.epicspray.AI.util;

/**
 * Created by Gebruiker on 15-12-2016.
 */
public class SystemController {
    private static Logger logger;
    private static Logger.LogLevel logLevel = Logger.LogLevel.VERBOSE;

    public static Logger getLogger() {
        if(logger == null){
            logger = new Logger(System.out, logLevel);
        }
        return logger;
    }


}
