package defpackage;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.ResolveInfo;
import android.text.TextUtils;
import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.datatype.pushmessage.RegisterTokenReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.UnRegisterReqMessage;
import com.huawei.android.pushagent.datatype.pushmessage.basic.PushMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.json.JSONObject;

/* renamed from: ao */
public class ao {
    private static ao bu;
    public HashMap bv;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: ao.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: ao.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: ao.<clinit>():void");
    }

    private ao(Context context) {
        this.bv = new HashMap();
        aw.d("PushLog2828", "DeviceTokenMgr: create the DeviceTokenMgr");
        bt btVar = new bt(context, "pclient_info_v2");
        if (this.bv == null) {
            aw.e("PushLog2828", "create TokenMap occur an error!!");
            return;
        }
        Set<String> keySet = btVar.getAll().keySet();
        if (keySet != null && keySet.size() > 0) {
            for (String str : keySet) {
                CharSequence s = az.s(context, str);
                if (TextUtils.isEmpty(s)) {
                    btVar.z(str);
                } else {
                    this.bv.put(s, str);
                    aw.i("PushLog2828", str + " has registed token");
                }
            }
        }
    }

    public static ArrayList A(Context context) {
        bt btVar = new bt(context, "pclient_info_v2");
        ArrayList arrayList = new ArrayList();
        List queryBroadcastReceivers = context.getPackageManager().queryBroadcastReceivers(new Intent("com.huawei.android.push.intent.RECEIVE"), 640, ActivityManager.getCurrentUser());
        int size = queryBroadcastReceivers == null ? 0 : queryBroadcastReceivers.size();
        if (size == 0) {
            aw.d("PushLog2828", "we have no push client");
            return arrayList;
        }
        String E = au.E(context);
        if (E == null) {
            aw.e("PushLog2828", "have no deviceId, when queryAllClientForRegister");
            return arrayList;
        }
        ArrayList arrayList2 = new ArrayList();
        Set<String> keySet = btVar.getAll().keySet();
        for (int i = 0; i < size; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) queryBroadcastReceivers.get(i);
            ComponentInfo componentInfo = resolveInfo.activityInfo != null ? resolveInfo.activityInfo : resolveInfo.serviceInfo;
            if (!(componentInfo == null || TextUtils.isEmpty(componentInfo.packageName) || "com.huawei.android.pushagent".equals(componentInfo.packageName))) {
                arrayList2.add(componentInfo.packageName);
            }
        }
        ao.a(context, E, arrayList, arrayList2);
        if (keySet != null && keySet.size() > 0) {
            for (String str : keySet) {
                String o = au.o(str);
                int p = au.p(str);
                if (!arrayList2.contains(o)) {
                    if (p == ActivityManager.getCurrentUser()) {
                        String s = az.s(context, str);
                        aw.d("PushLog2828", "this package [" + o + "] need to unregister device token");
                        arrayList.add(new UnRegisterReqMessage(s));
                        ao.f(context, str);
                    } else {
                        aw.d("PushLog2828", "apk not found in this user, but other user maybe exist:" + str);
                    }
                }
            }
        }
        if (arrayList != null && arrayList.size() == 0) {
            aw.d("PushLog2828", "there is no more client need register and unregister token");
        }
        return arrayList;
    }

    public static void B(Context context) {
        bt btVar = new bt(context, "pclient_info_v2");
        Map all = btVar.getAll();
        if (all != null && all.size() > 0) {
            bt btVar2 = new bt(context, "pclient_request_info");
            for (String str : all.keySet()) {
                btVar2.f(str, "true");
                aw.d("PushLog2828", str + " need to register again");
            }
            btVar.clear();
            ao.z(context).bv.clear();
        }
    }

    private static void a(Context context, String str, ArrayList arrayList, ArrayList arrayList2) {
        bt btVar = new bt(context, "pclient_request_info");
        for (String str2 : btVar.getAll().keySet()) {
            Object obj = str2.split("/")[0];
            int p = au.p(str2);
            if (arrayList2.contains(obj) || p != ActivityManager.getCurrentUser()) {
                PushMessage c = ao.c(context, str, str2);
                if (c != null) {
                    arrayList.add(c);
                } else {
                    btVar.z(str2);
                }
            } else {
                btVar.z(str2);
            }
        }
    }

    static void a(Context context, String[] strArr) {
        if (strArr != null && strArr.length != 0) {
            bt btVar = new bt(context, "pclient_info_v2");
            bt btVar2 = new bt(context, "pclient_request_info");
            Set<String> keySet = btVar.getAll().keySet();
            for (String str : strArr) {
                aw.d("PushLog2828", str + " need to register again");
                String s = az.s(context, str);
                for (String str2 : keySet) {
                    if (str2.startsWith(str)) {
                        btVar.z(str2);
                        ao.z(context).bv.remove(s);
                        btVar2.f(str2, "true");
                    }
                }
            }
        }
    }

    private static PushMessage c(Context context, String str, String str2) {
        if (TextUtils.isEmpty(str2)) {
            aw.d("PushLog2828", "packageName is null when buildRegisterReqMsg");
            return null;
        }
        String[] split = str2.split("/");
        if (split.length <= 1) {
            return new RegisterTokenReqMessage(str, au.o(context, str2));
        }
        Object obj = split[0];
        Object obj2 = split[1];
        PushMessage decoupledPushMessage = new DecoupledPushMessage((byte) -92);
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("cmdid", -36);
            jSONObject.put("packageName", obj);
            jSONObject.put("usrid", obj2);
            decoupledPushMessage.a(jSONObject);
            return decoupledPushMessage;
        } catch (Throwable e) {
            aw.d("PushLog2828", "create DecoupledPushMessage params error:" + e.toString(), e);
            return null;
        }
    }

    public static boolean d(Context context, String str, String str2) {
        bt btVar = new bt(context, "pclient_info_v2");
        String b = au.b(str, str2);
        return (btVar == null || !btVar.containsKey(b) || TextUtils.isEmpty(btVar.getString(b))) ? false : true;
    }

    public static void e(Context context, String str, String str2) {
        if (ao.z(context).bv == null) {
            aw.e("PushLog2828", "responseRegisterToken the map is null!!! ");
            return;
        }
        ao.z(context).bv.put(str, str2);
        az.l(context, str2, str);
    }

    public static void f(Context context, String str) {
        if (ao.z(context).bv == null) {
            aw.e("PushLog2828", "when removeClientInfo, tokenMap the map is null!!! ");
            return;
        }
        bt btVar = new bt(context, "pclient_info_v2");
        aw.d("PushLog2828", "remove package:" + str);
        btVar.z(str);
    }

    public static void g(Context context, String str) {
        String str2 = "";
        if (ao.z(context).bv != null) {
            str2 = (String) ao.z(context).bv.get(str);
        }
        bt btVar = new bt(context, "pclient_info_v2");
        aw.d("PushLog2828", "responseUnregisterToken,after delPClientInfo token,  packagename: " + str2);
        ao.z(context).bv.remove(str);
        btVar.z(str2);
    }

    public static String h(Context context, String str) {
        return az.s(context, str);
    }

    public static synchronized ao z(Context context) {
        ao aoVar;
        synchronized (ao.class) {
            if (bu != null) {
                aoVar = bu;
            } else {
                bu = new ao(context);
                aoVar = bu;
            }
        }
        return aoVar;
    }
}
