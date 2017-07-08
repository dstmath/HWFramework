package tmsdkobf;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.ErrorCode;
import tmsdk.common.TMSDKContext;
import tmsdk.common.TMServiceFactory;
import tmsdk.common.creator.BaseManagerC;
import tmsdk.common.utils.l;

/* compiled from: Unknown */
final class qu extends BaseManagerC {
    public static String TAG;
    private qr KD;
    private Context mContext;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: tmsdkobf.qu.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: tmsdkobf.qu.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: tmsdkobf.qu.<clinit>():void");
    }

    qu() {
    }

    private cr v(Context context) {
        cr crVar = new cr();
        crVar.b(l.dk(TMSDKContext.getStrFromEnvMap(TMSDKContext.CON_CHANNEL)));
        crVar.e(TMSDKContext.getIntFromEnvMap(TMSDKContext.CON_PRODUCT));
        crVar.f(0);
        py b = TMServiceFactory.getSystemInfoService().b(context.getPackageName(), 1);
        if (b != null && b.hA()) {
            crVar.f(1);
        }
        return crVar;
    }

    public int a(String str, AtomicReference<et> atomicReference) {
        qs cx = qp.cx(17);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("checkrequest", new es(str, null, 0, 2));
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cx.KA, "checkresponse", new et());
        if (a2 != null) {
            atomicReference.set((et) a2);
        }
        return 0;
    }

    public int a(List<eg> list, ArrayList<cp> arrayList, boolean z, int i) {
        qs cx = qp.cx(6);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.cy(i));
        hashMap.put("vecSoftFeature", list);
        cx.Ky = hashMap;
        cx.KB = z;
        int a = this.KD.a(cx);
        if (a != 0) {
            return a;
        }
        int i2;
        Object arrayList2 = new ArrayList();
        arrayList2.add(new cp());
        try {
            Object a2 = this.KD.a(cx.KA, "vecAnalyseInfo", arrayList2);
            if (a2 != null) {
                arrayList.clear();
                arrayList.addAll((Collection) a2);
            }
            i2 = 0;
        } catch (Throwable th) {
            i2 = ErrorCode.ERR_WUP;
        }
        return i2;
    }

    public int a(List<String> list, AtomicReference<du> atomicReference) {
        qs cx = qp.cx(18);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        int size = list.size();
        ArrayList arrayList = new ArrayList(size);
        for (int i = 0; i < size; i++) {
            arrayList.add(new es((String) list.get(i), null, 0, 2));
        }
        hashMap.put("reqtemp", new ds(arrayList));
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cx.KA, "rsptemp", new du());
        if (a2 != null) {
            atomicReference.set((du) a2);
        }
        return 0;
    }

    public int a(dj djVar, di diVar) {
        qs cx = qp.cx(20);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("licinfo", djVar);
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cx.KA, "outinfo", new dt());
        if (a2 == null) {
            return -2;
        }
        diVar.ir = (dt) a2;
        a2 = this.KD.a(cx.KA, "", Integer.valueOf(0));
        return a2 != null ? ((Integer) a2).intValue() : -2;
    }

    public int a(dr drVar) {
        qs cx = qp.cx(0);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("softreportinfo", drVar);
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        return a == 0 ? 0 : a;
    }

    public int a(ew ewVar, AtomicReference<ez> atomicReference, ArrayList<ey> arrayList, int i) {
        qs cx = qp.cx(2);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.cy(i));
        hashMap.put("clientinfo", ewVar);
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cx.KA, "serverinfo", new ez());
        if (a2 != null) {
            atomicReference.set((ez) a2);
        }
        a2 = new ArrayList();
        a2.add(new ey());
        a2 = this.KD.a(cx.KA, "virusinfos", a2);
        if (a2 != null) {
            arrayList.clear();
            arrayList.addAll((Collection) a2);
        }
        return 0;
    }

    public int b(da daVar, AtomicReference<dh> atomicReference) {
        qs cx = qp.cx(9);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hS());
        hashMap.put("userinfo", this.KD.hU());
        hashMap.put("deviceinfo", daVar);
        cx.Ky = hashMap;
        int a = this.KD.a(cx, true);
        if (a != 0) {
            return a;
        }
        Object a2 = this.KD.a(cx.KA, "guidinfo", new dh());
        if (a2 != null) {
            atomicReference.set((dh) a2);
        }
        return 0;
    }

    public int getSingletonType() {
        return 1;
    }

    public int ib() {
        this.KD.Kr = true;
        try {
            qs cx = qp.cx(1);
            HashMap hashMap = new HashMap(3);
            hashMap.put("phonetype", this.KD.hS());
            hashMap.put("userinfo", this.KD.hU());
            hashMap.put("channelinfo", v(this.mContext));
            cx.Ky = hashMap;
            int a = this.KD.a(cx);
            if (a == 0) {
                return a;
            }
            this.KD.Kr = false;
            return a;
        } finally {
            this.KD.Kr = false;
        }
    }

    public qo ic() {
        return this.KD;
    }

    public void onCreate(Context context) {
        this.mContext = context;
        this.KD = new qr(this.mContext);
    }

    public int x(List<ed> list) {
        qs cx = qp.cx(12);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hV());
        hashMap.put("vecSmsReport", list);
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        return a == 0 ? 0 : a;
    }

    public int y(List<em> list) {
        qs cx = qp.cx(13);
        HashMap hashMap = new HashMap(3);
        hashMap.put("phonetype", this.KD.hT());
        hashMap.put("userinfo", this.KD.hV());
        hashMap.put("vecTelReport", list);
        cx.Ky = hashMap;
        int a = this.KD.a(cx);
        return a == 0 ? 0 : a;
    }
}
