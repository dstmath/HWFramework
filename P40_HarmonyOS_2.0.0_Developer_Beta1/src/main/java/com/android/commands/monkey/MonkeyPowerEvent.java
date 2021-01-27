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
    private static ArrayList<ContentValues> mLogEvents = new ArrayList<>();
    private static long mTestStartTime;
    private String mPowerLogTag;
    private String mTestResult;

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
        ContentValues[] events = (ContentValues[]) mLogEvents.toArray(new ContentValues[0]);
        mLogEvents.clear();
        FileWriter writer = null;
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
            FileWriter writer2 = new FileWriter(LOG_FILE, true);
            writer2.write(buffer.toString());
            try {
                writer2.close();
            } catch (IOException e) {
            }
        } catch (IOException e2) {
            Log.w(TAG, "Can't write sdcard log file", e2);
            if (0 != 0) {
                writer.close();
            }
        } catch (Throwable th) {
            if (0 != 0) {
                try {
                    writer.close();
                } catch (IOException e3) {
                }
            }
            throw th;
        }
    }

    @Override // com.android.commands.monkey.MonkeyEvent
    public int injectEvent(IWindowManager iwm, IActivityManager iam, int verbose) {
        String str = this.mPowerLogTag;
        if (str == null) {
            writeLogEvents();
            return 1;
        } else if (str.compareTo(TEST_SEQ_BEGIN) == 0) {
            bufferLogEvent(this.mPowerLogTag, Build.FINGERPRINT);
            return 1;
        } else {
            String str2 = this.mTestResult;
            if (str2 == null) {
                return 1;
            }
            bufferLogEvent(this.mPowerLogTag, str2);
            return 1;
        }
    }
}
