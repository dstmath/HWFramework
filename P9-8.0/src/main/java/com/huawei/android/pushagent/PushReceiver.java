package com.huawei.android.pushagent;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.util.Log;
import com.huawei.android.pushagent.a.a.d;
import com.huawei.android.pushagent.a.a.e;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

public abstract class PushReceiver extends BroadcastReceiver {
    private static int a = -1;

    public static class ACTION {
        public static final String ACTION_CLIENT_DEREGISTER = "com.huawei.android.push.intent.DEREGISTER";
        public static final String ACTION_NOTIFICATION_MSG_CLICK = "com.huawei.android.push.intent.CLICK";
        public static final String ACTION_PUSH_MESSAGE = "com.huawei.android.push.intent.RECEIVE";
    }

    public static class BOUND_KEY {
        public static final String deviceTokenKey = "deviceToken";
        public static final String pushMsgKey = "pushMsg";
        public static final String pushNotifyId = "pushNotifyId";
        public static final String pushStateKey = "pushState";
        public static final String receiveTypeKey = "receiveType";
    }

    class EventThread extends Thread {
        Context a;
        Bundle b;

        public EventThread(Context context, Bundle bundle) {
            super("EventRunable");
            this.a = context;
            this.b = bundle;
        }

        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void run() {
            try {
                if (this.b != null) {
                    int i = this.b.getInt(BOUND_KEY.receiveTypeKey);
                    if (i >= 0 && i < ReceiveType.values().length) {
                        switch (ReceiveType.values()[i]) {
                            case ReceiveType_Token:
                                PushReceiver.this.onToken(this.a, this.b.getString(BOUND_KEY.deviceTokenKey), this.b);
                                break;
                            case ReceiveType_Msg:
                                PushReceiver.this.onPushMsg(this.a, this.b.getByteArray(BOUND_KEY.pushMsgKey), this.b.getString(BOUND_KEY.deviceTokenKey));
                                break;
                            case ReceiveType_PushState:
                                PushReceiver.this.onPushState(this.a, this.b.getBoolean(BOUND_KEY.pushStateKey));
                                break;
                            case ReceiveType_NotifyClick:
                                PushReceiver.this.onNotifyClickMsg(this.a, this.b.getString(BOUND_KEY.pushMsgKey));
                                break;
                            case ReceiveType_ClickBtn:
                                PushReceiver.this.onNotifyBtnClick(this.a, this.b.getInt(BOUND_KEY.pushNotifyId), this.b.getString(BOUND_KEY.pushMsgKey), new Bundle());
                                break;
                            case ReceiveType_PluginRsp:
                                PushReceiver.this.onPluginRsp(this.a, this.b.getInt(KEY_TYPE.PLUGINREPORTTYPE, -1), this.b.getBoolean(KEY_TYPE.PLUGINREPORTRESULT, false), this.b.getBundle(KEY_TYPE.PLUGINREPORTEXTRA));
                                break;
                        }
                        return;
                    }
                    Log.e("PushLogLightSC2907", "invalid receiverType:" + i);
                    return;
                }
                return;
            } catch (Throwable e) {
                Log.e("PushLogLightSC2907", "call EventThread(ReceiveType cause:" + e.toString(), e);
            }
        }
    }

    static class HandlePushTokenThread extends Thread {
        Context a;
        String b;
        String c;

        public HandlePushTokenThread(Context context, String str, String str2) {
            this.a = context;
            this.b = str;
            this.c = str2;
        }

        public void run() {
            e eVar = new e(this.a, "push_client_self_info");
            eVar.a("hasRequestToken", false);
            eVar.d("token_info");
            d.a(this.a, "push_client_self_info", "token_info", this.b);
            if (!TextUtils.isEmpty(this.c)) {
                String a = com.huawei.android.pushagent.a.a.a.d.a(this.a, this.c);
                if (!TextUtils.isEmpty(a)) {
                    new e(this.a, "push_client_self_info").a("push_notify_key", a);
                }
            }
        }
    }

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
        public static final String USERID = "userid";
    }

    enum ReceiveType {
        ReceiveType_Init,
        ReceiveType_Token,
        ReceiveType_Msg,
        ReceiveType_PushState,
        ReceiveType_NotifyClick,
        ReceiveType_PluginRsp,
        ReceiveType_ClickBtn
    }

    public static class SERVER {
        public static final String DEVICETOKEN = "device_token";
    }

    private static int a() {
        int i = -999;
        try {
            Class cls = Class.forName("android.os.UserHandle");
            i = ((Integer) cls.getDeclaredMethod("myUserId", new Class[0]).invoke(cls, new Object[0])).intValue();
            Log.d("PushLogLightSC2907", "getUserId:" + i);
            return i;
        } catch (ClassNotFoundException e) {
            Log.d("PushLogLightSC2907", " getUserId wrong");
            return i;
        } catch (NoSuchMethodException e2) {
            Log.d("PushLogLightSC2907", " getUserId wrong");
            return i;
        } catch (IllegalArgumentException e3) {
            Log.d("PushLogLightSC2907", " getUserId wrong");
            return i;
        } catch (IllegalAccessException e4) {
            Log.d("PushLogLightSC2907", " getUserId wrong");
            return i;
        } catch (InvocationTargetException e5) {
            Log.d("PushLogLightSC2907", " getUserId wrong");
            return i;
        }
    }

    private void a(Context context, Intent intent) throws UnsupportedEncodingException {
        if (context == null || intent == null) {
            Log.e("PushLogLightSC2907", "context is null");
            return;
        }
        String str = new String(intent.getByteArrayExtra("device_token"), "UTF-8");
        CharSequence stringExtra = intent.getStringExtra("extra_encrypt_key");
        Log.d("PushLogLightSC2907", "get a deviceToken");
        if (TextUtils.isEmpty(str)) {
            Log.w("PushLogLightSC2907", "get a deviceToken, but it is null");
            return;
        }
        boolean a = new e(context, "push_client_self_info").a("hasRequestToken");
        String a2 = d.a(context, "push_client_self_info", "token_info");
        Object b = new e(context, "push_client_self_info").b("push_notify_key");
        Object obj = (TextUtils.isEmpty(b) ? !TextUtils.isEmpty(stringExtra) : !(TextUtils.isEmpty(stringExtra) || b.equals(stringExtra))) ? 1 : null;
        if (!a && str.equals(a2) && obj == null) {
            Log.w("PushLogLightSC2907", "get a deviceToken, but do not requested token, and new token is equals old token");
        } else {
            Log.i("PushLogLightSC2907", "push client begin to receive the token");
            new HandlePushTokenThread(context, str, stringExtra).start();
            Bundle bundle = new Bundle();
            bundle.putString(BOUND_KEY.deviceTokenKey, str);
            bundle.putByteArray(BOUND_KEY.pushMsgKey, null);
            bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_Token.ordinal());
            if (intent.getExtras() != null) {
                bundle.putAll(intent.getExtras());
            }
            new EventThread(context, bundle).start();
        }
    }

    private static boolean a(Context context, boolean z) {
        boolean z2 = false;
        Log.d("PushLogLightSC2907", "existFrameworkPush:" + a + ",realCheck:" + z);
        if (z) {
            try {
                File file = new File("/system/framework/" + "hwpush.jar");
                if (isInCustDir()) {
                    Log.d("PushLogLightSC2907", "push jarFile is exist in cust");
                } else if (file.isFile()) {
                    Log.d("PushLogLightSC2907", "push jarFile is exist");
                } else {
                    Log.i("PushLogLightSC2907", "push jarFile is not exist");
                    if (SystemProperties.getBoolean("ro.config.push_enable", "CN".equals(SystemProperties.get("ro.product.locale.region")))) {
                        String str = SystemProperties.get("ro.build.version.emui", "-1");
                        if (!TextUtils.isEmpty(str) && (str.contains("2.0") || str.contains("2.3"))) {
                            Log.d("PushLogLightSC2907", "emui is 2.0 or 2.3");
                        } else {
                            Log.d("PushLogLightSC2907", "can not use framework push");
                            return false;
                        }
                    }
                    Log.d("PushLogLightSC2907", "framework not support push");
                    return false;
                }
                List queryIntentServices = context.getPackageManager().queryIntentServices(new Intent().setClassName("android", "com.huawei.android.pushagentproxy.PushService"), 128);
                if (queryIntentServices == null || queryIntentServices.size() == 0) {
                    Log.i("PushLogLightSC2907", "framework push not exist, need vote apk or sdk to support pushservice");
                    return false;
                }
                Log.i("PushLogLightSC2907", "framework push exist, use framework push first");
                return true;
            } catch (Exception e) {
                Log.e("PushLogLightSC2907", "get Apk version faild ,Exception e= " + e.toString());
                return false;
            }
        }
        if (1 == a) {
            z2 = true;
        }
        return z2;
    }

    private void b(Context context, Intent intent) throws UnsupportedEncodingException {
        if (context == null || intent == null) {
            Log.e("PushLogLightSC2907", "context is null");
            return;
        }
        g(context, intent);
        boolean a = new e(context, "push_switch").a("normal_msg_enable");
        Log.d("PushLogLightSC2907", "closePush_Normal:" + a);
        if (!a) {
            byte[] byteArrayExtra = intent.getByteArrayExtra(KEY_TYPE.PUSH_BROADCAST_MESSAGE);
            String str = new String(intent.getByteArrayExtra("device_token"), "UTF-8");
            Log.d("PushLogLightSC2907", "PushReceiver receive a message success");
            Bundle bundle = new Bundle();
            bundle.putString(BOUND_KEY.deviceTokenKey, str);
            bundle.putByteArray(BOUND_KEY.pushMsgKey, byteArrayExtra);
            bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_Msg.ordinal());
            new EventThread(context, bundle).start();
        }
    }

    private void c(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e("PushLogLightSC2907", "context is null");
            return;
        }
        String stringExtra = intent.getStringExtra(KEY_TYPE.PUSH_KEY_CLICK);
        Bundle bundle = new Bundle();
        bundle.putString(BOUND_KEY.pushMsgKey, stringExtra);
        bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_NotifyClick.ordinal());
        new EventThread(context, bundle).start();
    }

    private void d(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e("PushLogLightSC2907", "context is null");
            return;
        }
        String stringExtra = intent.getStringExtra(KEY_TYPE.PUSH_KEY_CLICK_BTN);
        int intExtra = intent.getIntExtra(KEY_TYPE.PUSH_KEY_NOTIFY_ID, 0);
        Bundle bundle = new Bundle();
        bundle.putString(BOUND_KEY.pushMsgKey, stringExtra);
        bundle.putInt(BOUND_KEY.pushNotifyId, intExtra);
        bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_ClickBtn.ordinal());
        new EventThread(context, bundle).start();
    }

    private void e(Context context, Intent intent) {
        if (context == null || intent == null) {
            Log.e("PushLogLightSC2907", "context is null");
            return;
        }
        boolean booleanExtra = intent.getBooleanExtra(KEY_TYPE.PUSHSTATE, false);
        Bundle bundle = new Bundle();
        bundle.putBoolean(BOUND_KEY.pushStateKey, booleanExtra);
        bundle.putInt(BOUND_KEY.receiveTypeKey, ReceiveType.ReceiveType_PushState.ordinal());
        new EventThread(context, bundle).start();
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
        Log.d("PushLogLightSC2907", "context is null");
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
        Log.d("PushLogLightSC2907", "context is null");
    }

    private void f(Context context, Intent intent) {
        boolean a = new e(context, "push_switch").a("notify_msg_enable");
        Log.d("PushLogLightSC2907", "closePush_Notify:" + a);
        if (!a) {
            try {
                Log.i("PushLogLightSC2907", "run push selfshow");
                Class cls = Class.forName("com.huawei.android.pushselfshow.SelfShowReceiver");
                Object newInstance = cls.getConstructor(new Class[0]).newInstance(new Object[0]);
                cls.getDeclaredMethod("onReceive", new Class[]{Context.class, Intent.class}).invoke(newInstance, new Object[]{context, intent});
            } catch (Throwable e) {
                Log.e("PushLogLightSC2907", "SelfShowReceiver class not found:" + e.getMessage(), e);
            } catch (Throwable e2) {
                Log.e("PushLogLightSC2907", "onReceive method not found:" + e2.getMessage(), e2);
            } catch (Throwable e22) {
                Log.e("PushLogLightSC2907", "invokeSelfShow error:" + e22.getMessage(), e22);
            }
        }
    }

    private void g(Context context, Intent intent) {
        if (context != null && intent != null) {
            Object stringExtra = intent.getStringExtra("msgIdStr");
            if (!TextUtils.isEmpty(stringExtra) && isFrameworkPushExist(context)) {
                Intent intent2 = new Intent("com.huawei.android.push.intent.MSG_RESPONSE");
                intent2.putExtra("msgIdStr", stringExtra);
                intent2.setPackage("android");
                intent2.setFlags(32);
                Log.d("PushLogLightSC2907", "send msg response broadcast to frameworkPush");
                context.sendBroadcast(intent2);
            }
        }
    }

    public static void getPushState(Context context) {
        Log.d("PushLogLightSC2907", "enter PushEntity:getPushState() pkgName" + context.getPackageName());
        Intent intent = new Intent("com.huawei.android.push.intent.GET_PUSH_STATE");
        intent.putExtra(KEY_TYPE.PKGNAME, context.getPackageName());
        intent.setFlags(32);
        if (isFrameworkPushExist(context)) {
            intent.setPackage("android");
            Log.d("PushLogLightSC2907", "send register broadcast to frameworkPush");
        }
        context.sendOrderedBroadcast(intent, null);
    }

    public static final void getToken(Context context) {
        Log.d("PushLogLightSC2907", "enter PushEntity:getToken() pkgName" + context.getPackageName());
        Intent intent = new Intent("com.huawei.android.push.intent.REGISTER");
        intent.putExtra(KEY_TYPE.PKGNAME, context.getPackageName());
        int a = a();
        if (-999 != a) {
            intent.putExtra(KEY_TYPE.USERID, String.valueOf(a));
        }
        intent.setFlags(32);
        if (isFrameworkPushExist(context)) {
            intent.setPackage("android");
            Log.d("PushLogLightSC2907", "send register broadcast to frameworkPush");
        }
        context.sendBroadcast(intent);
        new e(context, "push_client_self_info").a("hasRequestToken", true);
    }

    /* JADX WARNING: Missing block: B:11:0x0035, code:
            return r0;
     */
    /* JADX WARNING: Missing block: B:16:0x003b, code:
            return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized boolean isFrameworkPushExist(Context context) {
        boolean z = false;
        synchronized (PushReceiver.class) {
            Log.d("PushLogLightSC2907", "existFrameworkPush:" + a);
            if (-1 == a) {
                if (a(context, true)) {
                    a = 1;
                } else {
                    a = 0;
                }
                if (1 == a) {
                    z = true;
                }
            } else if (1 == a) {
                z = true;
            }
        }
    }

    public static boolean isInCustDir() {
        try {
            Class cls = Class.forName("huawei.cust.HwCfgFilePolicy");
            int intValue = ((Integer) cls.getDeclaredField("CUST_TYPE_CONFIG").get(cls)).intValue();
            File file = (File) cls.getDeclaredMethod("getCfgFile", new Class[]{String.class, Integer.TYPE}).invoke(cls, new Object[]{"jars/hwpush.jar", Integer.valueOf(intValue)});
            if (file != null && file.exists()) {
                Log.i("PushLogLightSC2907", "get push cust File path is " + file.getAbsolutePath());
                return true;
            }
        } catch (ClassNotFoundException e) {
            Log.e("PushLogLightSC2907", "HwCfgFilePolicy ClassNotFoundException");
        } catch (Throwable e2) {
            Log.e("PushLogLightSC2907", e2.toString(), e2);
        } catch (Throwable e22) {
            Log.e("PushLogLightSC2907", e22.toString(), e22);
        } catch (Throwable e222) {
            Log.e("PushLogLightSC2907", e222.toString(), e222);
        } catch (Throwable e2222) {
            Log.e("PushLogLightSC2907", e2222.toString(), e2222);
        } catch (Throwable e22222) {
            Log.e("PushLogLightSC2907", e22222.toString(), e22222);
        } catch (Throwable e222222) {
            Log.e("PushLogLightSC2907", e222222.toString(), e222222);
        }
        return false;
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
            Log.d("PushLogLightSC2907", "enter PushMsgReceiver:onReceive(Intent:" + intent.getAction() + " pkgName:" + context.getPackageName() + ")");
            String action = intent.getAction();
            if ("com.huawei.android.push.intent.REGISTRATION".equals(action) && intent.hasExtra("device_token")) {
                a(context, intent);
                return;
            }
            if (ACTION.ACTION_PUSH_MESSAGE.equals(action)) {
                if (intent.hasExtra(KEY_TYPE.PUSH_BROADCAST_MESSAGE)) {
                    b(context, intent);
                    return;
                }
            }
            if (ACTION.ACTION_NOTIFICATION_MSG_CLICK.equals(action)) {
                if (intent.hasExtra(KEY_TYPE.PUSH_KEY_CLICK)) {
                    c(context, intent);
                    return;
                }
            }
            if (ACTION.ACTION_NOTIFICATION_MSG_CLICK.equals(action) && intent.hasExtra(KEY_TYPE.PUSH_KEY_CLICK_BTN)) {
                d(context, intent);
            } else if ("com.huawei.intent.action.PUSH_STATE".equals(action)) {
                e(context, intent);
            } else if ("com.huawei.intent.action.PUSH".equals(action) && intent.hasExtra("selfshow_info")) {
                f(context, intent);
            } else {
                Log.w("PushLogLightSC2907", "unknowned message");
            }
        } catch (Throwable e) {
            Log.e("PushLogLightSC2907", "call onReceive(intent:" + intent + ") cause:" + e.toString(), e);
        } catch (Throwable e2) {
            Log.e("PushLogLightSC2907", "call onReceive(intent:" + intent + ") cause:" + e2.toString(), e2);
        }
    }

    public abstract void onToken(Context context, String str);

    public void onToken(Context context, String str, Bundle bundle) {
        onToken(context, str);
    }
}
