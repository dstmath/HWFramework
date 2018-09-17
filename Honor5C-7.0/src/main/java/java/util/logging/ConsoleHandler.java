package java.util.logging;

public class ConsoleHandler extends StreamHandler {
    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        setLevel(manager.getLevelProperty(cname + ".level", Level.INFO));
        setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter(manager.getFormatterProperty(cname + ".formatter", new SimpleFormatter()));
        try {
            setEncoding(manager.getStringProperty(cname + ".encoding", null));
        } catch (Exception e) {
            try {
                setEncoding(null);
            } catch (Exception e2) {
            }
        }
    }

    public ConsoleHandler() {
        this.sealed = false;
        configure();
        setOutputStream(System.err);
        this.sealed = true;
    }

    public void publish(LogRecord record) {
        super.publish(record);
        flush();
    }

    public void close() {
        flush();
    }
}
