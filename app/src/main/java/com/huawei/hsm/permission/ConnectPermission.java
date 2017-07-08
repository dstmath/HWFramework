package com.huawei.hsm.permission;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.provider.Settings.Global;
import android.util.Log;
import com.huawei.hsm.permission.minimms.PduParser;

public class ConnectPermission {
    public static final boolean DEBUG = false;
    public static final String HOTALK_CLASS = "com.hotalk.ui.chat.singleChat.SingleChatActivity";
    public static final String MMS_CLASS = "com.android.mms.ui.ComposeMessageActivity";
    public static final String MMS_PACKAGE = "com.huawei.message";
    private static final int ONE_MMS = 1;
    public static final int PERMISSION_MMS = 8192;
    private static final String SEND_MUTIL_MMS_STATUS = "true";
    public static final String TAG = null;
    public static final boolean isControl = false;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.hsm.permission.ConnectPermission.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.hsm.permission.ConnectPermission.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: com.huawei.hsm.permission.ConnectPermission.<clinit>():void");
    }

    public ConnectPermission(Context context) {
        this.mContext = context;
    }

    public static boolean isBlocked(int type, int uid, int pid) {
        if (!isControl || !StubController.checkPrecondition(uid)) {
            return DEBUG;
        }
        int remindResult = StubController.holdForGetPermissionSelection(type, uid, pid, null);
        return (remindResult == 0 || ONE_MMS == remindResult || 2 != remindResult) ? DEBUG : true;
    }

    public boolean isBlocked(byte[] pduDataStream) {
        if (!isControl) {
            return DEBUG;
        }
        if (StubController.checkPreBlock(Binder.getCallingUid(), PERMISSION_MMS)) {
            return true;
        }
        if (!StubController.checkPrecondition(Binder.getCallingUid())) {
            return DEBUG;
        }
        String desAddr = null;
        if (ONE_MMS < new PduParser(pduDataStream).getTargetCount()) {
            desAddr = SEND_MUTIL_MMS_STATUS;
        }
        int remindResult = StubController.holdForGetPermissionSelection(PERMISSION_MMS, Binder.getCallingUid(), Binder.getCallingPid(), desAddr);
        return (remindResult == 0 || ONE_MMS == remindResult || 2 != remindResult) ? DEBUG : true;
    }

    public static boolean blockStartActivity(Context context, Intent intent) {
        if (!isControl || !intentToMms(intent)) {
            return (isControl && intentToSms(intent) && StubController.checkPreBlock(Binder.getCallingUid(), 32)) ? true : DEBUG;
        } else {
            if (StubController.checkPreBlock(Binder.getCallingUid(), PERMISSION_MMS)) {
                return true;
            }
            ConnectPermission wnp = new ConnectPermission(context);
            if (isBlocked(PERMISSION_MMS, Binder.getCallingUid(), Binder.getCallingPid())) {
                return true;
            }
            if (context.getPackageManager().queryIntentActivities(intent, StubController.PERMISSION_SMSLOG_WRITE).size() <= 0) {
                intent.setComponent(new ComponentName(MMS_PACKAGE, HOTALK_CLASS));
            }
            return DEBUG;
        }
    }

    private static boolean intentToMms(Intent intent) {
        if (!"android.intent.action.SEND".equals(intent.getAction())) {
            return DEBUG;
        }
        ComponentName componentName = intent.getComponent();
        if (componentName == null) {
            return DEBUG;
        }
        return MMS_CLASS.equals(componentName.getClassName());
    }

    private static boolean intentToSms(Intent intent) {
        String scheme = null;
        String action = intent.getAction();
        if ("android.intent.action.SENDTO".equals(action) || "android.intent.action.VIEW".equals(action)) {
            if (intent.getData() != null) {
                scheme = intent.getData().getScheme();
            }
            if (scheme != null && (scheme.equalsIgnoreCase("sms") || scheme.equalsIgnoreCase("smsto"))) {
                return true;
            }
        }
        return DEBUG;
    }

    public static boolean allowOpenBt(Context cxt) {
        return (cxt == null || Global.getInt(cxt.getContentResolver(), "bluetooth_on", 0) == ONE_MMS || !isBlocked(StubController.PERMISSION_BLUETOOTH, Binder.getCallingUid(), Binder.getCallingPid())) ? true : DEBUG;
    }

    public static boolean allowOpenMobile(Context cxt) {
        return (cxt == null || Global.getInt(cxt.getContentResolver(), "mobile_data", 0) == ONE_MMS || !isBlocked(StubController.PERMISSION_MOBILEDATE, Binder.getCallingUid(), Binder.getCallingPid())) ? true : DEBUG;
    }

    public static boolean allowOpenWifi(Context cxt) {
        if (!(cxt == null || Global.getInt(cxt.getContentResolver(), "wifi_on", 0) == ONE_MMS)) {
            boolean blocked = isBlocked(StubController.PERMISSION_WIFI, Binder.getCallingUid(), Binder.getCallingPid());
            Log.i(TAG, "allowOpenWifi blocked:" + blocked);
            if (blocked) {
                return DEBUG;
            }
        }
        return true;
    }
}
