package android.common;

import android.content.ContentResolver;
import android.provider.Settings.SettingNotFoundException;
import java.io.File;

public interface HwSettingsManager {
    String adjustValueForMDMPolicy(ContentResolver contentResolver, String str, String str2);

    boolean checkPrimaryVolumeIsSD();

    File getExternalStorageDirectory();

    File getExternalStoragePublicDirectory(String str);

    File getExternalStorageState();

    float getFloat(ContentResolver contentResolver, String str) throws SettingNotFoundException;

    float getFloat(ContentResolver contentResolver, String str, float f);

    int getInt(ContentResolver contentResolver, String str) throws SettingNotFoundException;

    int getInt(ContentResolver contentResolver, String str, int i);

    long getLong(ContentResolver contentResolver, String str) throws SettingNotFoundException;

    long getLong(ContentResolver contentResolver, String str, long j);

    File getMediaStorageDirectory();

    String getString(ContentResolver contentResolver, String str);

    void initUserEnvironmentSD(int i);

    boolean putFloat(ContentResolver contentResolver, String str, float f);

    boolean putInt(ContentResolver contentResolver, String str, int i);

    boolean putLong(ContentResolver contentResolver, String str, long j);

    boolean putString(ContentResolver contentResolver, String str, String str2);

    void setAirplaneMode(ContentResolver contentResolver, String str);
}
