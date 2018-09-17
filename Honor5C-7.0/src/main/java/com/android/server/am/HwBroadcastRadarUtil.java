package com.android.server.am;

import android.content.Intent;
import android.os.Bundle;
import android.util.Flog;
import android.util.LogException;
import java.util.HashMap;
import java.util.HashSet;

public class HwBroadcastRadarUtil {
    private static final int BUG_TYPE_BROADCAST = 104;
    private static final String CATEGORY = "framework";
    public static final String KEY_ACTION = "action";
    public static final String KEY_ACTION_COUNT = "actionCount";
    public static final String KEY_BROADCAST_INTENT = "intent";
    public static final String KEY_MMS_BROADCAST_FLAG = "mmsFlag";
    public static final String KEY_PACKAGE = "package";
    public static final String KEY_RECEIVER = "receiver";
    public static final String KEY_RECEIVE_TIME = "receiveTime";
    public static final String KEY_VERSION_CODE = "versionCode";
    public static final String KEY_VERSION_NAME = "versionName";
    private static final int LEVEL_A = 65;
    private static final int LEVEL_B = 66;
    private static final int LEVEL_C = 67;
    private static final int LEVEL_D = 68;
    private static final int MAX_BODY_SIZE = 512;
    public static final int MAX_BROADCASTQUEUE_LENGTH = 150;
    private static final int MAX_HEAD_SIZE = 256;
    public static final int SCENE_DEF_BROADCAST_OVERLENGTH = 2801;
    public static final int SCENE_DEF_RECEIVER_TIMEOUT = 2803;
    public static final long SYSTEM_BOOT_COMPLETED_TIME = 1800000;
    private static final String TAG = "BroadcastRadar";
    private static LogException mLogException;
    private HashMap<Integer, HashMap<String, HashSet<String>>> mUploadedPackages;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.server.am.HwBroadcastRadarUtil.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.android.server.am.HwBroadcastRadarUtil.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 7 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 8 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.server.am.HwBroadcastRadarUtil.<clinit>():void");
    }

    public HwBroadcastRadarUtil() {
        this.mUploadedPackages = new HashMap();
    }

    public void handleBroadcastQueueOverlength(Bundle bundle) {
        if (bundle != null) {
            String packageName = bundle.getString(KEY_PACKAGE, "");
            String actionName = bundle.getString(KEY_ACTION, "");
            int actionCount = bundle.getInt(KEY_ACTION_COUNT, 0);
            boolean isContainsMms = bundle.getBoolean(KEY_MMS_BROADCAST_FLAG, false);
            String receiverName = bundle.getString(KEY_RECEIVER, "unknown");
            String versionName = bundle.getString(KEY_VERSION_NAME, "unknown");
            StringBuilder body = new StringBuilder(MAX_BODY_SIZE);
            body.append("Package:").append(packageName);
            body.append(" send Broadcast[").append(actionName).append("] for ");
            body.append(actionCount).append(" times, current queue ");
            body.append(isContainsMms ? "contains" : "contains not").append(" mms broadcast");
            body.append(" and curReceiver is ").append(receiverName);
            uploadExceptionLog(BUG_TYPE_BROADCAST, SCENE_DEF_BROADCAST_OVERLENGTH, packageName, versionName, body.toString(), bundle);
        }
    }

    public void handleReceiverTimeOut(Bundle bundle) {
        if (bundle != null) {
            String packageName = bundle.getString(KEY_PACKAGE, "");
            String receiverName = bundle.getString(KEY_RECEIVER, "unknown");
            String versionName = bundle.getString(KEY_VERSION_NAME, "unknown");
            float receiverTime = bundle.getFloat(KEY_RECEIVE_TIME, -1.0f);
            Intent objIntent = bundle.getParcelable(KEY_BROADCAST_INTENT);
            Intent intent = objIntent != null ? objIntent : new Intent();
            StringBuilder body = new StringBuilder(MAX_BODY_SIZE);
            body.append("Receiver[").append(receiverName);
            body.append("] from ").append(packageName);
            body.append(" receiving ").append(intent);
            body.append(" tooks ").append(receiverTime).append("s.");
            uploadExceptionLog(BUG_TYPE_BROADCAST, SCENE_DEF_RECEIVER_TIMEOUT, packageName, versionName, body.toString(), bundle);
        }
    }

    private boolean isPackageUploaded(int sceneDef, String pkg, String action) {
        HashMap<String, HashSet<String>> uploadedPackages = (HashMap) this.mUploadedPackages.get(Integer.valueOf(sceneDef));
        if (uploadedPackages == null) {
            return false;
        }
        HashSet<String> pkgActions = (HashSet) uploadedPackages.get(pkg);
        if (pkgActions == null || !pkgActions.contains(action)) {
            return false;
        }
        return true;
    }

    private void uploadExceptionLog(int bugType, int sceneDef, String pkg, String apkVersion, String body, Bundle extras) {
        try {
            if (!isPackageUploaded(sceneDef, pkg, extras.getString(KEY_ACTION, null))) {
                StringBuilder header = new StringBuilder(MAX_HEAD_SIZE);
                header.append("Package:").append(pkg).append("\n");
                header.append("APK version:").append(apkVersion).append("\n");
                header.append("Bug type:").append(bugType).append("\n");
                header.append("Scene def:").append(sceneDef);
                switch (sceneDef) {
                    case SCENE_DEF_BROADCAST_OVERLENGTH /*2801*/:
                        Flog.i(BUG_TYPE_BROADCAST, "Trigger order broadcast queue overlength radar upload.");
                        break;
                    case SCENE_DEF_RECEIVER_TIMEOUT /*2803*/:
                        Flog.i(BUG_TYPE_BROADCAST, "Trigger receiver timeout radar upload. Receiver[" + extras.getString(KEY_RECEIVER, "unknown") + "] from " + pkg + " receiving " + extras.getParcelable(KEY_BROADCAST_INTENT) + " tooks " + extras.getFloat(KEY_RECEIVE_TIME, -1.0f) + "s.");
                        break;
                    default:
                        Flog.i(BUG_TYPE_BROADCAST, "Trigger broadcast radar upload for scene " + sceneDef + ".");
                        break;
                }
                mLogException.msg(CATEGORY, LEVEL_A, header.toString(), body);
                Flog.i(BUG_TYPE_BROADCAST, "Radar upload for Package: " + pkg + ", BugType: " + bugType + ", Scene: " + sceneDef + ".");
                HashMap<String, HashSet<String>> uploadedPackages = (HashMap) this.mUploadedPackages.get(Integer.valueOf(sceneDef));
                if (uploadedPackages == null) {
                    uploadedPackages = new HashMap();
                    this.mUploadedPackages.put(Integer.valueOf(sceneDef), uploadedPackages);
                }
                HashSet<String> pkgActions = (HashSet) uploadedPackages.get(pkg);
                if (pkgActions == null) {
                    pkgActions = new HashSet();
                    uploadedPackages.put(pkg, pkgActions);
                }
                pkgActions.add(extras.getString(KEY_ACTION));
            }
        } catch (RuntimeException ex) {
            Flog.e(BUG_TYPE_BROADCAST, "radar upload failed.", ex);
        }
    }
}
