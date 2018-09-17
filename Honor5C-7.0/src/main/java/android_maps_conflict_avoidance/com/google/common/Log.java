package android_maps_conflict_avoidance.com.google.common;

import android_maps_conflict_avoidance.com.google.common.io.PersistentStore;
import android_maps_conflict_avoidance.com.google.common.util.text.TextUtil;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Hashtable;
import java.util.Vector;

public class Log {
    private static final long START_TIME = 0;
    private static StringBuffer entryBuffer;
    private static boolean isEventLoggingEnabledForTest;
    private static boolean isExplicitClearForTest;
    private static long lastEventTimeMillis;
    private static final Object lastThrowableLock = null;
    private static String lastThrowableString;
    private static final Vector logEntries = null;
    private static boolean logMemory;
    private static LogSaver logSaver;
    private static boolean logThread;
    private static boolean logTime;
    private static OnScreenPrinter onScreenPrinter;
    private static Printer printer;
    private static int throwableCount;
    private static ThrowableListener throwableListener;
    private static final Hashtable timers = null;

    public interface LogSaver {
        Object uploadEventLog(boolean z, Object obj, byte[] bArr);
    }

    public interface OnScreenPrinter {
        void printToScreen(String str);
    }

    public interface Printer {
    }

    public static class StandardErrorPrinter implements Printer {
    }

    public interface ThrowableListener {
        void onThrowable(String str, Throwable th, boolean z);
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android_maps_conflict_avoidance.com.google.common.Log.<clinit>():void");
    }

    private Log() {
    }

    public static void logThrowable(String source, Throwable t) {
        t.printStackTrace();
        addThrowableString(source + ": " + t.toString());
        sendThrowable(source, t, false);
    }

    public static void logQuietThrowable(String source, Throwable t) {
        t.printStackTrace();
        sendThrowable(source, t, true);
    }

    public static void addThrowableString(String message) {
        if (message != null) {
            synchronized (lastThrowableLock) {
                if (lastThrowableString != null) {
                    lastThrowableString += "\n" + message;
                } else {
                    lastThrowableString = message;
                }
                if (lastThrowableString.length() > 300) {
                    lastThrowableString = lastThrowableString.substring(0, 300);
                }
            }
        }
    }

    public static boolean addEvent(short type, String status, String data) {
        short numEvents;
        ByteArrayOutputStream baos;
        DataOutputStream dos;
        long timestamp = System.currentTimeMillis();
        PersistentStore store = getPersistentStore();
        byte[] oldEvents = store.readPreference("EVENT_LOG");
        if (oldEvents != null && oldEvents.length <= 600) {
            if ((timestamp - lastEventTimeMillis <= 6553500 ? 1 : null) == null) {
            }
            numEvents = (short) 0;
            if (oldEvents.length > 2) {
                numEvents = (short) (((oldEvents[0] & 255) << 8) | (oldEvents[1] & 255));
            }
            numEvents = (short) (numEvents + 1);
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);
            dos.writeShort(numEvents);
            dos.write(oldEvents, 2, oldEvents.length - 2);
            dos.writeShort(type);
            dos.writeShort((int) (Math.min(timestamp - lastEventTimeMillis, 6553500) / 100));
            dos.writeUTF(status);
            dos.writeUTF(data);
            getPersistentStore().setPreference("EVENT_LOG", baos.toByteArray());
            lastEventTimeMillis = timestamp;
            return true;
        }
        if (oldEvents == null) {
            resetPersistentEventLog(timestamp);
        } else if (logSaver != null) {
            uploadEventLog(false, null, timestamp);
        }
        oldEvents = store.readPreference("EVENT_LOG");
        numEvents = (short) 0;
        if (oldEvents.length > 2) {
            numEvents = (short) (((oldEvents[0] & 255) << 8) | (oldEvents[1] & 255));
        }
        numEvents = (short) (numEvents + 1);
        baos = new ByteArrayOutputStream();
        dos = new DataOutputStream(baos);
        try {
            dos.writeShort(numEvents);
            dos.write(oldEvents, 2, oldEvents.length - 2);
            dos.writeShort(type);
            dos.writeShort((int) (Math.min(timestamp - lastEventTimeMillis, 6553500) / 100));
            dos.writeUTF(status);
            dos.writeUTF(data);
            getPersistentStore().setPreference("EVENT_LOG", baos.toByteArray());
            lastEventTimeMillis = timestamp;
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public static String createEventTuple(String[] elements) {
        if (elements.length == 0) {
            return "";
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("|");
        for (int i = 0; i < elements.length; i++) {
            if (elements[i] != null) {
                StringBuffer element = new StringBuffer(elements[i]);
                TextUtil.replace("|", "", element);
                buffer.append(element);
                buffer.append("|");
            }
        }
        return buffer.toString();
    }

    public static void setLogSaver(LogSaver logSaver) {
        logSaver = logSaver;
    }

    private static Object uploadEventLog(boolean immediate, Object waitObject, long timestamp) {
        Object uploadTracker = logSaver.uploadEventLog(immediate, waitObject, getPersistentStore().readPreference("EVENT_LOG"));
        resetPersistentEventLog(timestamp);
        return uploadTracker;
    }

    private static void resetPersistentEventLog(long timestamp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        DataOutputStream dos = new DataOutputStream(baos);
        try {
            dos.writeShort(0);
            dos.writeLong(timestamp);
            lastEventTimeMillis = timestamp;
        } catch (IOException e) {
        } finally {
            getPersistentStore().setPreference("EVENT_LOG", baos.toByteArray());
        }
    }

    private static PersistentStore getPersistentStore() {
        return Config.getInstance().getPersistentStore();
    }

    private static void sendThrowable(String source, Throwable throwable, boolean isQuiet) {
        if (throwableListener != null) {
            throwableListener.onThrowable(source, throwable, isQuiet);
        }
    }

    public static void logToScreen(String logString) {
        if (onScreenPrinter != null) {
            onScreenPrinter.printToScreen(logString);
        }
    }
}
