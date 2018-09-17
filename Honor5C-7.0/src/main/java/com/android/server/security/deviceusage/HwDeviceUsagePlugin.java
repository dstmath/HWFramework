package com.android.server.security.deviceusage;

import android.content.Context;
import android.os.IBinder;
import android.util.Slog;
import com.android.server.security.core.IHwSecurityPlugin;
import com.android.server.security.core.IHwSecurityPlugin.Creator;
import huawei.android.security.IHwDeviceUsagePlugin.Stub;

public class HwDeviceUsagePlugin extends Stub implements IHwSecurityPlugin {
    public static final boolean CHINA_RELEASE_VERSION = false;
    public static final Creator CREATOR = null;
    private static final boolean HW_DEBUG = false;
    public static final boolean IS_PHONE = false;
    private static final String MANAGE_USE_SECURITY = "com.huawei.permission.MANAGE_USE_SECURITY";
    private static final String TAG = "HwDeviceUsagePlugin";
    private Context mContext;
    private HwDeviceUsageCollection mHwDeviceUsageCollection;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.security.deviceusage.HwDeviceUsagePlugin.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.security.deviceusage.HwDeviceUsagePlugin.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 5 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 6 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.security.deviceusage.HwDeviceUsagePlugin.<clinit>():void");
    }

    public HwDeviceUsagePlugin(Context context) {
        this.mContext = context;
    }

    public IBinder asBinder() {
        return this;
    }

    public void onStart() {
        if (HW_DEBUG) {
            Slog.d(TAG, "HwDeviceUsagePlugin is Start");
        }
        this.mHwDeviceUsageCollection = new HwDeviceUsageCollection(this.mContext);
        boolean openFlag = (CHINA_RELEASE_VERSION && IS_PHONE) ? this.mHwDeviceUsageCollection.getOpenFlag() : IS_PHONE;
        if (openFlag) {
            this.mHwDeviceUsageCollection.onStart();
        }
    }

    public void onStop() {
    }

    public long getScreenOnTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getScreenOnTime();
    }

    public long getChargeTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getChargeTime();
    }

    public long getTalkTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getTalkTime();
    }

    public long getFristUseTime() {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection == null) {
            return -1;
        }
        return this.mHwDeviceUsageCollection.getFristUseTime();
    }

    public void setOpenFlag(int flag) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setOpenFlag(flag);
        }
    }

    public void setScreenOnTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setScreenOnTime(time);
        }
    }

    public void setChargeTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setChargeTime(time);
        }
    }

    public void setTalkTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setTalkTime(time);
        }
    }

    public void setFristUseTime(long time) {
        checkPermission(MANAGE_USE_SECURITY);
        if (this.mHwDeviceUsageCollection != null) {
            this.mHwDeviceUsageCollection.setFristUseTime(time);
        }
    }

    private void checkPermission(String permission) {
        this.mContext.enforceCallingOrSelfPermission(permission, "Must have " + permission + " permission.");
    }
}
