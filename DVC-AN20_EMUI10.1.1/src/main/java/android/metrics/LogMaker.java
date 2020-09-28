package android.metrics;

import android.annotation.SystemApi;
import android.content.ComponentName;
import android.util.Log;
import android.util.SparseArray;
import com.android.internal.annotations.VisibleForTesting;
import com.android.internal.logging.nano.MetricsProto;

@SystemApi
public class LogMaker {
    @VisibleForTesting
    public static final int MAX_SERIALIZED_SIZE = 4000;
    private static final String TAG = "LogBuilder";
    private SparseArray<Object> entries = new SparseArray<>();

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
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_CATEGORY, Integer.valueOf(category));
        return this;
    }

    public LogMaker clearCategory() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_CATEGORY);
        return this;
    }

    public LogMaker setType(int type) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_TYPE, Integer.valueOf(type));
        return this;
    }

    public LogMaker clearType() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_TYPE);
        return this;
    }

    public LogMaker setSubtype(int subtype) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_SUBTYPE, Integer.valueOf(subtype));
        return this;
    }

    public LogMaker clearSubtype() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_SUBTYPE);
        return this;
    }

    public LogMaker setLatency(long milliseconds) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_LATENCY_MILLIS, Long.valueOf(milliseconds));
        return this;
    }

    public LogMaker setTimestamp(long timestamp) {
        this.entries.put(805, Long.valueOf(timestamp));
        return this;
    }

    public LogMaker clearTimestamp() {
        this.entries.remove(805);
        return this;
    }

    public LogMaker setPackageName(String packageName) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PACKAGENAME, packageName);
        return this;
    }

    public LogMaker setComponentName(ComponentName component) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PACKAGENAME, component.getPackageName());
        this.entries.put(MetricsProto.MetricsEvent.FIELD_CLASS_NAME, component.getClassName());
        return this;
    }

    public LogMaker clearPackageName() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PACKAGENAME);
        return this;
    }

    public LogMaker setProcessId(int pid) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PID, Integer.valueOf(pid));
        return this;
    }

    public LogMaker clearProcessId() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PID);
        return this;
    }

    public LogMaker setUid(int uid) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_UID, Integer.valueOf(uid));
        return this;
    }

    public LogMaker clearUid() {
        this.entries.remove(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_UID);
        return this;
    }

    public LogMaker setCounterName(String name) {
        this.entries.put(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_NAME, name);
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
        return (value instanceof Integer) || (value instanceof String) || (value instanceof Long) || (value instanceof Float);
    }

    public Object getTaggedData(int tag) {
        return this.entries.get(tag);
    }

    public int getCategory() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_CATEGORY);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public int getType() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_TYPE);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public int getSubtype() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_SUBTYPE);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return 0;
    }

    public long getTimestamp() {
        Object obj = this.entries.get(805);
        if (obj instanceof Long) {
            return ((Long) obj).longValue();
        }
        return 0;
    }

    public String getPackageName() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PACKAGENAME);
        if (obj instanceof String) {
            return (String) obj;
        }
        return null;
    }

    public int getProcessId() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_PID);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return -1;
    }

    public int getUid() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_UID);
        if (obj instanceof Integer) {
            return ((Integer) obj).intValue();
        }
        return -1;
    }

    public String getCounterName() {
        Object obj = this.entries.get(MetricsProto.MetricsEvent.RESERVED_FOR_LOGBUILDER_NAME);
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
        int size = out.toString().getBytes().length;
        if (size <= 4000) {
            return out;
        }
        Log.i(TAG, "Log line too long, did not emit: " + size + " bytes.");
        throw new RuntimeException();
    }

    /* JADX INFO: Multiple debug info for r0v2 java.lang.Object: [D('i' int), D('key' java.lang.Object)] */
    public void deserialize(Object[] items) {
        int i;
        Object value;
        int i2 = 0;
        while (items != null && i2 < items.length) {
            int i3 = i2 + 1;
            Object key = items[i2];
            if (i3 < items.length) {
                i = i3 + 1;
                value = items[i3];
            } else {
                i = i3;
                value = null;
            }
            if (key instanceof Integer) {
                this.entries.put(((Integer) key).intValue(), value);
            } else {
                StringBuilder sb = new StringBuilder();
                sb.append("Invalid key ");
                sb.append(key == null ? "null" : key.toString());
                Log.i(TAG, sb.toString());
            }
            i2 = i;
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
            if ((thisValue == null && thatValue != null) || !thisValue.equals(thatValue)) {
                return false;
            }
        }
        return true;
    }

    public SparseArray<Object> getEntries() {
        return this.entries;
    }
}
