package android.common;

import android.content.ContentResolver;
import android.provider.Settings;
import java.io.File;

public class HwSettingsManagerDummy implements HwSettingsManager {
    private static HwSettingsManager mHwSettingsManager = null;

    private HwSettingsManagerDummy() {
    }

    public static HwSettingsManager getDefault() {
        if (mHwSettingsManager == null) {
            mHwSettingsManager = new HwSettingsManagerDummy();
        }
        return mHwSettingsManager;
    }

    public String getString(ContentResolver cr, String name) {
        return Settings.System.getString(cr, name);
    }

    public boolean putString(ContentResolver cr, String name, String value) {
        return Settings.System.putString(cr, name, value);
    }

    public int getInt(ContentResolver cr, String name, int def) {
        return Settings.System.getInt(cr, name, def);
    }

    public int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getInt(cr, name);
    }

    public boolean putInt(ContentResolver cr, String name, int value) {
        return Settings.System.putInt(cr, name, value);
    }

    public long getLong(ContentResolver cr, String name, long def) {
        return Settings.System.getLong(cr, name, def);
    }

    public long getLong(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getLong(cr, name);
    }

    public boolean putLong(ContentResolver cr, String name, long value) {
        return Settings.System.putLong(cr, name, value);
    }

    public float getFloat(ContentResolver cr, String name, float def) {
        return Settings.System.getFloat(cr, name, def);
    }

    public float getFloat(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getFloat(cr, name);
    }

    public boolean putFloat(ContentResolver cr, String name, float value) {
        return Settings.System.putFloat(cr, name, value);
    }

    public void setAirplaneMode(ContentResolver cr, String name) {
    }

    public boolean checkPrimaryVolumeIsSD() {
        return false;
    }

    public void initUserEnvironmentSD(int userId) {
    }

    public File getMediaStorageDirectory() {
        return null;
    }

    public File getExternalStorageDirectory() {
        return null;
    }

    public File getExternalStoragePublicDirectory(String type) {
        return null;
    }

    public File getExternalStorageState() {
        return null;
    }

    public String adjustValueForMDMPolicy(ContentResolver cr, String name, String origValue) {
        return origValue;
    }
}
