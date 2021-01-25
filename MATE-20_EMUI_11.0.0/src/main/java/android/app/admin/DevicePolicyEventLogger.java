package android.app.admin;

import android.content.ComponentName;
import android.stats.devicepolicy.nano.StringList;
import android.util.StatsLog;
import com.android.framework.protobuf.nano.MessageNano;
import com.android.internal.util.Preconditions;
import java.util.Arrays;

public class DevicePolicyEventLogger {
    private String mAdminPackageName;
    private boolean mBooleanValue;
    private final int mEventId;
    private int mIntValue;
    private String[] mStringArrayValue;
    private long mTimePeriodMs;

    private DevicePolicyEventLogger(int eventId) {
        this.mEventId = eventId;
    }

    public static DevicePolicyEventLogger createEvent(int eventId) {
        return new DevicePolicyEventLogger(eventId);
    }

    public int getEventId() {
        return this.mEventId;
    }

    public DevicePolicyEventLogger setInt(int value) {
        this.mIntValue = value;
        return this;
    }

    public int getInt() {
        return this.mIntValue;
    }

    public DevicePolicyEventLogger setBoolean(boolean value) {
        this.mBooleanValue = value;
        return this;
    }

    public boolean getBoolean() {
        return this.mBooleanValue;
    }

    public DevicePolicyEventLogger setTimePeriod(long timePeriodMillis) {
        this.mTimePeriodMs = timePeriodMillis;
        return this;
    }

    public long getTimePeriod() {
        return this.mTimePeriodMs;
    }

    public DevicePolicyEventLogger setStrings(String... values) {
        this.mStringArrayValue = values;
        return this;
    }

    public DevicePolicyEventLogger setStrings(String value, String[] values) {
        Preconditions.checkNotNull(values, "values parameter cannot be null");
        this.mStringArrayValue = new String[(values.length + 1)];
        String[] strArr = this.mStringArrayValue;
        strArr[0] = value;
        System.arraycopy(values, 0, strArr, 1, values.length);
        return this;
    }

    public DevicePolicyEventLogger setStrings(String value1, String value2, String[] values) {
        Preconditions.checkNotNull(values, "values parameter cannot be null");
        this.mStringArrayValue = new String[(values.length + 2)];
        String[] strArr = this.mStringArrayValue;
        strArr[0] = value1;
        strArr[1] = value2;
        System.arraycopy(values, 0, strArr, 2, values.length);
        return this;
    }

    public String[] getStringArray() {
        String[] strArr = this.mStringArrayValue;
        if (strArr == null) {
            return null;
        }
        return (String[]) Arrays.copyOf(strArr, strArr.length);
    }

    public DevicePolicyEventLogger setAdmin(String packageName) {
        this.mAdminPackageName = packageName;
        return this;
    }

    public DevicePolicyEventLogger setAdmin(ComponentName componentName) {
        this.mAdminPackageName = componentName != null ? componentName.getPackageName() : null;
        return this;
    }

    public String getAdminPackageName() {
        return this.mAdminPackageName;
    }

    public void write() {
        StatsLog.write(103, this.mEventId, this.mAdminPackageName, this.mIntValue, this.mBooleanValue, this.mTimePeriodMs, stringArrayValueToBytes(this.mStringArrayValue));
    }

    private static byte[] stringArrayValueToBytes(String[] array) {
        if (array == null) {
            return null;
        }
        StringList stringList = new StringList();
        stringList.stringValue = array;
        return MessageNano.toByteArray(stringList);
    }
}
