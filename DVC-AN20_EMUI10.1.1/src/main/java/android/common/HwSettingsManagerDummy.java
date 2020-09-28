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

    @Override // android.common.HwSettingsManager
    public String getString(ContentResolver cr, String name) {
        return Settings.System.getString(cr, name);
    }

    @Override // android.common.HwSettingsManager
    public boolean putString(ContentResolver cr, String name, String value) {
        return Settings.System.putString(cr, name, value);
    }

    @Override // android.common.HwSettingsManager
    public int getInt(ContentResolver cr, String name, int def) {
        return Settings.System.getInt(cr, name, def);
    }

    @Override // android.common.HwSettingsManager
    public int getInt(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getInt(cr, name);
    }

    @Override // android.common.HwSettingsManager
    public boolean putInt(ContentResolver cr, String name, int value) {
        return Settings.System.putInt(cr, name, value);
    }

    @Override // android.common.HwSettingsManager
    public long getLong(ContentResolver cr, String name, long def) {
        return Settings.System.getLong(cr, name, def);
    }

    @Override // android.common.HwSettingsManager
    public long getLong(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getLong(cr, name);
    }

    @Override // android.common.HwSettingsManager
    public boolean putLong(ContentResolver cr, String name, long value) {
        return Settings.System.putLong(cr, name, value);
    }

    @Override // android.common.HwSettingsManager
    public float getFloat(ContentResolver cr, String name, float def) {
        return Settings.System.getFloat(cr, name, def);
    }

    @Override // android.common.HwSettingsManager
    public float getFloat(ContentResolver cr, String name) throws Settings.SettingNotFoundException {
        return Settings.System.getFloat(cr, name);
    }

    @Override // android.common.HwSettingsManager
    public boolean putFloat(ContentResolver cr, String name, float value) {
        return Settings.System.putFloat(cr, name, value);
    }

    @Override // android.common.HwSettingsManager
    public void setAirplaneMode(ContentResolver cr, String name) {
    }

    @Override // android.common.HwSettingsManager
    public boolean checkPrimaryVolumeIsSD() {
        return false;
    }

    @Override // android.common.HwSettingsManager
    public void initUserEnvironmentSD(int userId) {
    }

    @Override // android.common.HwSettingsManager
    public File getMediaStorageDirectory() {
        return null;
    }

    @Override // android.common.HwSettingsManager
    public File getExternalStorageDirectory() {
        return null;
    }

    @Override // android.common.HwSettingsManager
    public File getExternalStoragePublicDirectory(String type) {
        return null;
    }

    @Override // android.common.HwSettingsManager
    public File getExternalStorageState() {
        return null;
    }

    @Override // android.common.HwSettingsManager
    public String adjustValueForMDMPolicy(ContentResolver cr, String name, String origValue) {
        return origValue;
    }
}
