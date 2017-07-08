package android.hsm;

import android.app.PendingIntent;
import android.common.HwFrameworkFactory;
import android.common.HwFrameworkFactory.IHwSystemManager;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.location.Location;
import android.media.MediaRecorder;
import android.net.ProxyInfo;
import android.net.Uri;
import android.telecom.AudioState;
import android.util.Log;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HwSystemManager {
    public static final int ACTION_PTIVATE_DELETE = 3;
    public static final int ACTION_PTIVATE_NONE = 0;
    public static final int ACTION_PTIVATE_READ = 1;
    public static final int ACTION_PTIVATE_WRITE = 2;
    public static final int CMD_ADD = 0;
    public static final int CMD_CHECK = 2;
    public static final int CMD_REMOVE = 1;
    public static final int PERMISSION_BLUETOOTH = 8388608;
    public static final int PERMISSION_CAMERA = 1024;
    public static final int PERMISSION_GET_APP_LIST = 33554432;
    public static final int PERMISSION_LOCATION = 8;
    public static final int PERMISSION_MOBILEDATE = 4194304;
    public static final int PERMISSION_RECORD = 128;
    public static final int PERMISSION_WIFI = 2097152;
    public static final int PRIVATE_FLAG_COMMON_SHOW_DIALOG = 1024;
    public static final int RHD_PERMISSION_CODE = 134217728;
    public static final int RMD_PERMISSION_CODE = 67108864;
    private static final String TAG = null;
    public static final int mPermissionEnabled = 0;
    private static HsmInterface sInstance;

    public interface HsmInterface {
        boolean allowOp(int i);

        boolean allowOp(Context context, int i);

        boolean allowOp(Context context, int i, boolean z);

        boolean allowOp(Uri uri, int i);

        boolean allowOp(String str, String str2, PendingIntent pendingIntent);

        boolean allowOp(String str, String str2, List<PendingIntent> list);

        boolean canSendBroadcast(Context context, Intent intent);

        boolean canStartActivity(Context context, Intent intent);

        Cursor getDummyCursor(ContentResolver contentResolver, Uri uri, String[] strArr, String str, String[] strArr2, String str2);

        List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> list);

        Location getFakeLocation(String str);

        List<PackageInfo> getFakePackages(List<PackageInfo> list);

        List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> list);

        void setOutputFile(MediaRecorder mediaRecorder, long j, long j2) throws IllegalStateException, IOException;

        boolean shouldInterceptAudience(String[] strArr, String str);
    }

    public static class HsmDefImpl implements HsmInterface {
        public boolean canStartActivity(Context context, Intent intent) {
            return true;
        }

        public boolean canSendBroadcast(Context context, Intent intent) {
            return true;
        }

        public boolean allowOp(Uri uri, int action) {
            return true;
        }

        public boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
            return true;
        }

        public boolean allowOp(String destAddr, String smsBody, List<PendingIntent> list) {
            return true;
        }

        public boolean allowOp(int type) {
            return true;
        }

        public boolean allowOp(Context cxt, int type) {
            return true;
        }

        public boolean allowOp(Context cxt, int type, boolean enable) {
            return true;
        }

        public Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
            return null;
        }

        public Location getFakeLocation(String name) {
            return null;
        }

        public void setOutputFile(MediaRecorder recorder, long offset, long len) throws IllegalStateException, IOException {
        }

        public boolean shouldInterceptAudience(String[] people, String pkgName) {
            return false;
        }

        public List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
            return installedList;
        }

        public List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
            return installedList;
        }

        public List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
            return originalList;
        }
    }

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: android.hsm.HwSystemManager.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: android.hsm.HwSystemManager.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: android.hsm.HwSystemManager.<clinit>():void");
    }

    private static HsmInterface getImplObject() {
        if (sInstance != null) {
            return sInstance;
        }
        HsmInterface instance = null;
        IHwSystemManager obj = HwFrameworkFactory.getHwSystemManager();
        if (obj != null) {
            instance = obj.getHsmInstance();
        }
        if (instance != null) {
            sInstance = instance;
        } else {
            Log.w(TAG, "can't get impl object from vendor, use default implemention");
            sInstance = new HsmDefImpl();
        }
        return sInstance;
    }

    public static boolean canStartActivity(Context context, Intent intent) {
        return getImplObject().canStartActivity(context, intent);
    }

    public static boolean canSendBroadcast(Context context, Intent intent) {
        return getImplObject().canSendBroadcast(context, intent);
    }

    public static boolean allowOp(Uri uri, int action) {
        return getImplObject().allowOp(uri, action);
    }

    public static boolean allowOp(int type) {
        return getImplObject().allowOp(type);
    }

    public static boolean allowOp(Context cxt, int type) {
        return getImplObject().allowOp(cxt, type);
    }

    public static boolean allowOp(Context cxt, int type, boolean enable) {
        return getImplObject().allowOp(cxt, type);
    }

    public static boolean allowOp(String destAddr, String smsBody, List<PendingIntent> sentIntents) {
        return getImplObject().allowOp(destAddr, smsBody, (List) sentIntents);
    }

    public static boolean allowOp(String destAddr, String smsBody, PendingIntent sentIntent) {
        return getImplObject().allowOp(destAddr, smsBody, sentIntent);
    }

    public static boolean allowOp(String destAddr, byte[] data, PendingIntent sentIntent) {
        try {
            return allowOp(destAddr, new String(data, "UTF-8"), sentIntent);
        } catch (UnsupportedEncodingException e) {
            return true;
        }
    }

    public static ArrayList<ContentProviderOperation> getAllowedApplyBatchOp(String authority, ArrayList<ContentProviderOperation> operations) {
        ArrayList<ContentProviderOperation> allowedOperations = new ArrayList();
        Map<String, Map<Integer, Boolean>> authmap = new HashMap();
        for (ContentProviderOperation operation : operations) {
            String authStr = operation.getUri().getAuthority();
            int type = operation.getType();
            int action = CMD_ADD;
            switch (type) {
                case CMD_REMOVE /*1*/:
                case CMD_CHECK /*2*/:
                    action = CMD_CHECK;
                    break;
                case ACTION_PTIVATE_DELETE /*3*/:
                    action = ACTION_PTIVATE_DELETE;
                    break;
                case AudioState.ROUTE_WIRED_HEADSET /*4*/:
                    action = CMD_REMOVE;
                    break;
            }
            Map<Integer, Boolean> actionMap;
            if (authmap.containsKey(authStr)) {
                actionMap = (Map) authmap.get(authStr);
                if (actionMap.containsKey(Integer.valueOf(action))) {
                    if (((Boolean) actionMap.get(Integer.valueOf(action))).booleanValue()) {
                        allowedOperations.add(operation);
                    }
                } else if (allowOp(operation.getUri(), action)) {
                    actionMap.put(Integer.valueOf(action), Boolean.valueOf(true));
                    allowedOperations.add(operation);
                } else {
                    actionMap.put(Integer.valueOf(action), Boolean.valueOf(false));
                }
            } else {
                actionMap = new HashMap();
                if (allowOp(operation.getUri(), action)) {
                    actionMap.put(Integer.valueOf(action), Boolean.valueOf(true));
                    allowedOperations.add(operation);
                } else {
                    actionMap.put(Integer.valueOf(action), Boolean.valueOf(false));
                }
                authmap.put(authStr, actionMap);
            }
        }
        return allowedOperations;
    }

    public static Cursor getDummyCursor(ContentResolver resolver, Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        return getImplObject().getDummyCursor(resolver, uri, projection, selection, selectionArgs, sortOrder);
    }

    public static Location getFakeLocation(String name) {
        return getImplObject().getFakeLocation(name);
    }

    public static boolean checkWindowType(int flag) {
        return (flag & PRIVATE_FLAG_COMMON_SHOW_DIALOG) != 0;
    }

    public static void setOutputFile(MediaRecorder recorder, long offset, long len) throws IllegalStateException, IOException {
        getImplObject().setOutputFile(recorder, offset, len);
    }

    public static boolean shouldInterceptAudience(String[] people) {
        return getImplObject().shouldInterceptAudience(people, ProxyInfo.LOCAL_EXCL_LIST);
    }

    public static boolean shouldInterceptAudience(String[] people, String pkgName) {
        return getImplObject().shouldInterceptAudience(people, pkgName);
    }

    public static List<PackageInfo> getFakePackages(List<PackageInfo> installedList) {
        return getImplObject().getFakePackages(installedList);
    }

    public static List<ApplicationInfo> getFakeApplications(List<ApplicationInfo> installedList) {
        return getImplObject().getFakeApplications(installedList);
    }

    public static List<ResolveInfo> getFakeResolveInfoList(List<ResolveInfo> originalList) {
        return getImplObject().getFakeResolveInfoList(originalList);
    }
}
