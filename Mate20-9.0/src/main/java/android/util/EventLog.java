package android.util;

import android.annotation.SystemApi;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EventLog {
    private static final String COMMENT_PATTERN = "^\\s*(#.*)?$";
    private static final String TAG = "EventLog";
    private static final String TAGS_FILE = "/system/etc/event-log-tags";
    private static final String TAG_PATTERN = "^\\s*(\\d+)\\s+(\\w+)\\s*(\\(.*\\))?\\s*$";
    private static HashMap<String, Integer> sTagCodes = null;
    private static HashMap<Integer, String> sTagNames = null;

    public static final class Event {
        private static final int DATA_OFFSET = 4;
        private static final byte FLOAT_TYPE = 4;
        private static final int HEADER_SIZE_OFFSET = 2;
        private static final byte INT_TYPE = 0;
        private static final int LENGTH_OFFSET = 0;
        private static final byte LIST_TYPE = 3;
        private static final byte LONG_TYPE = 1;
        private static final int NANOSECONDS_OFFSET = 16;
        private static final int PROCESS_OFFSET = 4;
        private static final int SECONDS_OFFSET = 12;
        private static final byte STRING_TYPE = 2;
        private static final int THREAD_OFFSET = 8;
        private static final int UID_OFFSET = 24;
        private static final int V1_PAYLOAD_START = 20;
        private final ByteBuffer mBuffer;
        private Exception mLastWtf;

        Event(byte[] data) {
            this.mBuffer = ByteBuffer.wrap(data);
            this.mBuffer.order(ByteOrder.nativeOrder());
        }

        public int getProcessId() {
            return this.mBuffer.getInt(4);
        }

        @SystemApi
        public int getUid() {
            try {
                return this.mBuffer.getInt(24);
            } catch (IndexOutOfBoundsException e) {
                return -1;
            }
        }

        public int getThreadId() {
            return this.mBuffer.getInt(8);
        }

        public long getTimeNanos() {
            return (((long) this.mBuffer.getInt(12)) * 1000000000) + ((long) this.mBuffer.getInt(16));
        }

        public int getTag() {
            int offset = this.mBuffer.getShort(2);
            if (offset == 0) {
                offset = 20;
            }
            return this.mBuffer.getInt(offset);
        }

        public synchronized Object getData() {
            try {
                int offset = this.mBuffer.getShort(2);
                if (offset == 0) {
                    offset = 20;
                }
                this.mBuffer.limit(this.mBuffer.getShort(0) + offset);
                if (offset + 4 >= this.mBuffer.limit()) {
                    return null;
                }
                this.mBuffer.position(offset + 4);
                return decodeObject();
            } catch (IllegalArgumentException e) {
                Log.wtf(EventLog.TAG, "Illegal entry payload: tag=" + getTag(), e);
                this.mLastWtf = e;
                return null;
            } catch (BufferUnderflowException e2) {
                Log.wtf(EventLog.TAG, "Truncated entry payload: tag=" + getTag(), e2);
                this.mLastWtf = e2;
                return null;
            }
        }

        private Object decodeObject() {
            byte type = this.mBuffer.get();
            switch (type) {
                case 0:
                    return Integer.valueOf(this.mBuffer.getInt());
                case 1:
                    return Long.valueOf(this.mBuffer.getLong());
                case 2:
                    try {
                        int length = this.mBuffer.getInt();
                        int start = this.mBuffer.position();
                        this.mBuffer.position(start + length);
                        return new String(this.mBuffer.array(), start, length, "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        Log.wtf(EventLog.TAG, "UTF-8 is not supported", e);
                        this.mLastWtf = e;
                        return null;
                    }
                case 3:
                    int length2 = this.mBuffer.get();
                    if (length2 < 0) {
                        length2 += 256;
                    }
                    Object[] array = new Object[length2];
                    for (int i = 0; i < length2; i++) {
                        array[i] = decodeObject();
                    }
                    return array;
                case 4:
                    return Float.valueOf(this.mBuffer.getFloat());
                default:
                    throw new IllegalArgumentException("Unknown entry type: " + type);
            }
        }

        public static Event fromBytes(byte[] data) {
            return new Event(data);
        }

        public byte[] getBytes() {
            byte[] bytes = this.mBuffer.array();
            return Arrays.copyOf(bytes, bytes.length);
        }

        public Exception getLastError() {
            return this.mLastWtf;
        }

        public void clearError() {
            this.mLastWtf = null;
        }

        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            return Arrays.equals(this.mBuffer.array(), ((Event) o).mBuffer.array());
        }

        public int hashCode() {
            return Arrays.hashCode(this.mBuffer.array());
        }
    }

    public static native void readEvents(int[] iArr, Collection<Event> collection) throws IOException;

    @SystemApi
    public static native void readEventsOnWrapping(int[] iArr, long j, Collection<Event> collection) throws IOException;

    public static native int writeEvent(int i, float f);

    public static native int writeEvent(int i, int i2);

    public static native int writeEvent(int i, long j);

    public static native int writeEvent(int i, String str);

    public static native int writeEvent(int i, Object... objArr);

    public static String getTagName(int tag) {
        readTagsFile();
        return sTagNames.get(Integer.valueOf(tag));
    }

    public static int getTagCode(String name) {
        readTagsFile();
        Integer code = sTagCodes.get(name);
        if (code != null) {
            return code.intValue();
        }
        return -1;
    }

    private static synchronized void readTagsFile() {
        synchronized (EventLog.class) {
            if (sTagCodes == null || sTagNames == null) {
                sTagCodes = new HashMap<>();
                sTagNames = new HashMap<>();
                Pattern comment = Pattern.compile(COMMENT_PATTERN);
                Pattern tag = Pattern.compile(TAG_PATTERN);
                BufferedReader reader = null;
                try {
                    reader = new BufferedReader(new FileReader(TAGS_FILE), 256);
                    while (true) {
                        String readLine = reader.readLine();
                        String line = readLine;
                        if (readLine == null) {
                            try {
                                break;
                            } catch (IOException e) {
                            }
                        } else if (!comment.matcher(line).matches()) {
                            Matcher m = tag.matcher(line);
                            if (!m.matches()) {
                                Log.wtf(TAG, "Bad entry in /system/etc/event-log-tags: " + line);
                            } else {
                                try {
                                    int num = Integer.parseInt(m.group(1));
                                    String name = m.group(2);
                                    sTagCodes.put(name, Integer.valueOf(num));
                                    sTagNames.put(Integer.valueOf(num), name);
                                } catch (NumberFormatException e2) {
                                    Log.wtf(TAG, "Error in /system/etc/event-log-tags: " + line, e2);
                                }
                            }
                        }
                    }
                    reader.close();
                } catch (IOException e3) {
                    try {
                        Log.wtf(TAG, "Error reading /system/etc/event-log-tags", e3);
                        if (reader != null) {
                            reader.close();
                        }
                        return;
                    } catch (Throwable th) {
                        if (reader != null) {
                            try {
                                reader.close();
                            } catch (IOException e4) {
                            }
                        }
                        throw th;
                    }
                }
            } else {
                return;
            }
        }
    }
}
