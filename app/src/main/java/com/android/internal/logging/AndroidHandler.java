package com.android.internal.logging;

import android.util.FrameworkTagConstant;
import android.util.Log;
import com.android.internal.telephony.RILConstants;
import dalvik.system.DalvikLogHandler;
import dalvik.system.DalvikLogging;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;

public class AndroidHandler extends Handler implements DalvikLogHandler {
    private static final Formatter THE_FORMATTER = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.logging.AndroidHandler.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.logging.AndroidHandler.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.logging.AndroidHandler.<clinit>():void");
    }

    public AndroidHandler() {
        setFormatter(THE_FORMATTER);
    }

    public void close() {
    }

    public void flush() {
    }

    public void publish(LogRecord record) {
        int level = getAndroidLevel(record.getLevel());
        String tag = DalvikLogging.loggerNameToTag(record.getLoggerName());
        if (Log.isLoggable(tag, level)) {
            try {
                Log.println(level, tag, getFormatter().format(record));
            } catch (RuntimeException e) {
                Log.e("AndroidHandler", "Error logging message.", e);
            }
        }
    }

    public void publish(Logger source, String tag, Level level, String message) {
        int priority = getAndroidLevel(level);
        if (Log.isLoggable(tag, priority)) {
            try {
                Log.println(priority, tag, message);
            } catch (RuntimeException e) {
                Log.e("AndroidHandler", "Error logging message.", e);
            }
        }
    }

    static int getAndroidLevel(Level level) {
        int value = level.intValue();
        if (value >= RILConstants.RIL_UNSOL_RESPONSE_RADIO_STATE_CHANGED) {
            return 6;
        }
        if (value >= FrameworkTagConstant.HWTAG_USERS) {
            return 5;
        }
        if (value >= RILConstants.RIL_RESPONSE_ACKNOWLEDGEMENT) {
            return 4;
        }
        return 3;
    }
}
