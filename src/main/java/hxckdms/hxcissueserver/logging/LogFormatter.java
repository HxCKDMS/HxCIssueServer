package hxckdms.hxcissueserver.logging;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

public class LogFormatter extends Formatter {
    private static SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");

    @Override
    public String format(LogRecord record) {
        Thread thread = getThreadFromID(record.getThreadID());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(record.getMillis());
        return "[" + format.format(calendar.getTime()) + "] [" + (thread != null ? thread.getName() : "null") + "/" + record.getLevel() + "] [" + record.getLoggerName() + "]: " + record.getMessage() + "\n";
    }

    private static Thread getThreadFromID(int id) {
        Thread[] threads = new Thread[512];
        Thread.enumerate(threads);
        for(Thread thread : threads) {
            if(thread.getId() == id) return thread;
        }
        return null;
    }
}
