package com.huawei.hsm;

import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.hsm.HwSystemManager.HsmInterface;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;
import com.huawei.hsm.permission.PermissionManager;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HsmInterfaceImpl implements HsmInterface {
    private static final int RESTRICTED_NUM = 2;
    private static final String TAG = null;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.HsmInterfaceImpl.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.HsmInterfaceImpl.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.HsmInterfaceImpl.<clinit>():void");
    }

    public boolean canStartActivity(Context context, Intent intent) {
        return PermissionManager.canStartActivity(context, intent);
    }

    public boolean canSendBroadcast(Context context, Intent intent) {
        return PermissionManager.canSendBroadcast(context, intent);
    }

    public boolean allowOp(Uri uri, int action) {
        return PermissionManager.allowOp(uri, action);
    }

    public boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return PermissionManager.allowOp(destAddr, smsBody, sentIntent);
    }

    public boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return PermissionManager.allowOp(destAddr, smsBody, (List) sentIntents);
    }

    public boolean allowOp(int type) {
        return PermissionManager.allowOp(type);
    }

    public boolean allowOp(Context cxt, int type) {
        return PermissionManager.allowOp(cxt, type);
    }

    public boolean allowOp(Context cxt, int type, boolean enable) {
        return PermissionManager.allowOp(cxt, type, enable);
    }

    public Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return PermissionManager.getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public Location getFakeLocation(String name) {
        return PermissionManager.getFakeLocation(name);
    }

    public void setOutputFile(MediaRecorder recorder, long offset, long len) throws IllegalStateException, IOException {
        PermissionManager.setOutputFile(recorder, offset, len);
    }

    public boolean shouldInterceptAudience(String[] people, String pkgName) {
        return false;
    }

    public List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
        List<PackageInfo> fakeList = new ArrayList();
        int num = 0;
        for (PackageInfo packageInfo : installedList) {
            if (isSameUid(packageInfo.applicationInfo)) {
                fakeList.add(packageInfo);
            } else if (num < RESTRICTED_NUM) {
                num++;
                fakeList.add(packageInfo);
            }
        }
        Log.d(TAG, "List " + getListStr(fakeList));
        return fakeList;
    }

    public List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
        List<ApplicationInfo> fakeList = new ArrayList();
        int num = 0;
        for (ApplicationInfo applicationInfo : installedList) {
            if (isSameUid(applicationInfo)) {
                fakeList.add(applicationInfo);
            } else if (num < RESTRICTED_NUM) {
                num++;
                fakeList.add(applicationInfo);
            }
        }
        Log.d(TAG, "List " + getListStr(fakeList));
        return fakeList;
    }

    public List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
        if (originalList == null) {
            return originalList;
        }
        List<ResolveInfo> fakeList = new ArrayList();
        int num = 0;
        for (ResolveInfo resolveInfo : originalList) {
            ActivityInfo activityInfo = resolveInfo.activityInfo;
            if (activityInfo != null) {
                if (isSameUid(activityInfo.applicationInfo)) {
                    fakeList.add(resolveInfo);
                } else if (num < RESTRICTED_NUM) {
                    fakeList.add(resolveInfo);
                    num++;
                }
            }
        }
        Log.d(TAG, "List " + getListStr(fakeList));
        return fakeList;
    }

    private boolean isSameUid(ApplicationInfo applicationInfo) {
        boolean z = false;
        if (applicationInfo == null) {
            return false;
        }
        if (applicationInfo.uid == Binder.getCallingUid()) {
            z = true;
        }
        return z;
    }

    private <T> String getListStr(List<T> list) {
        if (list == null) {
            return "NULL";
        }
        return Arrays.toString(list.toArray());
    }
}
