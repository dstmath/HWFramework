package com.android.internal.content;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.storage.IMountService.Stub;
import android.os.storage.StorageManager;
import android.os.storage.VolumeInfo;
import android.util.Log;
import java.io.File;

public class HwCustPackageHelperImpl extends HwCustPackageHelper {
    private static boolean HWFLOW = false;
    private static final boolean HWLOGW_E = true;
    static final String TAG = "HwCustPackageHelperImpl";
    private static final String TAG_FLOW = "HwCustPackageHelperImpl_FLOW";
    private boolean mSdInstallEnabled;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.internal.content.HwCustPackageHelperImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.internal.content.HwCustPackageHelperImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.internal.content.HwCustPackageHelperImpl.<clinit>():void");
    }

    public HwCustPackageHelperImpl() {
        this.mSdInstallEnabled = SystemProperties.getBoolean("ro.config.hw_sdInstall_enable", false);
        if (HWFLOW) {
            Log.i(TAG_FLOW, TAG);
        }
    }

    public boolean isSdInstallEnabled() {
        return this.mSdInstallEnabled;
    }

    public boolean fitsOnExternalEx(Context context, long sizeBytes) {
        if (isSDCardMounted()) {
            File sdFile = getMountedSDCardPath();
            if (sdFile == null) {
                Log.e(TAG_FLOW, " fitsOnExternal sdFile null ");
                return false;
            }
            long freeSize = ((StorageManager) context.getSystemService(StorageManager.class)).getStorageBytesUntilLow(sdFile);
            if (sizeBytes <= 0 || sizeBytes >= freeSize) {
                return false;
            }
            return HWLOGW_E;
        }
        Log.e(TAG_FLOW, " fitsOnExternal sd unMounted ");
        return false;
    }

    private static boolean isFirstSdVolume(VolumeInfo vol) {
        String CurrentDiskID = vol.getDisk().getId();
        String CurrentVolumeID = vol.getId();
        String[] CurrentDiskIDSplitstr = CurrentDiskID.split(":");
        String[] CurrentVolumeIDSplitstr = CurrentVolumeID.split(":");
        if (CurrentDiskIDSplitstr.length != 3 || CurrentVolumeIDSplitstr.length != 3) {
            return false;
        }
        try {
            int DiskID = Integer.valueOf(CurrentDiskIDSplitstr[2]).intValue();
            int VolumeID = Integer.valueOf(CurrentVolumeIDSplitstr[2]).intValue();
            if (VolumeID == DiskID + 1 || VolumeID == DiskID) {
                return HWLOGW_E;
            }
            return false;
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return false;
        }
    }

    private static boolean isSDCardMounted() {
        try {
            for (VolumeInfo vol : Stub.asInterface(ServiceManager.getService("mount")).getVolumes(0)) {
                if (vol.getDisk() != null && vol.isMountedWritable() && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                    return HWLOGW_E;
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return false;
    }

    private static File getMountedSDCardPath() {
        try {
            for (VolumeInfo vol : Stub.asInterface(ServiceManager.getService("mount")).getVolumes(0)) {
                if (vol.getDisk() != null && vol.isMountedWritable() && vol.getDisk().isSd() && isFirstSdVolume(vol)) {
                    return vol.getPath();
                }
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
