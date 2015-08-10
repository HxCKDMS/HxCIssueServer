package HxCKDMS.HxCIssueServer.Logging;

import java.io.IOException;
import java.util.logging.FileHandler;

public class LogFileHandler extends FileHandler {
    public LogFileHandler(String pattern, boolean append) throws IOException, SecurityException {
        super(pattern, append);
        setFormatter(new LogFormatter());
    }
}
