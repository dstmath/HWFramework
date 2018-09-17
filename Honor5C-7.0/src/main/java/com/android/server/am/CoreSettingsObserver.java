package com.android.server.am;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Bundle;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.provider.Settings.System;
import java.util.Map;
import java.util.Map.Entry;

final class CoreSettingsObserver extends ContentObserver {
    private static final String LOG_TAG = null;
    private static final Map<String, Class<?>> sGlobalSettingToTypeMap = null;
    private static final Map<String, Class<?>> sSecureSettingToTypeMap = null;
    private static final Map<String, Class<?>> sSystemSettingToTypeMap = null;
    private final ActivityManagerService mActivityManagerService;
    private final Bundle mCoreSettings;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.CoreSettingsObserver.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.CoreSettingsObserver.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.CoreSettingsObserver.<clinit>():void");
    }

    public CoreSettingsObserver(ActivityManagerService activityManagerService) {
        super(activityManagerService.mHandler);
        this.mCoreSettings = new Bundle();
        this.mActivityManagerService = activityManagerService;
        beginObserveCoreSettings();
        sendCoreSettings();
    }

    public Bundle getCoreSettingsLocked() {
        return (Bundle) this.mCoreSettings.clone();
    }

    public void onChange(boolean selfChange) {
        synchronized (this.mActivityManagerService) {
            try {
                ActivityManagerService.boostPriorityForLockedSection();
                sendCoreSettings();
            } finally {
                ActivityManagerService.resetPriorityAfterLockedSection();
            }
        }
    }

    private void sendCoreSettings() {
        populateSettings(this.mCoreSettings, sSecureSettingToTypeMap);
        populateSettings(this.mCoreSettings, sSystemSettingToTypeMap);
        populateSettings(this.mCoreSettings, sGlobalSettingToTypeMap);
        this.mActivityManagerService.onCoreSettingsChange(this.mCoreSettings);
    }

    private void beginObserveCoreSettings() {
        for (String setting : sSecureSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(setting), false, this);
        }
        for (String setting2 : sSystemSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(System.getUriFor(setting2), false, this);
        }
        for (String setting22 : sGlobalSettingToTypeMap.keySet()) {
            this.mActivityManagerService.mContext.getContentResolver().registerContentObserver(Global.getUriFor(setting22), false, this);
        }
    }

    private void populateSettings(Bundle snapshot, Map<String, Class<?>> map) {
        Context context = this.mActivityManagerService.mContext;
        for (Entry<String, Class<?>> entry : map.entrySet()) {
            String setting = (String) entry.getKey();
            Class<?> type = (Class) entry.getValue();
            if (type == String.class) {
                String value;
                if (map == sSecureSettingToTypeMap) {
                    value = Secure.getString(context.getContentResolver(), setting);
                } else if (map == sSystemSettingToTypeMap) {
                    value = System.getString(context.getContentResolver(), setting);
                } else {
                    value = Global.getString(context.getContentResolver(), setting);
                }
                snapshot.putString(setting, value);
            } else if (type == Integer.TYPE) {
                int value2;
                if (map == sSecureSettingToTypeMap) {
                    value2 = Secure.getInt(context.getContentResolver(), setting, 0);
                } else if (map == sSystemSettingToTypeMap) {
                    value2 = System.getInt(context.getContentResolver(), setting, 0);
                } else {
                    value2 = Global.getInt(context.getContentResolver(), setting, 0);
                }
                snapshot.putInt(setting, value2);
            } else if (type == Float.TYPE) {
                float value3;
                if (map == sSecureSettingToTypeMap) {
                    value3 = Secure.getFloat(context.getContentResolver(), setting, 0.0f);
                } else if (map == sSystemSettingToTypeMap) {
                    value3 = System.getFloat(context.getContentResolver(), setting, 0.0f);
                } else {
                    value3 = Global.getFloat(context.getContentResolver(), setting, 0.0f);
                }
                snapshot.putFloat(setting, value3);
            } else if (type == Long.TYPE) {
                long value4;
                if (map == sSecureSettingToTypeMap) {
                    value4 = Secure.getLong(context.getContentResolver(), setting, 0);
                } else if (map == sSystemSettingToTypeMap) {
                    value4 = System.getLong(context.getContentResolver(), setting, 0);
                } else {
                    value4 = Global.getLong(context.getContentResolver(), setting, 0);
                }
                snapshot.putLong(setting, value4);
            }
        }
    }
}
