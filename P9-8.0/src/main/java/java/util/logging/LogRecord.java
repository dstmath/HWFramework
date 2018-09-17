package java.util.logging;

import dalvik.system.VMStack;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LogRecord implements Serializable {
    private static final int MIN_SEQUENTIAL_THREAD_ID = 1073741823;
    private static final AtomicLong globalSequenceNumber = new AtomicLong(0);
    private static final AtomicInteger nextThreadId = new AtomicInteger(MIN_SEQUENTIAL_THREAD_ID);
    private static final long serialVersionUID = 5372048053134512534L;
    private static final ThreadLocal<Integer> threadIds = new ThreadLocal();
    private Level level;
    private String loggerName;
    private String message;
    private long millis = System.currentTimeMillis();
    private transient boolean needToInferCaller = true;
    private transient Object[] parameters;
    private transient ResourceBundle resourceBundle;
    private String resourceBundleName;
    private long sequenceNumber = globalSequenceNumber.getAndIncrement();
    private String sourceClassName;
    private String sourceMethodName;
    private int threadID = defaultThreadID();
    private Throwable thrown;

    private int defaultThreadID() {
        long tid = Thread.currentThread().getId();
        if (tid < 1073741823) {
            return (int) tid;
        }
        Integer id = (Integer) threadIds.get();
        if (id == null) {
            id = Integer.valueOf(nextThreadId.getAndIncrement());
            threadIds.set(id);
        }
        return id.intValue();
    }

    public LogRecord(Level level, String msg) {
        level.getClass();
        this.level = level;
        this.message = msg;
    }

    public String getLoggerName() {
        return this.loggerName;
    }

    public void setLoggerName(String name) {
        this.loggerName = name;
    }

    public ResourceBundle getResourceBundle() {
        return this.resourceBundle;
    }

    public void setResourceBundle(ResourceBundle bundle) {
        this.resourceBundle = bundle;
    }

    public String getResourceBundleName() {
        return this.resourceBundleName;
    }

    public void setResourceBundleName(String name) {
        this.resourceBundleName = name;
    }

    public Level getLevel() {
        return this.level;
    }

    public void setLevel(Level level) {
        if (level == null) {
            throw new NullPointerException();
        }
        this.level = level;
    }

    public long getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(long seq) {
        this.sequenceNumber = seq;
    }

    public String getSourceClassName() {
        if (this.needToInferCaller) {
            inferCaller();
        }
        return this.sourceClassName;
    }

    public void setSourceClassName(String sourceClassName) {
        this.sourceClassName = sourceClassName;
        this.needToInferCaller = false;
    }

    public String getSourceMethodName() {
        if (this.needToInferCaller) {
            inferCaller();
        }
        return this.sourceMethodName;
    }

    public void setSourceMethodName(String sourceMethodName) {
        this.sourceMethodName = sourceMethodName;
        this.needToInferCaller = false;
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object[] getParameters() {
        return this.parameters;
    }

    public void setParameters(Object[] parameters) {
        this.parameters = parameters;
    }

    public int getThreadID() {
        return this.threadID;
    }

    public void setThreadID(int threadID) {
        this.threadID = threadID;
    }

    public long getMillis() {
        return this.millis;
    }

    public void setMillis(long millis) {
        this.millis = millis;
    }

    public Throwable getThrown() {
        return this.thrown;
    }

    public void setThrown(Throwable thrown) {
        this.thrown = thrown;
    }

    private void writeObject(ObjectOutputStream out) throws IOException {
        out.defaultWriteObject();
        out.writeByte(1);
        out.writeByte(0);
        if (this.parameters == null) {
            out.writeInt(-1);
            return;
        }
        out.writeInt(this.parameters.length);
        for (int i = 0; i < this.parameters.length; i++) {
            if (this.parameters[i] == null) {
                out.writeObject(null);
            } else {
                out.writeObject(this.parameters[i].toString());
            }
        }
    }

    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        int major = in.readByte();
        int minor = in.readByte();
        if (major != (byte) 1) {
            throw new IOException("LogRecord: bad version: " + major + "." + minor);
        }
        int len = in.readInt();
        if (len < -1) {
            throw new NegativeArraySizeException();
        }
        int i;
        if (len == -1) {
            this.parameters = null;
        } else if (len < 255) {
            this.parameters = new Object[len];
            for (i = 0; i < this.parameters.length; i++) {
                this.parameters[i] = in.readObject();
            }
        } else {
            List<Object> params = new ArrayList(Math.min(len, 1024));
            for (i = 0; i < len; i++) {
                params.add(in.readObject());
            }
            this.parameters = params.toArray(new Object[params.size()]);
        }
        if (this.resourceBundleName != null) {
            try {
                this.resourceBundle = ResourceBundle.getBundle(this.resourceBundleName, Locale.getDefault(), ClassLoader.getSystemClassLoader());
            } catch (MissingResourceException e) {
                try {
                    this.resourceBundle = ResourceBundle.getBundle(this.resourceBundleName, Locale.getDefault(), Thread.currentThread().getContextClassLoader());
                } catch (MissingResourceException e2) {
                    this.resourceBundle = null;
                }
            }
        }
        this.needToInferCaller = false;
    }

    private void inferCaller() {
        this.needToInferCaller = false;
        boolean lookingForLogger = true;
        for (StackTraceElement frame : VMStack.getThreadStackTrace(Thread.currentThread())) {
            String cname = frame.getClassName();
            boolean isLoggerImpl = isLoggerImplFrame(cname);
            if (lookingForLogger) {
                if (isLoggerImpl) {
                    lookingForLogger = false;
                }
            } else if (!(isLoggerImpl || cname.startsWith("java.lang.reflect.") || (cname.startsWith("sun.reflect.") ^ 1) == 0)) {
                setSourceClassName(cname);
                setSourceMethodName(frame.getMethodName());
                return;
            }
        }
    }

    private boolean isLoggerImplFrame(String cname) {
        if (cname.equals("java.util.logging.Logger") || cname.startsWith("java.util.logging.LoggingProxyImpl")) {
            return true;
        }
        return cname.startsWith("sun.util.logging.");
    }
}
