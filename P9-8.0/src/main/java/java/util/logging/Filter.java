package java.util.logging;

@FunctionalInterface
public interface Filter {
    boolean isLoggable(LogRecord logRecord);
}
