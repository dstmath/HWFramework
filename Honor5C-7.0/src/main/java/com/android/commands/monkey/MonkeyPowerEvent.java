package com.android.commands.monkey;

import android.app.IActivityManager;
import android.content.ContentValues;
import android.os.Build;
import android.util.Log;
import android.view.IWindowManager;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

public class MonkeyPowerEvent extends MonkeyEvent {
    private static final String LOG_FILE = "/sdcard/autotester.log";
    private static final String TAG = "PowerTester";
    private static final String TEST_DELAY_STARTED = "AUTOTEST_TEST_BEGIN_DELAY";
    private static final String TEST_ENDED = "AUTOTEST_TEST_SUCCESS";
    private static final String TEST_IDLE_ENDED = "AUTOTEST_IDLE_SUCCESS";
    private static final String TEST_SEQ_BEGIN = "AUTOTEST_SEQUENCE_BEGIN";
    private static final String TEST_STARTED = "AUTOTEST_TEST_BEGIN";
    private static final long USB_DELAY_TIME = 10000;
    private static ArrayList<ContentValues> mLogEvents;
    private static long mTestStartTime;
    private String mPowerLogTag;
    private String mTestResult;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.commands.monkey.MonkeyPowerEvent.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.commands.monkey.MonkeyPowerEvent.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.commands.monkey.MonkeyPowerEvent.<clinit>():void");
    }

    public MonkeyPowerEvent(String powerLogTag, String powerTestResult) {
        super(4);
        this.mPowerLogTag = powerLogTag;
        this.mTestResult = powerTestResult;
    }

    public MonkeyPowerEvent(String powerLogTag) {
        super(4);
        this.mPowerLogTag = powerLogTag;
        this.mTestResult = null;
    }

    public MonkeyPowerEvent() {
        super(4);
        this.mPowerLogTag = null;
        this.mTestResult = null;
    }

    private void bufferLogEvent(String tag, String value) {
        long tagTime = System.currentTimeMillis();
        if (tag.compareTo(TEST_STARTED) == 0) {
            mTestStartTime = tagTime;
        } else if (tag.compareTo(TEST_IDLE_ENDED) == 0) {
            tagTime = mTestStartTime + Long.parseLong(value);
            tag = TEST_ENDED;
        } else if (tag.compareTo(TEST_DELAY_STARTED) == 0) {
            mTestStartTime = USB_DELAY_TIME + tagTime;
            tagTime = mTestStartTime;
            tag = TEST_STARTED;
        }
        ContentValues event = new ContentValues();
        event.put("date", Long.valueOf(tagTime));
        event.put("tag", tag);
        if (value != null) {
            event.put("value", value);
        }
        mLogEvents.add(event);
    }

    private void writeLogEvents() {
        IOException e;
        Throwable th;
        ContentValues[] events = (ContentValues[]) mLogEvents.toArray(new ContentValues[0]);
        mLogEvents.clear();
        FileWriter fileWriter = null;
        try {
            StringBuffer buffer = new StringBuffer();
            for (ContentValues event : events) {
                buffer.append(MonkeyUtils.toCalendarTime(event.getAsLong("date").longValue()));
                buffer.append(event.getAsString("tag"));
                if (event.containsKey("value")) {
                    String value = event.getAsString("value");
                    buffer.append(" ");
                    buffer.append(value.replace('\n', '/'));
                }
                buffer.append("\n");
            }
            FileWriter writer = new FileWriter(LOG_FILE, true);
            try {
                writer.write(buffer.toString());
                if (writer != null) {
                    try {
                        writer.close();
                    } catch (IOException e2) {
                    }
                }
                fileWriter = writer;
            } catch (IOException e3) {
                e = e3;
                fileWriter = writer;
                try {
                    Log.w(TAG, "Can't write sdcard log file", e);
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e4) {
                        }
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (fileWriter != null) {
                        try {
                            fileWriter.close();
                        } catch (IOException e5) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileWriter = writer;
                if (fileWriter != null) {
                    fileWriter.close();
                }
                throw th;
            }
        } catch (IOException e6) {
            e = e6;
            Log.w(TAG, "Can't write sdcard log file", e);
            if (fileWriter != null) {
                fileWriter.close();
            }
        }
    }

    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        if (this.mPowerLogTag == null) {
            writeLogEvents();
        } else if (this.mPowerLogTag.compareTo(TEST_SEQ_BEGIN) == 0) {
            bufferLogEvent(this.mPowerLogTag, Build.FINGERPRINT);
        } else if (this.mTestResult != null) {
            bufferLogEvent(this.mPowerLogTag, this.mTestResult);
        }
        return 1;
    }
}
