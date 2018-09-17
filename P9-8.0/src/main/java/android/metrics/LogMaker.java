package android.metrics;

import android.content.ComponentName;
import android.media.MediaPlayer;
import android.util.Log;
import android.util.SparseArray;
import java.util.Arrays;

public class LogMaker {
    public static final int MAX_SERIALIZED_SIZE = 4000;
    private static final String TAG = "LogBuilder";
    private SparseArray<Object> entries = new SparseArray();

    public LogMaker(int category) {
        setCategory(category);
    }

    public LogMaker(Object[] items) {
        if (items != null) {
            deserialize(items);
        } else {
            setCategory(0);
        }
    }

    public LogMaker setCategory(int category) {
        this.entries.put(757, Integer.valueOf(category));
        return this;
    }

    public LogMaker clearCategory() {
        this.entries.remove(757);
        return this;
    }

    public LogMaker setType(int type) {
        this.entries.put(758, Integer.valueOf(type));
        return this;
    }

    public LogMaker clearType() {
        this.entries.remove(758);
        return this;
    }

    public LogMaker setSubtype(int subtype) {
        this.entries.put(759, Integer.valueOf(subtype));
        return this;
    }

    public LogMaker clearSubtype() {
        this.entries.remove(759);
        return this;
    }

    public LogMaker setLatency(long milliseconds) {
        this.entries.put(793, Long.valueOf(milliseconds));
        return this;
    }

    public LogMaker setTimestamp(long timestamp) {
        this.entries.put(MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING, Long.valueOf(timestamp));
        return this;
    }

    public LogMaker clearTimestamp() {
        this.entries.remove(MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING);
        return this;
    }

    public LogMaker setPackageName(String packageName) {
        this.entries.put(806, packageName);
        return this;
    }

    public LogMaker setComponentName(ComponentName component) {
        this.entries.put(806, component.getPackageName());
        this.entries.put(871, component.getClassName());
        return this;
    }

    public LogMaker clearPackageName() {
        this.entries.remove(806);
        return this;
    }

    public LogMaker setProcessId(int pid) {
        this.entries.put(865, Integer.valueOf(pid));
        return this;
    }

    public LogMaker clearProcessId() {
        this.entries.remove(865);
        return this;
    }

    public LogMaker setUid(int uid) {
        this.entries.put(943, Integer.valueOf(uid));
        return this;
    }

    public LogMaker clearUid() {
        this.entries.remove(943);
        return this;
    }

    public LogMaker setCounterName(String name) {
        this.entries.put(799, name);
        return this;
    }

    public LogMaker setCounterBucket(int bucket) {
        this.entries.put(801, Integer.valueOf(bucket));
        return this;
    }

    public LogMaker setCounterBucket(long bucket) {
        this.entries.put(801, Long.valueOf(bucket));
        return this;
    }

    public LogMaker setCounterValue(int value) {
        this.entries.put(802, Integer.valueOf(value));
        return this;
    }

    public LogMaker addTaggedData(int tag, Object value) {
        if (value == null) {
            return clearTaggedData(tag);
        }
        if (isValidValue(value)) {
            if (value.toString().getBytes().length > 4000) {
                Log.i(TAG, "Log value too long, omitted: " + value.toString());
            } else {
                this.entries.put(tag, value);
            }
            return this;
        }
        throw new IllegalArgumentException("Value must be loggable type - int, long, float, String");
    }

    public LogMaker clearTaggedData(int tag) {
        this.entries.delete(tag);
        return this;
    }

    public boolean isValidValue(Object value) {
        if ((value instanceof Integer) || (value instanceof String) || (value instanceof Long)) {
            return true;
        }
        return value instanceof Float;
    }

    public Object getTaggedData(int tag) {
        return this.entries.get(tag);
    }

    public int getCategory() {
        Object obj = this.entries.get(757);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public int getType() {
        Object obj = this.entries.get(758);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public int getSubtype() {
        Object obj = this.entries.get(759);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public long getTimestamp() {
        Object obj = this.entries.get(MediaPlayer.MEDIA_INFO_VIDEO_NOT_PLAYING);
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        return 0;
    }

    public String getPackageName() {
        Object obj = this.entries.get(806);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public int getProcessId() {
        Object obj = this.entries.get(865);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return -1;
    }

    public int getUid() {
        Object obj = this.entries.get(943);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return -1;
    }

    public String getCounterName() {
        Object obj = this.entries.get(799);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public long getCounterBucket() {
        Object obj = this.entries.get(801);
        if (obj instanceof Number) {
            return ((Number) obj).longValue();
        }
        return 0;
    }

    public boolean isLongCounterBucket() {
        return this.entries.get(801) instanceof Long;
    }

    public int getCounterValue() {
        Object obj = this.entries.get(802);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public Object[] serialize() {
        Object[] out = new Object[(this.entries.size() * 2)];
        for (int i = 0; i < this.entries.size(); i++) {
            out[i * 2] = Integer.valueOf(this.entries.keyAt(i));
            out[(i * 2) + 1] = this.entries.valueAt(i);
        }
        int size = Arrays.toString(out).getBytes().length;
        if (size <= 4000) {
            return out;
        }
        Log.i(TAG, "Log line too long, did not emit: " + size + " bytes.");
        throw new RuntimeException();
    }

    public void deserialize(Object[] items) {
        int i = 0;
        while (items != null && i < items.length) {
            Object value;
            int i2 = i + 1;
            Object key = items[i];
            if (i2 < items.length) {
                i = i2 + 1;
                value = items[i2];
                i2 = i;
            } else {
                value = null;
            }
            if (key instanceof Integer) {
                this.entries.put(((Integer) key).intValue(), value);
            } else {
                Log.i(TAG, "Invalid key " + (key == null ? "null" : key.toString()));
            }
            i = i2;
        }
    }

    public boolean isSubsetOf(LogMaker that) {
        if (that == null) {
            return false;
        }
        for (int i = 0; i < this.entries.size(); i++) {
            int key = this.entries.keyAt(i);
            Object thisValue = this.entries.valueAt(i);
            Object thatValue = that.entries.get(key);
            if ((thisValue == null && thatValue != null) || (thisValue != null && (thisValue.equals(thatValue) ^ 1) != 0)) {
                return false;
            }
        }
        return true;
    }
}
