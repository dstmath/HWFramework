package ohos.hiviewdfx;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import ohos.app.Context;
import ohos.bundle.BundleInfo;
import ohos.bundle.IBundleManager;
import ohos.rpc.RemoteException;

public class HiEvent {
    private static final String APP_NAME = "PNAMEID";
    private static final String APP_VERSION = "PVERSIONID";
    static final int INVALID_NUM = -1;
    private static final HiLogLabel LABEL = new HiLogLabel(3, 218115329, "HiView.HiEvent");
    static final int MAX_ARR_SIZE = 100;
    static final int MAX_FILE_NUM = 10;
    static final int MAX_KEY_LEN = 32;
    static final int MAX_PAIR_NUM = 256;
    static final int MAX_PATH_LEN = 256;
    static final int MAX_VALUE_LEN = 128;
    private Context lastContext;
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
    private String name;
    private String version;

    private enum Status {
        NORMAL,
        FLATTERNED
    }

    public HiEvent(int i) {
        this.mId = i;
        initPayload();
    }

    public HiEvent setTime(Date date) {
        if (date != null) {
            this.mTime = (Date) date.clone();
        }
        return this;
    }

    public HiEvent putBool(String str, boolean z) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, z);
        }
        return this;
    }

    public HiEvent putBoolArray(String str, boolean[] zArr) {
        if (!checkValid(str, 32) || zArr == null) {
            return this;
        }
        int min = Math.min(zArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, zArr[i]);
        }
        return this;
    }

    public HiEvent putByte(String str, byte b) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, b);
        }
        return this;
    }

    public HiEvent putByteArray(String str, byte[] bArr) {
        if (!checkValid(str, 32) || bArr == null) {
            return this;
        }
        int min = Math.min(bArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, bArr[i]);
        }
        return this;
    }

    public HiEvent putShort(String str, short s) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, s);
        }
        return this;
    }

    public HiEvent putShortArray(String str, short[] sArr) {
        if (!checkValid(str, 32) || sArr == null) {
            return this;
        }
        int min = Math.min(sArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, sArr[i]);
        }
        return this;
    }

    public HiEvent putInt(String str, int i) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, i);
        }
        return this;
    }

    public HiEvent putIntArray(String str, int[] iArr) {
        if (!checkValid(str, 32) || iArr == null) {
            return this;
        }
        int min = Math.min(iArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, iArr[i]);
        }
        return this;
    }

    public HiEvent putLong(String str, long j) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, j);
        }
        return this;
    }

    public HiEvent putLongArray(String str, long[] jArr) {
        if (!checkValid(str, 32) || jArr == null) {
            return this;
        }
        int min = Math.min(jArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, jArr[i]);
        }
        return this;
    }

    public HiEvent putFloat(String str, float f) {
        if (checkValid(str, 32)) {
            this.mPayload.put(str, f);
        }
        return this;
    }

    public HiEvent putFloatArray(String str, float[] fArr) {
        if (!checkValid(str, 32) || fArr == null) {
            return this;
        }
        int min = Math.min(fArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, fArr[i]);
        }
        return this;
    }

    public HiEvent putString(String str, String str2) {
        if (checkValid(str, 32) && checkValid(str2, 128)) {
            this.mPayload.put(str, str2);
        }
        return this;
    }

    public HiEvent putStringArray(String str, String[] strArr) {
        if (!checkValid(str, 32) || strArr == null) {
            return this;
        }
        int min = Math.min(strArr.length, 100);
        for (int i = 0; i < min; i++) {
            this.mPayload.append(str, strArr[i]);
        }
        return this;
    }

    public HiEvent putHiEvent(String str, HiEvent hiEvent) {
        if (checkValid(str, 32) && hiEvent != null) {
            this.mPayload.put(str, hiEvent.getPayload());
        }
        return this;
    }

    public HiEvent putHiEventArray(String str, HiEvent[] hiEventArr) {
        if (!checkValid(str, 32) || hiEventArr == null) {
            return this;
        }
        int min = Math.min(hiEventArr.length, 100);
        int i = 0;
        while (i < min && hiEventArr[i] != null) {
            this.mPayload.append(str, hiEventArr[i].getPayload());
            i++;
        }
        return this;
    }

    public HiEvent addFilePath(String str) {
        if (checkValid(str, 256)) {
            if (this.mFilePaths == null) {
                this.mFilePaths = new HashSet();
            }
            if (this.mFilePaths.size() < 10) {
                this.mFilePaths.add(str);
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

    public HiEvent putAppInfo(Context context) {
        BundleInfo bundleInfo;
        if (context == null) {
            return this;
        }
        Context context2 = this.lastContext;
        if (context2 == null || !context2.equals(context)) {
            this.lastContext = context;
            try {
                this.name = context.getBundleName();
                IBundleManager bundleManager = context.getBundleManager();
                if (bundleManager == null || (bundleInfo = bundleManager.getBundleInfo(context.getBundleName(), 0)) == null) {
                    return this;
                }
                this.version = bundleInfo.getVersionName();
            } catch (RemoteException unused) {
                HiLog.error(LABEL, "HiEventEx putAppInfo NameNotFoundException", new Object[0]);
            }
        }
        putString(APP_NAME, this.name);
        putString(APP_VERSION, this.version);
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

    /* access modifiers changed from: package-private */
    public int getId() {
        return this.mId;
    }

    /* access modifiers changed from: package-private */
    public Payload getPayload() {
        return this.mPayload;
    }

    /* access modifiers changed from: package-private */
    public String flatten() {
        String str;
        if (this.mStatus == Status.FLATTERNED && (str = this.mFlattened) != null) {
            return str;
        }
        StringBuilder sb = new StringBuilder();
        sb.append("eventid ");
        sb.append(this.mId);
        Set<String> set = this.mFilePaths;
        if (set != null) {
            for (String str2 : set) {
                sb.append(" -i ");
                sb.append(str2);
            }
        }
        sb.append(" -t ");
        sb.append(this.mTime.getTime() / 1000);
        if (this.mTraceId != -1) {
            sb.append(" -r ");
            sb.append(Long.toHexString(this.mTraceId));
        }
        if (this.mTraceFlag != -1) {
            sb.append(" -f ");
            sb.append(Integer.toHexString(this.mTraceFlag));
        }
        if (this.mSpanId != -1) {
            sb.append(" -s ");
            sb.append(Long.toHexString(this.mSpanId));
        }
        if (this.mParentSpanId != -1) {
            sb.append(" -p ");
            sb.append(Long.toHexString(this.mParentSpanId));
        }
        Payload payload = this.mPayload;
        if (payload != null && payload.size() > 0) {
            sb.append(" --jextra ");
            sb.append(this.mPayload.toString());
        }
        this.mFlattened = sb.toString();
        this.mStatus = Status.FLATTERNED;
        return this.mFlattened;
    }

    /* access modifiers changed from: package-private */
    public boolean setTraceInfo() {
        HiTraceId id = HiTrace.getId();
        if (!id.isValid()) {
            return false;
        }
        this.mTraceId = id.getChainId();
        this.mTraceFlag = id.getFlags();
        this.mSpanId = id.getSpanId();
        this.mParentSpanId = id.getParentSpanId();
        return true;
    }

    private void initPayload() {
        this.mPayload = new JsonPayload();
    }

    private boolean checkValid(String str, int i) {
        if (str == null) {
            return false;
        }
        int length = str.length();
        if (length != 0 && length <= i) {
            return true;
        }
        HiLog.error(LABEL, "%{public}s, length is 0 or exceed MAX: %{public}d", str, 32);
        return false;
    }
}
