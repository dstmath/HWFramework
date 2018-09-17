package java.util.logging;

import dalvik.system.VMStack;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class LogRecord implements Serializable {
    private static final int MIN_SEQUENTIAL_THREAD_ID = 1073741823;
    private static final AtomicLong globalSequenceNumber = null;
    private static final AtomicInteger nextThreadId = null;
    private static final long serialVersionUID = 5372048053134512534L;
    private static final ThreadLocal<Integer> threadIds = null;
    private Level level;
    private String loggerName;
    private String message;
    private long millis;
    private transient boolean needToInferCaller;
    private transient Object[] parameters;
    private transient ResourceBundle resourceBundle;
    private String resourceBundleName;
    private long sequenceNumber;
    private String sourceClassName;
    private String sourceMethodName;
    private int threadID;
    private Throwable thrown;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: java.util.logging.LogRecord.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: java.util.logging.LogRecord.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: java.util.logging.LogRecord.<clinit>():void");
    }

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
        this.sequenceNumber = globalSequenceNumber.getAndIncrement();
        this.threadID = defaultThreadID();
        this.millis = System.currentTimeMillis();
        this.needToInferCaller = true;
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
        if (major != 1) {
            throw new IOException("LogRecord: bad version: " + major + "." + minor);
        }
        int len = in.readInt();
        if (len == -1) {
            this.parameters = null;
        } else {
            this.parameters = new Object[len];
            for (int i = 0; i < this.parameters.length; i++) {
                this.parameters[i] = in.readObject();
            }
        }
        if (this.resourceBundleName != null) {
            try {
                this.resourceBundle = ResourceBundle.getBundle(this.resourceBundleName);
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
            } else if (!(isLoggerImpl || cname.startsWith("java.lang.reflect.") || cname.startsWith("sun.reflect."))) {
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
