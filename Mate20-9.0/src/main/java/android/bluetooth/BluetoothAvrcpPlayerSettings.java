package android.bluetooth;

import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;
import java.util.HashMap;
import java.util.Map;

public final class BluetoothAvrcpPlayerSettings implements Parcelable {
    public static final Parcelable.Creator<BluetoothAvrcpPlayerSettings> CREATOR = new Parcelable.Creator<BluetoothAvrcpPlayerSettings>() {
        public BluetoothAvrcpPlayerSettings createFromParcel(Parcel in) {
            return new BluetoothAvrcpPlayerSettings(in);
        }

        public BluetoothAvrcpPlayerSettings[] newArray(int size) {
            return new BluetoothAvrcpPlayerSettings[size];
        }
    };
    public static final int SETTING_EQUALIZER = 1;
    public static final int SETTING_REPEAT = 2;
    public static final int SETTING_SCAN = 8;
    public static final int SETTING_SHUFFLE = 4;
    public static final int STATE_ALL_TRACK = 3;
    public static final int STATE_GROUP = 4;
    public static final int STATE_INVALID = -1;
    public static final int STATE_OFF = 0;
    public static final int STATE_ON = 1;
    public static final int STATE_SINGLE_TRACK = 2;
    public static final String TAG = "BluetoothAvrcpPlayerSettings";
    private int mSettings;
    private Map<Integer, Integer> mSettingsValue;

    public int describeContents() {
        return 0;
    }

    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(this.mSettings);
        out.writeInt(this.mSettingsValue.size());
        for (Integer intValue : this.mSettingsValue.keySet()) {
            int k = intValue.intValue();
            out.writeInt(k);
            out.writeInt(this.mSettingsValue.get(Integer.valueOf(k)).intValue());
        }
    }

    private BluetoothAvrcpPlayerSettings(Parcel in) {
        this.mSettingsValue = new HashMap();
        this.mSettings = in.readInt();
        int numSettings = in.readInt();
        for (int i = 0; i < numSettings; i++) {
            this.mSettingsValue.put(Integer.valueOf(in.readInt()), Integer.valueOf(in.readInt()));
        }
    }

    public BluetoothAvrcpPlayerSettings(int settings) {
        this.mSettingsValue = new HashMap();
        this.mSettings = settings;
    }

    public int getSettings() {
        return this.mSettings;
    }

    public void addSettingValue(int setting, int value) {
        if ((this.mSettings & setting) != 0) {
            this.mSettingsValue.put(Integer.valueOf(setting), Integer.valueOf(value));
            return;
        }
        Log.e(TAG, "Setting not supported: " + setting + " " + this.mSettings);
        throw new IllegalStateException("Setting not supported: " + setting);
    }

    public int getSettingValue(int setting) {
        if ((this.mSettings & setting) != 0) {
            Integer i = this.mSettingsValue.get(Integer.valueOf(setting));
            if (i == null) {
                return -1;
            }
            return i.intValue();
        }
        Log.e(TAG, "Setting not supported: " + setting + " " + this.mSettings);
        throw new IllegalStateException("Setting not supported: " + setting);
    }
}
