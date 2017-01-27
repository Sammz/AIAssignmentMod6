package nl.epicspray.AI.util;

import java.io.PrintStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Formatter;

/**
 * Created by Gebruiker on 19-11-2016.
 */
public class Logger {
    public enum LogLevel{
        FATAL (0), ERROR (1), WARN (2), INFO(3), DEBUG(4), TRACE(5), VERBOSE(6);

        private final int level;

        LogLevel(int level){
            this.level = level;
        }
        public int getLevel() {
            return level;
        }
    }

    private PrintStream output;
    private final static DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss:SSS");
    private LogLevel logLevel;

    public Logger(PrintStream output, LogLevel level){
        this.output = output;
        this.logLevel = level;
    }

    public void log(LogLevel level, String message, int strace_depth) {
        Formatter format = new Formatter();
        LocalDateTime date = LocalDateTime.now();
        String origin = Thread.currentThread().getStackTrace()[strace_depth].getClassName().substring("nl.epicspray.".length());
        String method = Thread.currentThread().getStackTrace()[strace_depth].getMethodName();
        if (this.logLevel.getLevel() < level.getLevel()) {
            return;
        }

        switch (level) {
            case FATAL:
                format.format("%6$1s [%1$1s] %5$6s:  %2$-30s  |  %3$-20s     |     %4$1s", date.format(FORMATTER), origin, method, message, "FATAL", ANSIColors.ANSI_RED);
                break;
            case ERROR:
                format.format("%6$1s [%1$1s] %5$6s:  %2$-30s  |  %3$-20s     |     %4$1s", date.format(FORMATTER), origin, method, message, "ERROR", ANSIColors.ANSI_YELLOW);
                break;
            case WARN:
                format.format("%6$1s [%1$1s] %5$6s:  %2$-30s  |  %3$-20s     |     %4$1s", date.format(FORMATTER), origin, method, message, "WARN", ANSIColors.ANSI_BLUE);
                break;
            case INFO:
                format.format("%6$1s [%1$1s] %5$6s:  %2$-30s  |  %3$-20s     |     %4$1s", date.format(FORMATTER), origin, method, message, "INFO", ANSIColors.ANSI_WHITE);
                break;
            case DEBUG:
                format.format("%6$1s [%1$1s] %5$6s:  %2$-30s  |  %3$-20s     |     %4$1s", date.format(FORMATTER), origin, method, message, "DEBUG", ANSIColors.ANSI_PURPLE);
                break;
            case TRACE:
                break;
            case VERBOSE:
                break;
        }
        String output = format.toString();
        System.out.println(output);
        format.close();
    }

    public void log(LogLevel level, String message){
        this.log(level, message, 3);
    }

    public void debug(String message){
        this.log(LogLevel.DEBUG, message, 3);
    }

    public void warning(String message){
        this.log(LogLevel.WARN, message, 3);
    }

    public void info(String message){
        this.log(LogLevel.INFO, message, 3);
    }

    public void fatalError(String message){
        this.log(LogLevel.FATAL, message, 3);
    }

    public void error(String message){
        this.log(LogLevel.ERROR, message, 3);
    }
}
