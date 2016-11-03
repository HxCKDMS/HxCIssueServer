package hxckdms.hxcissueserver.logging;

import java.io.IOException;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;

public class Logger  {
    private java.util.logging.Logger logger;

    public Logger(String name, String fileName) {
        logger = java.util.logging.Logger.getLogger(name);

        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setFormatter(new LogFormatter());
        logger.setUseParentHandlers(false);
        logger.addHandler(consoleHandler);
        try {
            logger.addHandler(new LogFileHandler(fileName + ".log", true));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void log(Level logLevel, Object object, Exception e) {
        if(e != null)  logger.log(logLevel, String.valueOf(object), e);
        logger.log(logLevel, String.valueOf(object));
    }

    public void log(Level logLevel, Object object) {
        log(logLevel, object, null);
    }

    public void all(Object object){
        log(Level.ALL, object);
    }

    public void finest(Object object){
        log(Level.FINEST, object);
    }

    public void finer(Object object) {
        log(Level.FINER, object);
    }

    public void fine(Object object) {
        log(Level.FINE, object);
    }

    public void info(Object object){
        log(Level.INFO, object);
    }

    public void warning(Object object, Exception e) {
        log(Level.WARNING, object, e);
    }

    public void warning(Object object) {
        log(Level.WARNING, object);
    }

    public void severe(Object object, Exception e) {
        log(Level.SEVERE, object, e);
    }

    public void severe(Object object) {
        log(Level.SEVERE, object);
    }
}
