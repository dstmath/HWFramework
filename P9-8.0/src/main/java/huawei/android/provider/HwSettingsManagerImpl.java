package huawei.android.provider;

import android.common.HwSettingsManager;
import android.content.ContentResolver;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.provider.Settings.SettingNotFoundException;
import huawei.android.os.HwEnvironment;
import huawei.android.provider.HwSettings.Systemex;
import java.io.File;

public class HwSettingsManagerImpl implements HwSettingsManager {
    private static final boolean LOCAL_LOGV = false;
    private static final String TAG = "HwSettingsManagerImpl";
    private static HwSettingsManager mHwSettingsManager = null;

    private HwSettingsManagerImpl() {
    }

    public static HwSettingsManager getDefault() {
        if (mHwSettingsManager == null) {
            mHwSettingsManager = new HwSettingsManagerImpl();
        }
        return mHwSettingsManager;
    }

    public String getString(ContentResolver cr, String name) {
        return Systemex.getString(cr, name);
    }

    public boolean putString(ContentResolver cr, String name, String value) {
        return Systemex.putString(cr, name, value);
    }

    public int getInt(ContentResolver cr, String name, int def) {
        return Systemex.getInt(cr, name, def);
    }

    public int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
        return Systemex.getInt(cr, name);
    }

    public boolean putInt(ContentResolver cr, String name, int value) {
        return Systemex.putInt(cr, name, value);
    }

    public long getLong(ContentResolver cr, String name, long def) {
        return Systemex.getLong(cr, name, def);
    }

    public long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
        return Systemex.getLong(cr, name);
    }

    public boolean putLong(ContentResolver cr, String name, long value) {
        return Systemex.putLong(cr, name, value);
    }

    public float getFloat(ContentResolver cr, String name, float def) {
        return Systemex.getFloat(cr, name, def);
    }

    public float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
        return Systemex.getFloat(cr, name);
    }

    public boolean putFloat(ContentResolver cr, String name, float value) {
        return Systemex.putFloat(cr, name, value);
    }

    public void setAirplaneMode(ContentResolver cr, String name) {
        if ("true".equals(SystemProperties.get("ro.poweroff_alarm", "true")) && "airplane_mode_on".equalsIgnoreCase(name)) {
            Global.putInt(cr, Systemex.USER_SET_AIRPLANE, -1);
        }
    }

    public boolean checkPrimaryVolumeIsSD() {
        return HwEnvironment.checkPrimaryVolumeIsSD();
    }

    public void initUserEnvironmentSD(int userId) {
        HwEnvironment.initUserEnvironmentSD(userId);
    }

    public File getMediaStorageDirectory() {
        return HwEnvironment.getMediaStorageDirectory();
    }

    public File getExternalStorageDirectory() {
        return HwEnvironment.getExternalStorageDirectory();
    }

    public File getExternalStoragePublicDirectory(String type) {
        return HwEnvironment.getExternalStoragePublicDirectory(type);
    }

    public File getExternalStorageState() {
        return HwEnvironment.getExternalStorageState();
    }

    public String adjustValueForMDMPolicy(ContentResolver cr, String name, String origValue) {
        return HwSettings.adjustValueForMDMPolicy(cr, name, origValue);
    }
}
