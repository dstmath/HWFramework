package java.util.logging;

public class MemoryHandler extends Handler {
    private static final int DEFAULT_SIZE = 1000;
    private LogRecord[] buffer;
    int count;
    private volatile Level pushLevel;
    private int size;
    int start;
    private Handler target;

    private void configure() {
        LogManager manager = LogManager.getLogManager();
        String cname = getClass().getName();
        this.pushLevel = manager.getLevelProperty(cname + ".push", Level.SEVERE);
        this.size = manager.getIntProperty(cname + ".size", 1000);
        if (this.size <= 0) {
            this.size = 1000;
        }
        setLevel(manager.getLevelProperty(cname + ".level", Level.ALL));
        setFilter(manager.getFilterProperty(cname + ".filter", null));
        setFormatter(manager.getFormatterProperty(cname + ".formatter", new SimpleFormatter()));
    }

    public MemoryHandler() {
        this.sealed = false;
        configure();
        this.sealed = true;
        LogManager manager = LogManager.getLogManager();
        String handlerName = getClass().getName();
        String targetName = manager.getProperty(handlerName + ".target");
        if (targetName != null) {
            try {
                this.target = (Handler) ClassLoader.getSystemClassLoader().loadClass(targetName).newInstance();
            } catch (Exception e) {
                try {
                    Class<?> clz = Thread.currentThread().getContextClassLoader().loadClass(targetName);
                    this.target = (Handler) clz.newInstance();
                    Class<?> cls = clz;
                } catch (Exception innerE) {
                    throw new RuntimeException("MemoryHandler can't load handler target \"" + targetName + "\"", innerE);
                }
            }
            init();
            return;
        }
        throw new RuntimeException("The handler " + handlerName + " does not specify a target");
    }

    private void init() {
        this.buffer = new LogRecord[this.size];
        this.start = 0;
        this.count = 0;
    }

    public MemoryHandler(Handler target2, int size2, Level pushLevel2) {
        if (target2 == null || pushLevel2 == null) {
            throw new NullPointerException();
        } else if (size2 > 0) {
            this.sealed = false;
            configure();
            this.sealed = true;
            this.target = target2;
            this.pushLevel = pushLevel2;
            this.size = size2;
            init();
        } else {
            throw new IllegalArgumentException();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:15:0x0046, code lost:
        return;
     */
    public synchronized void publish(LogRecord record) {
        if (isLoggable(record)) {
            this.buffer[(this.start + this.count) % this.buffer.length] = record;
            if (this.count < this.buffer.length) {
                this.count++;
            } else {
                this.start++;
                this.start %= this.buffer.length;
            }
            if (record.getLevel().intValue() >= this.pushLevel.intValue()) {
                push();
            }
        }
    }

    public synchronized void push() {
        for (int i = 0; i < this.count; i++) {
            this.target.publish(this.buffer[(this.start + i) % this.buffer.length]);
        }
        this.start = 0;
        this.count = 0;
    }

    public void flush() {
        this.target.flush();
    }

    public void close() throws SecurityException {
        this.target.close();
        setLevel(Level.OFF);
    }

    public synchronized void setPushLevel(Level newLevel) throws SecurityException {
        if (newLevel != null) {
            checkPermission();
            this.pushLevel = newLevel;
        } else {
            throw new NullPointerException();
        }
    }

    public Level getPushLevel() {
        return this.pushLevel;
    }

    public boolean isLoggable(LogRecord record) {
        return super.isLoggable(record);
    }
}
