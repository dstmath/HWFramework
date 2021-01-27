package huawei.hiview;

import android.common.HwFrameworkFactory;
import android.util.Log;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class HiEvent {
    static final int INVALID_NUM = -1;
    static final int MAX_ARR_SIZE = 100;
    static final int MAX_FILE_NUM = 10;
    static final int MAX_KEY_LEN = 32;
    static final int MAX_PAIR_NUM = 256;
    static final int MAX_PATH_LEN = 256;
    static final int MAX_VALUE_LEN = 1024;
    private static final String TAG = "HiView.HiEvent";
    private Set<String> mFilePaths;
    private String mFlattened;
    private int mId;
    private long mParentSpanId = -1;
    private Payload mPayload;
    private long mSpanId = -1;
    private Status mStatus = Status.NORMAL;
    private Date mTime = new Date(0);
    private int mTraceFlag = -1;
    private long mTraceId = -1;

    private enum Status {
        NORMAL,
        FLATTERNED
    }

    public HiEvent(int id) {
        this.mId = id;
        initPayload();
    }

    public HiEvent setTime(Date date) {
        if (date != null) {
            Object clone = date.clone();
            if (clone instanceof Date) {
                this.mTime = (Date) clone;
            }
        }
        return this;
    }

    public HiEvent putBool(String key, boolean value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putBoolArray(String key, boolean[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putByte(String key, byte value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putByteArray(String key, byte[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putShort(String key, short value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putShortArray(String key, short[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putInt(String key, int value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putIntArray(String key, int[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putLong(String key, long value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putLongArray(String key, long[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putFloat(String key, float value) {
        if (checkValid(key, 32)) {
            this.mPayload.put(key, value);
        }
        return this;
    }

    public HiEvent putFloatArray(String key, float[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putString(String key, String value) {
        if (checkValid(key, 32)) {
            if (checkValid(value, 1024)) {
                this.mPayload.put(key, value);
                return this;
            } else if (value != null && value.length() > 1024) {
                this.mPayload.put(key, value.substring(0, 1024));
            }
        }
        return this;
    }

    public HiEvent putStringArray(String key, String[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        for (int i = 0; i < len; i++) {
            this.mPayload.append(key, value[i]);
        }
        return this;
    }

    public HiEvent putHiEvent(String key, HiEvent value) {
        if (checkValid(key, 32) && value != null) {
            this.mPayload.put(key, value.getPayload());
        }
        return this;
    }

    public HiEvent putHiEventArray(String key, HiEvent[] value) {
        if (!checkValid(key, 32) || value == null) {
            return this;
        }
        int len = Math.min(value.length, 100);
        int i = 0;
        while (i < len && value[i] != null) {
            this.mPayload.append(key, value[i].getPayload());
            i++;
        }
        return this;
    }

    public HiEvent addFilePath(String path) {
        if (checkValid(path, 256)) {
            if (this.mFilePaths == null) {
                this.mFilePaths = new HashSet();
            }
            if (this.mFilePaths.size() < 10) {
                this.mFilePaths.add(path);
            }
        }
        return this;
    }

    /* access modifiers changed from: package-private */
    public HiEvent putPayload(Payload payload) {
        Payload payload2 = this.mPayload;
        if (payload2 != null) {
            payload2.merge(payload);
        } else {
            this.mPayload = payload;
        }
        return this;
    }

    public HiEvent reset() {
        this.mTime.setTime(0);
        this.mStatus = Status.NORMAL;
        Payload payload = this.mPayload;
        if (payload != null) {
            payload.clear();
        }
        Set<String> set = this.mFilePaths;
        if (set != null) {
            set.clear();
        }
        this.mFlattened = null;
        return this;
    }

    public int getId() {
        return this.mId;
    }

    /* access modifiers changed from: package-private */
    public Payload getPayload() {
        return this.mPayload;
    }

    /* access modifiers changed from: package-private */
    public String flatten() {
        if (this.mStatus != Status.FLATTERNED || this.mFlattened == null) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("eventid ");
            stringBuilder.append(this.mId);
            Set<String> set = this.mFilePaths;
            if (set != null) {
                for (String path : set) {
                    stringBuilder.append(" -i ");
                    stringBuilder.append(path);
                }
            }
            stringBuilder.append(" -t ");
            stringBuilder.append(this.mTime.getTime() / 1000);
            if (this.mTraceId != -1) {
                stringBuilder.append(" -r ");
                stringBuilder.append(Long.toHexString(this.mTraceId));
            }
            if (this.mTraceFlag != -1) {
                stringBuilder.append(" -f ");
                stringBuilder.append(Integer.toHexString(this.mTraceFlag));
            }
            if (this.mSpanId != -1) {
                stringBuilder.append(" -s ");
                stringBuilder.append(Long.toHexString(this.mSpanId));
            }
            if (this.mParentSpanId != -1) {
                stringBuilder.append(" -p ");
                stringBuilder.append(Long.toHexString(this.mParentSpanId));
            }
            Payload payload = this.mPayload;
            if (payload != null && payload.size() > 0) {
                stringBuilder.append(" --jextra ");
                stringBuilder.append(this.mPayload.toString());
            }
            this.mFlattened = stringBuilder.toString();
            this.mStatus = Status.FLATTERNED;
            Log.i(TAG, "Flatten done:" + this.mId);
            return this.mFlattened;
        }
        Log.i(TAG, "has flattened:" + this.mFlattened);
        return this.mFlattened;
    }

    /* access modifiers changed from: package-private */
    public boolean setTraceInfo() {
        HiTraceId traceId = HwFrameworkFactory.getHiTrace().getId();
        if (traceId == null || !traceId.isValid()) {
            return false;
        }
        this.mTraceId = traceId.getChainId();
        this.mTraceFlag = traceId.getFlags();
        this.mSpanId = traceId.getSpanId();
        this.mParentSpanId = traceId.getParentSpanId();
        Log.i(TAG, "trace id:" + Long.toHexString(this.mTraceId) + " trace flag:" + Integer.toHexString(this.mTraceFlag) + " span id:" + Long.toHexString(this.mSpanId) + " parent span id:" + Long.toHexString(this.mParentSpanId));
        return true;
    }

    private void initPayload() {
        this.mPayload = new JsonPayload();
    }

    private boolean checkValid(String src, int maxThreshold) {
        if (src == null) {
            return false;
        }
        int length = src.length();
        if (length != 0 && length <= maxThreshold) {
            return true;
        }
        Log.i(TAG, src + " length is 0 or exceed MAX: 32");
        return false;
    }
}
