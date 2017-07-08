package com.huawei.hsm.permission;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.util.Slog;
import huawei.android.app.admin.ConstantValue;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class PermissionManager {
    private static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.PermissionManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.PermissionManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.PermissionManager.<clinit>():void");
    }

    public static boolean canStartActivity(Context context, Intent intent) {
        boolean z = true;
        if (intent == null) {
            return true;
        }
        boolean block;
        if (CallPermission.blockStartActivity(context, intent)) {
            block = true;
        } else {
            block = ConnectPermission.blockStartActivity(context, intent);
        }
        if (block) {
            z = false;
        }
        return z;
    }

    public static boolean canSendBroadcast(Context context, Intent intent) {
        if (intent == null) {
            return true;
        }
        return new SendBroadcastPermission(context).allowSendBroadcast(intent);
    }

    public static boolean allowOp(Uri uri, int action) {
        return ContentPermission.allowContentOpInner(uri, action);
    }

    public static boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return !SmsPermission.isSmsBlocked(destAddr, smsBody, sentIntent);
    }

    public static boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return !SmsPermission.isSmsBlocked(destAddr, smsBody, (List) sentIntents);
    }

    public static boolean allowOp(int type) {
        boolean z = false;
        if (ConstantValue.transaction_setBluetoothDisabled == type) {
            boolean z2;
            CameraPermission cameraPermission = new CameraPermission();
            cameraPermission.remind();
            String str = TAG;
            StringBuilder append = new StringBuilder().append("camera remind result:");
            if (cameraPermission.isCameraBlocked) {
                z2 = false;
            } else {
                z2 = true;
            }
            Slog.i(str, append.append(z2).toString());
            if (!cameraPermission.isCameraBlocked) {
                z = true;
            }
            return z;
        } else if (PduHeaders.VALUE_YES == type) {
            return new AudioRecordPermission().remindWithResult();
        } else {
            if (StubController.PERMISSION_GET_PACKAGE_LIST == type) {
                return new AppListPermission().allowOp();
            }
            if (StubController.RMD_PERMISSION_CODE == type) {
                return new ReadMotionDataPermission().allowOp();
            }
            if (StubController.RHD_PERMISSION_CODE == type) {
                return new ReadHealthDataPermission().allowOp();
            }
            return true;
        }
    }

    public static boolean allowOp(Context cxt, int type) {
        boolean z = true;
        if (StubController.PERMISSION_BLUETOOTH == type) {
            return ConnectPermission.allowOpenBt(cxt);
        }
        if (StubController.PERMISSION_MOBILEDATE == type) {
            return ConnectPermission.allowOpenMobile(cxt);
        }
        if (StubController.PERMISSION_WIFI == type) {
            return ConnectPermission.allowOpenWifi(cxt);
        }
        if (8 != type) {
            return true;
        }
        if (new LocationPermission(cxt).isLocationBlocked()) {
            z = false;
        }
        return z;
    }

    public static boolean allowOp(Context cxt, int type, boolean enable) {
        if (enable) {
            return allowOp(cxt, type);
        }
        return true;
    }

    public static Location getFakeLocation(String name) {
        return LocationPermission.getFakeLocation(name);
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return ContentPermission.getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static void setOutputFile(MediaRecorder recorder, long offset, long len) throws IllegalStateException, IOException {
        Slog.d(TAG, "set put File null");
        FileOutputStream fos = new FileOutputStream("dev/null");
        try {
            recorder._setOutputFile(fos.getFD(), offset, len);
        } finally {
            fos.close();
        }
    }
}
