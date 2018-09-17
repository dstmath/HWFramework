package defpackage;

import android.content.Context;
import android.util.Log;
import com.huawei.android.pushagent.datatype.pushmessage.DecoupledPushMessage;
import com.huawei.android.pushagent.model.channel.ChannelMgr;
import org.json.JSONArray;
import org.json.JSONObject;

/* renamed from: an */
public class an {
    private static an bt;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: an.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: an.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: an.<clinit>():void");
    }

    private an() {
    }

    private void a(JSONObject jSONObject, Context context) {
        Log.d("PushLog2828", "server command agent to refresh token");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshToken");
        if (jSONObject2.has("pkgs")) {
            JSONArray jSONArray = jSONObject2.getJSONArray("pkgs");
            String[] strArr = new String[jSONArray.length()];
            String str = "";
            for (int i = 0; i < jSONArray.length(); i++) {
                String string = jSONArray.getString(i);
                Log.d("PushLog2828", "package need to refresh token:" + string);
                strArr[i] = string;
            }
            ao.a(context, strArr);
            return;
        }
        Log.d("PushLog2828", "all packages need to refresh token");
        ao.B(context);
    }

    private void b(JSONObject jSONObject, Context context) {
        Log.d("PushLog2828", "server command agent to refresh trs");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshTrs");
        if (jSONObject2.has("belongId")) {
            int i = jSONObject2.getInt("belongId");
            Log.d("PushLog2828", "need to refresh trs in belongId:" + i);
            if (i >= 0) {
                ae.l(context).a("belongId", (Object) Integer.valueOf(i));
            }
        }
        ae.m(context);
    }

    public static synchronized an bH() {
        an anVar;
        synchronized (an.class) {
            if (bt != null) {
                anVar = bt;
            } else {
                bt = new an();
                anVar = bt;
            }
        }
        return anVar;
    }

    private void c(JSONObject jSONObject, Context context) {
        Log.d("PushLog2828", "server command agent to refresh heartbeat");
        JSONObject jSONObject2 = jSONObject.getJSONObject("refreshHb");
        if (jSONObject2.has("fixedWifiHb")) {
            int i = jSONObject2.getInt("fixedWifiHb");
            Log.d("PushLog2828", "fixed heartbeat in wifi is " + i);
            if (i > 60) {
                ae.l(context).b((long) i);
                ae.l(context).c((long) i);
            } else {
                Log.d("PushLog2828", "fixed heartbeat in wifi is invalid");
            }
        }
        if (jSONObject2.has("fixed3GHb")) {
            int i2 = jSONObject2.getInt("fixed3GHb");
            Log.d("PushLog2828", "fixed heartbeat in 3g is " + i2);
            if (i2 > 60) {
                ae.l(context).d((long) i2);
                ae.l(context).e((long) i2);
            } else {
                Log.d("PushLog2828", "fixed heartbeat in 3g is invalid");
            }
        }
        try {
            Log.d("PushLog2828", "delete heartbeat files and reload heartbeat");
            au.n(context, new y(context).bd());
            ChannelMgr.aW().S.bg();
        } catch (Throwable e) {
            Log.e("PushLog2828", "delete heartbeat files or reload heartbeat error:" + e.getMessage(), e);
        }
    }

    public void a(Context context, DecoupledPushMessage decoupledPushMessage) {
        try {
            byte j = decoupledPushMessage.j();
            if (-91 == j) {
                Log.d("PushLog2828", "receive response from server");
            } else if (-90 == j) {
                JSONObject aF = decoupledPushMessage.aF();
                if (aF.has("refreshHb")) {
                    c(aF, context);
                } else if (aF.has("refreshToken")) {
                    a(aF, context);
                } else if (aF.has("refreshTrs")) {
                    b(aF, context);
                } else {
                    Log.e("PushLog2828", "cannot parse the unknown message:" + aF.toString());
                }
            }
        } catch (Throwable e) {
            Log.e("PushLog2828", "parse json error:" + e.getMessage(), e);
        } catch (Throwable e2) {
            Log.e("PushLog2828", "parse DecoupledPushMessage error: " + e2.getMessage(), e2);
        }
    }
}
