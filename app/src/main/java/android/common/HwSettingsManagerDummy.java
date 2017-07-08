package android.common;

import android.content.ContentResolver;
import android.provider.Settings.SettingNotFoundException;
import android.provider.Settings.System;
import java.io.File;

public class HwSettingsManagerDummy implements HwSettingsManager {
    private static HwSettingsManager mHwSettingsManager;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.common.HwSettingsManagerDummy.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.common.HwSettingsManagerDummy.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: android.common.HwSettingsManagerDummy.<clinit>():void");
    }

    private HwSettingsManagerDummy() {
    }

    public static HwSettingsManager getDefault() {
        if (mHwSettingsManager == null) {
            mHwSettingsManager = new HwSettingsManagerDummy();
        }
        return mHwSettingsManager;
    }

    public String getString(ContentResolver cr, String name) {
        return System.getString(cr, name);
    }

    public boolean putString(ContentResolver cr, String name, String value) {
        return System.putString(cr, name, value);
    }

    public int getInt(ContentResolver cr, String name, int def) {
        return System.getInt(cr, name, def);
    }

    public int getInt(ContentResolver cr, String name) throws SettingNotFoundException {
        return System.getInt(cr, name);
    }

    public boolean putInt(ContentResolver cr, String name, int value) {
        return System.putInt(cr, name, value);
    }

    public long getLong(ContentResolver cr, String name, long def) {
        return System.getLong(cr, name, def);
    }

    public long getLong(ContentResolver cr, String name) throws SettingNotFoundException {
        return System.getLong(cr, name);
    }

    public boolean putLong(ContentResolver cr, String name, long value) {
        return System.putLong(cr, name, value);
    }

    public float getFloat(ContentResolver cr, String name, float def) {
        return System.getFloat(cr, name, def);
    }

    public float getFloat(ContentResolver cr, String name) throws SettingNotFoundException {
        return System.getFloat(cr, name);
    }

    public boolean putFloat(ContentResolver cr, String name, float value) {
        return System.putFloat(cr, name, value);
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

    public File handleExternalStorageDirectoryForClone(File file) {
        return file;
    }

    public File handleDateDirectoryForClone(File file, int euid) {
        return file;
    }
}
