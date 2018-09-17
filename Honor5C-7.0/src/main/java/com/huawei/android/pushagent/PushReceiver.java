package com.huawei.android.pushagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.pushagent.a.a.d;
import com.huawei.android.pushagent.a.a.e;
import tmsdk.common.module.urlcheck.UrlCheckType;
import tmsdk.fg.module.deepclean.RubbishType;
import tmsdk.fg.module.spacemanager.FileInfo;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles.CLEANTYPE;
import tmsdk.fg.module.urlcheck.IUrlMonitorCallback;

/* compiled from: Unknown */
public abstract class PushReceiver extends BroadcastReceiver {

    /* compiled from: Unknown */
    /* renamed from: com.huawei.android.pushagent.PushReceiver.1 */
    static /* synthetic */ class AnonymousClass1 {
        static final /* synthetic */ int[] a = null;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.PushReceiver.1.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.PushReceiver.1.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 00e9
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.PushReceiver.1.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    public static class ACTION {
        public static final String ACTION_CLIENT_DEREGISTER = "com.huawei.android.push.intent.DEREGISTER";
        public static final String ACTION_PUSH_MESSAGE = "com.huawei.android.push.intent.RECEIVE";

        public ACTION() {
        }
    }

    /* compiled from: Unknown */
    public static class BOUND_KEY {
        public static final String deviceTokenKey = "deviceToken";
        public static final String pushMsgKey = "pushMsg";
        public static final String pushNotifyId = "pushNotifyId";
        public static final String pushStateKey = "pushState";
        public static final String receiveTypeKey = "receiveType";

        public BOUND_KEY() {
        }
    }

    /* compiled from: Unknown */
    class EventThread extends Thread {
        Context a;
        Bundle b;
        final /* synthetic */ PushReceiver c;

        public EventThread(PushReceiver pushReceiver, Context context, Bundle bundle) {
            this.c = pushReceiver;
            super("EventRunable");
            this.a = context;
            this.b = bundle;
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                if (this.b != null) {
                    int i = this.b.getInt(BOUND_KEY.receiveTypeKey);
                    if (i >= 0 && i < ReceiveType.values().length) {
                        switch (AnonymousClass1.a[ReceiveType.values()[i].ordinal()]) {
                            case IUrlMonitorCallback.DES_ID_HOBBYHORSE /*1*/:
                                this.c.onToken(this.a, this.b.getString(BOUND_KEY.deviceTokenKey), this.b);
                                break;
                            case CLEANTYPE.CLEANTYPE_CARE /*2*/:
                                this.c.onPushMsg(this.a, this.b.getByteArray(BOUND_KEY.pushMsgKey), this.b.getString(BOUND_KEY.deviceTokenKey));
                                break;
                            case FileInfo.TYPE_BIGFILE /*3*/:
                                this.c.onPushState(this.a, this.b.getBoolean(BOUND_KEY.pushStateKey));
                                break;
                            case RubbishType.SCAN_FLAG_GENERAL_CACHE /*4*/:
                                this.c.onNotifyClickMsg(this.a, this.b.getString(BOUND_KEY.pushMsgKey));
                                break;
                            case UrlCheckType.STEAL_ACCOUNT /*5*/:
                                this.c.onNotifyBtnClick(this.a, this.b.getInt(BOUND_KEY.pushNotifyId), this.b.getString(BOUND_KEY.pushMsgKey), new Bundle());
                                break;
                            case UrlCheckType.TIPS_CHEAT /*6*/:
                                this.c.onPluginRsp(this.a, this.b.getInt(KEY_TYPE.PLUGINREPORTTYPE, -1), this.b.getBoolean(KEY_TYPE.PLUGINREPORTRESULT, false), this.b.getBundle(KEY_TYPE.PLUGINREPORTEXTRA));
                                break;
                        }
                        return;
                    }
                    Log.e("PushLogLightSC2606", "invalid receiverType:" + i);
                }
            } catch (Throwable e) {
                Log.e("PushLogLightSC2606", "call EventThread(ReceiveType cause:" + e.toString(), e);
            }
        }
    }

    /* compiled from: Unknown */
    public static class KEY_TYPE {
        public static final String PKGNAME = "pkg_name";
        public static final String PLUGINREPORTEXTRA = "reportExtra";
        public static final String PLUGINREPORTRESULT = "isReportSuccess";
        public static final String PLUGINREPORTTYPE = "reportType";
        public static final String PUSHSTATE = "push_state";
        public static final String PUSH_BROADCAST_MESSAGE = "msg_data";
        public static final String PUSH_KEY_CLICK = "click";
        public static final String PUSH_KEY_CLICK_BTN = "clickBtn";
        public static final String PUSH_KEY_DEVICE_TOKEN = "device_token";
        public static final String PUSH_KEY_NOTIFY_ID = "notifyId";

        public KEY_TYPE() {
        }
    }

    /* compiled from: Unknown */
    enum ReceiveType {
        ;

        static {
            /* JADX: method processing error */
/*
            Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.huawei.android.pushagent.PushReceiver.ReceiveType.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:263)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: com.huawei.android.pushagent.PushReceiver.ReceiveType.<clinit>():void
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:46)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:98)
	... 6 more
Caused by: java.lang.IllegalArgumentException: bogus opcode: 0073
	at com.android.dx.io.OpcodeInfo.get(OpcodeInfo.java:1197)
	at com.android.dx.io.OpcodeInfo.getFormat(OpcodeInfo.java:1212)
	at com.android.dx.io.instructions.DecodedInstruction.decode(DecodedInstruction.java:72)
	at jadx.core.dex.instructions.InsnDecoder.decodeInsns(InsnDecoder.java:43)
	... 7 more
*/
            /*
            // Can't load method instructions.
            */
            throw new UnsupportedOperationException("Method not decompiled: com.huawei.android.pushagent.PushReceiver.ReceiveType.<clinit>():void");
        }
    }

    /* compiled from: Unknown */
    public static class SERVER {
        public static final String DEVICETOKEN = "device_token";

        public SERVER() {
        }
    }

    public PushReceiver() {
    }

    private void a(Context context, Intent intent) {
        boolean a = new e(context, "push_switch").a("notify_msg_enable");
        Log.d("PushLogLightSC2606", "closePush_Notify:" + a);
        if (!a) {
            try {
                Log.i("PushLogLightSC2606", "run push selfshow");
                Class cls = Class.forName("com.huawei.android.pushselfshow.SelfShowReceiver");
                Object newInstance = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
                cls.getDeclaredMethod("onReceive", new Class[]{Context.class, Intent.class}).invoke(newInstance, new Object[]{context, intent});
            } catch (Throwable e) {
                Log.e("PushLogLightSC2606", "SelfShowReceiver class not found:" + e.getMessage(), e);
            } catch (Throwable e2) {
                Log.e("PushLogLightSC2606", "onReceive method not found:" + e2.getMessage(), e2);
            } catch (Throwable e22) {
                Log.e("PushLogLightSC2606", "invokeSelfShow error:" + e22.getMessage(), e22);
            }
        }
    }

    public static final void enableReceiveNormalMsg(Context context, boolean z) {
        boolean z2 = false;
        if (context != null) {
            e eVar = new e(context, "push_switch");
            String str = "normal_msg_enable";
            if (!z) {
                z2 = true;
            }
            eVar.a(str, z2);
            return;
        }
        Log.d("PushLogLightSC2606", "context is null");
    }

    public static final void enableReceiveNotifyMsg(Context context, boolean z) {
        boolean z2 = false;
        if (context != null) {
            e eVar = new e(context, "push_switch");
            String str = "notify_msg_enable";
            if (!z) {
                z2 = true;
            }
            eVar.a(str, z2);
            return;
        }
        Log.d("PushLogLightSC2606", "context is null");
    }

    public static void getPushState(Context context) {
        Log.d("PushLogLightSC2606", "enter PushEntity:getPushState() pkgName" + context.getPackageName());
        Intent intent = new Intent("com.huawei.android.push.intent.GET_PUSH_STATE");
        intent.putExtra(KEY_TYPE.PKGNAME, context.getPackageName());
        intent.setFlags(32);
        context.sendOrderedBroadcast(intent, null);
    }

    public static final void getToken(Context context) {
        Log.d("PushLogLightSC2606", "enter PushEntity:getToken() pkgName" + context.getPackageName());
        Intent intent = new Intent("com.huawei.android.push.intent.REGISTER");
        intent.putExtra(KEY_TYPE.PKGNAME, context.getPackageName());
        intent.setFlags(32);
        context.sendBroadcast(intent);
        new e(context, "push_client_self_info").a("hasRequestToken", true);
    }

    public boolean canExit() {
        return true;
    }

    public void onNotifyBtnClick(Context context, int i, String str, Bundle bundle) {
    }

    public void onNotifyClickMsg(Context context, String str) {
    }

    public void onPluginRsp(Context context, int i, boolean z, Bundle bundle) {
    }

    public abstract void onPushMsg(Context context, byte[] bArr, String str);

    public void onPushState(Context context, boolean z) {
    }

    public final void onReceive(Context context, Intent intent) {
        try {
            Bundle bundle = new Bundle();
            Log.d("PushLogLightSC2606", "enter PushMsgReceiver:onReceive(Intent:" + intent.getAction() + " pkgName:" + context.getPackageName() + ")");
            String action = intent.getAction();
            if ("com.huawei.android.push.intent.REGISTRATION".equals(action) && intent.hasExtra(SERVER.DEVICETOKEN)) {
                action = new String(intent.getByteArrayExtra(SERVER.DEVICETOKEN), "UTF-8");
                Log.d("PushLogLightSC2606", "get a deviceToken");
                if (TextUtils.isEmpty(action)) {
                    Log.w("PushLogLightSC2606", "get a deviceToken, but it is null");
                    return;
                }
                e eVar = new e(context, "push_client_self_info");
                boolean a = eVar.a("hasRequestToken");
                String a2 = d.a(context, "push_client_self_info", "token_info");
                if (!a && action.equals(a2)) {
                    Log.w("PushLogLightSC2606", "get a deviceToken, but do not requested token, and new token is equals old token");
                }
                Log.i("PushLogLightSC2606", "push client begin to receive the token");
                eVar.a("hasRequestToken", false);
                eVar.d("token_info");
                d.a(context, "push_client_self_info", "token_info", action);
                bundle.putString(BOUND_KEY.deviceTokenKey, action);
                bundle.putByteArray(BOUND_KEY.pushMsgKey, null);
                bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_Token.ordinal());
                if (intent.getExtras() != null) {
                    bundle.putAll(intent.getExtras());
                }
                new EventThread(this, context, bundle).start();
            } else if (ACTION.ACTION_PUSH_MESSAGE.equals(action) && intent.hasExtra(KEY_TYPE.PUSH_BROADCAST_MESSAGE)) {
                boolean a3 = new e(context, "push_switch").a("normal_msg_enable");
                Log.d("PushLogLightSC2606", "closePush_Normal:" + a3);
                if (!a3) {
                    byte[] byteArrayExtra = intent.getByteArrayExtra(KEY_TYPE.PUSH_BROADCAST_MESSAGE);
                    String str = new String(intent.getByteArrayExtra(SERVER.DEVICETOKEN), "UTF-8");
                    Log.d("PushLogLightSC2606", "PushReceiver receive a message success");
                    bundle.putString(BOUND_KEY.deviceTokenKey, str);
                    bundle.putByteArray(BOUND_KEY.pushMsgKey, byteArrayExtra);
                    bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_Msg.ordinal());
                    new EventThread(this, context, bundle).start();
                }
            } else if (ACTION.ACTION_PUSH_MESSAGE.equals(action) && intent.hasExtra(KEY_TYPE.PUSH_KEY_CLICK)) {
                bundle.putString(BOUND_KEY.pushMsgKey, intent.getStringExtra(KEY_TYPE.PUSH_KEY_CLICK));
                bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_NotifyClick.ordinal());
                new EventThread(this, context, bundle).start();
            } else if (ACTION.ACTION_PUSH_MESSAGE.equals(action) && intent.hasExtra(KEY_TYPE.PUSH_KEY_CLICK_BTN)) {
                action = intent.getStringExtra(KEY_TYPE.PUSH_KEY_CLICK_BTN);
                int intExtra = intent.getIntExtra(KEY_TYPE.PUSH_KEY_NOTIFY_ID, 0);
                bundle.putString(BOUND_KEY.pushMsgKey, action);
                bundle.putInt(BOUND_KEY.pushNotifyId, intExtra);
                bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_ClickBtn.ordinal());
                new EventThread(this, context, bundle).start();
            } else {
                if ("com.huawei.intent.action.PUSH_STATE".equals(action)) {
                    bundle.putBoolean(BOUND_KEY.pushStateKey, intent.getBooleanExtra(KEY_TYPE.PUSHSTATE, false));
                    bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_PushState.ordinal());
                    new EventThread(this, context, bundle).start();
                } else if ("com.huawei.intent.action.PUSH".equals(action) && intent.hasExtra("selfshow_info")) {
                    a(context, intent);
                } else {
                    Log.w("PushLogLightSC2606", "unknowned message");
                }
            }
        } catch (Throwable e) {
            Log.e("PushLogLightSC2606", "call onReceive(intent:" + intent + ") cause:" + e.toString(), e);
        }
    }

    public abstract void onToken(Context context, String str);

    public void onToken(Context context, String str, Bundle bundle) {
        onToken(context, str);
    }
}
