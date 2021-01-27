package android.mtp;

import android.annotation.UnsupportedAppUsage;
import java.util.ArrayList;
import java.util.List;

/* access modifiers changed from: package-private */
public class MtpPropertyList {
    private int mCode;
    private List<Integer> mDataTypes = new ArrayList();
    private List<Long> mLongValues = new ArrayList();
    private List<Integer> mObjectHandles = new ArrayList();
    private List<Integer> mPropertyCodes = new ArrayList();
    private List<String> mStringValues = new ArrayList();

    public MtpPropertyList(int code) {
        this.mCode = code;
    }

    @UnsupportedAppUsage
    public void append(int handle, int property, int type, long value) {
        this.mObjectHandles.add(Integer.valueOf(handle));
        this.mPropertyCodes.add(Integer.valueOf(property));
        this.mDataTypes.add(Integer.valueOf(type));
        this.mLongValues.add(Long.valueOf(value));
        this.mStringValues.add(null);
    }

    @UnsupportedAppUsage
    public void append(int handle, int property, String value) {
        this.mObjectHandles.add(Integer.valueOf(handle));
        this.mPropertyCodes.add(Integer.valueOf(property));
        this.mDataTypes.add(65535);
        this.mStringValues.add(value);
        this.mLongValues.add(0L);
    }

    public int getCode() {
        return this.mCode;
    }

    public int getCount() {
        return this.mObjectHandles.size();
    }

    public int[] getObjectHandles() {
        return this.mObjectHandles.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public int[] getPropertyCodes() {
        return this.mPropertyCodes.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public int[] getDataTypes() {
        return this.mDataTypes.stream().mapToInt($$Lambda$UV1wDVoVlbcxpr8zevj_aMFtUGw.INSTANCE).toArray();
    }

    public long[] getLongValues() {
        return this.mLongValues.stream().mapToLong($$Lambda$ELHKvd8JMVRD8rbALqYPKbDX2mM.INSTANCE).toArray();
    }

    public String[] getStringValues() {
        return (String[]) this.mStringValues.toArray(new String[0]);
    }

    public void setResult(int result) {
        this.mCode = result;
    }
}
