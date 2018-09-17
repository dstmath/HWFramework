package defpackage;

import android.content.Context;
import android.text.TextUtils;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

/* renamed from: ai */
public class ai {
    private static ai ba;
    private List aU;
    private List aV;
    private List aW;
    private List aX;
    private List aY;
    private List aZ;
    private Context context;

    static {
        /* JADX: method processing error */
/*
        Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: ai.<clinit>():void
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:113)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:256)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:281)
	at jadx.api.JavaClass.decompile(JavaClass.java:59)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:161)
Caused by: jadx.core.utils.exceptions.DecodeException:  in method: ai.<clinit>():void
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
        throw new UnsupportedOperationException("Method not decompiled: ai.<clinit>():void");
    }

    private ai(Context context) {
        this.context = null;
        this.aU = new LinkedList();
        this.aV = new LinkedList();
        this.aW = new LinkedList();
        this.aX = new LinkedList();
        this.aY = new LinkedList();
        this.aZ = new LinkedList();
        this.context = context;
        load();
        if (this.aU.size() == 0 && this.aV.size() == 0 && this.aW.size() == 0 && this.aX.size() == 0 && this.aY.size() == 0 && this.aZ.size() == 0) {
            aw.d("PushLog2828", "Connect Control is not set, begin to config it");
            by();
        }
    }

    public static synchronized boolean a(Context context, int i) {
        boolean z;
        synchronized (ai.class) {
            ba = ai.q(context);
            if (ba == null) {
                aw.e("PushLog2828", "cannot get ConnectControlMgr instance, may be system err!!");
                z = false;
            } else {
                z = ba.b(i);
            }
        }
        return z;
    }

    private boolean a(bt btVar, List list, String str) {
        int i = 0;
        String str2 = "\\|";
        list.clear();
        String string = btVar.getString(str);
        if (TextUtils.isEmpty(string)) {
            aw.d("PushLog2828", str + " is not set");
        } else {
            aw.d("PushLog2828", str + "=" + string);
            String[] split = string.split(str2);
            if (split == null || split.length == 0) {
                aw.e("PushLog2828", str + " len 0, maybe system err");
                return false;
            }
            int length = split.length;
            while (i < length) {
                String str3 = split[i];
                al alVar = new al();
                if (alVar.i(str3)) {
                    list.add(alVar);
                }
                i++;
            }
        }
        return true;
    }

    private boolean a(List list) {
        if (a(list, 1)) {
            b(list, 1);
            save();
            return true;
        }
        aw.i("PushLog2828", "volumeControl not allow to pass!!");
        return false;
    }

    private boolean a(List list, long j) {
        if (list == null || list.size() == 0) {
            aw.d("PushLog2828", "there is no volome control");
            return true;
        }
        for (am amVar : list) {
            if (amVar.k(j)) {
                aw.d("PushLog2828", " pass:" + amVar);
            } else {
                aw.i("PushLog2828", " not pass:" + amVar);
                return false;
            }
        }
        return true;
    }

    private boolean a(List list, List list2) {
        if (list == null && list2 == null) {
            return true;
        }
        if (list == null || list2 == null || list.size() != list2.size()) {
            return false;
        }
        for (am amVar : list) {
            Object obj;
            for (am a : list2) {
                if (amVar.a(a)) {
                    obj = 1;
                    break;
                    continue;
                }
            }
            obj = null;
            continue;
            if (obj == null) {
                return false;
            }
        }
        return true;
    }

    private boolean b(int i) {
        return 1 == au.G(this.context) ? a(this.aZ) : a(this.aW);
    }

    private boolean b(bt btVar, List list, String str) {
        String str2 = "|";
        StringBuffer stringBuffer = new StringBuffer();
        for (am bG : list) {
            stringBuffer.append(bG.bG()).append(str2);
        }
        if (btVar.f(str, stringBuffer.toString())) {
            return true;
        }
        aw.e("PushLog2828", "save " + str + " failed!!");
        return false;
    }

    private synchronized boolean b(List list, long j) {
        boolean z;
        if (list != null) {
            if (list.size() != 0) {
                for (am amVar : list) {
                    if (!amVar.l(j)) {
                        aw.i("PushLog2828", " control info:" + amVar);
                        z = false;
                        break;
                    }
                }
                z = true;
            }
        }
        z = true;
        return z;
    }

    private boolean b(List list, List list2) {
        if (0 == ag.a(this.context, "lastQueryTRSsucc_time", 0)) {
            if (a(list, 1)) {
                b(list, 1);
            } else {
                aw.e("PushLog2828", "trsFirstFlowControl not allowed to pass!!");
                return false;
            }
        } else if (a(list2, 1)) {
            b(list2, 1);
        } else {
            aw.e("PushLog2828", "trsFlowControl not allowed to pass!!");
            return false;
        }
        save();
        return true;
    }

    private boolean bx() {
        List linkedList = new LinkedList();
        linkedList.add(new al(86400000, ae.l(this.context).T()));
        linkedList.add(new al(3600000, ae.l(this.context).U()));
        if (a(linkedList, this.aU)) {
            linkedList = new LinkedList();
            linkedList.add(new al(86400000, ae.l(this.context).V()));
            if (a(linkedList, this.aV)) {
                List linkedList2 = new LinkedList();
                for (Entry entry : ae.l(this.context).W().entrySet()) {
                    linkedList2.add(new al(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
                }
                if (a(linkedList2, this.aW)) {
                    linkedList = new LinkedList();
                    linkedList.add(new al(86400000, ae.l(this.context).X()));
                    linkedList.add(new al(3600000, ae.l(this.context).Y()));
                    if (a(linkedList, this.aX)) {
                        linkedList = new LinkedList();
                        linkedList.add(new al(86400000, ae.l(this.context).Z()));
                        if (a(linkedList, this.aY)) {
                            linkedList2 = new LinkedList();
                            for (Entry entry2 : ae.l(this.context).ae().entrySet()) {
                                linkedList2.add(new al(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
                            }
                            if (a(linkedList2, this.aZ)) {
                                aw.d("PushLog2828", "cur control is equal trs cfg");
                                return true;
                            }
                            aw.d("PushLog2828", "wifiVolumeControl cfg is change!!");
                            return false;
                        }
                        aw.d("PushLog2828", "wifiTrsFlowControl cfg is change!!");
                        return false;
                    }
                    aw.d("PushLog2828", "wifiTrsFirstFlowControl cfg is change!");
                    return false;
                }
                aw.d("PushLog2828", "flowcControl cfg is change!!");
                return false;
            }
            aw.d("PushLog2828", "trsFlowControl cfg is change!!");
            return false;
        }
        aw.d("PushLog2828", "trsFirstFlowControl cfg is change!");
        return false;
    }

    private boolean by() {
        this.aU.clear();
        this.aU.add(new al(86400000, ae.l(this.context).T()));
        this.aU.add(new al(3600000, ae.l(this.context).U()));
        this.aV.clear();
        this.aV.add(new al(86400000, ae.l(this.context).V()));
        this.aW.clear();
        for (Entry entry : ae.l(this.context).W().entrySet()) {
            this.aW.add(new al(((Long) entry.getKey()).longValue() * 1000, ((Long) entry.getValue()).longValue()));
        }
        this.aX.clear();
        this.aX.add(new al(86400000, ae.l(this.context).X()));
        this.aX.add(new al(3600000, ae.l(this.context).Y()));
        this.aY.clear();
        this.aY.add(new al(86400000, ae.l(this.context).Z()));
        this.aZ.clear();
        for (Entry entry2 : ae.l(this.context).ae().entrySet()) {
            this.aZ.add(new al(((Long) entry2.getKey()).longValue() * 1000, ((Long) entry2.getValue()).longValue()));
        }
        save();
        return true;
    }

    private boolean bz() {
        return 1 == au.G(this.context) ? b(this.aX, this.aY) : b(this.aU, this.aV);
    }

    private boolean load() {
        try {
            bt btVar = new bt(this.context, "PushConnectControl");
            a(btVar, this.aU, "trsFirstFlowControlData");
            a(btVar, this.aV, "trsFlowControlData");
            a(btVar, this.aW, "volumeControlData");
            a(btVar, this.aX, "wifiTrsFirstFlowControlData");
            a(btVar, this.aY, "wifiTrsFlowControlData");
            a(btVar, this.aZ, "wifiVolumeControlData");
            return true;
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
            return false;
        }
    }

    public static synchronized boolean p(Context context) {
        boolean z;
        synchronized (ai.class) {
            ba = ai.q(context);
            if (ba == null) {
                aw.e("PushLog2828", "cannot get ConnectControlMgr instance, may be system err!!");
                z = false;
            } else {
                z = ba.bz();
            }
        }
        return z;
    }

    public static synchronized ai q(Context context) {
        ai aiVar;
        synchronized (ai.class) {
            if (ba == null) {
                ba = new ai(context);
            }
            aiVar = ba;
        }
        return aiVar;
    }

    public static void r(Context context) {
        ba = ai.q(context);
        if (ba != null && !ba.bx()) {
            aw.d("PushLog2828", "TRS cfg change, need reload");
            ba.by();
        }
    }

    private boolean save() {
        try {
            bt btVar = new bt(this.context, "PushConnectControl");
            return b(btVar, this.aX, "wifiTrsFirstFlowControlData") && b(btVar, this.aY, "wifiTrsFlowControlData") && b(btVar, this.aZ, "wifiVolumeControlData") && b(btVar, this.aU, "trsFirstFlowControlData") && b(btVar, this.aV, "trsFlowControlData") && b(btVar, this.aW, "volumeControlData");
        } catch (Throwable e) {
            aw.d("PushLog2828", e.toString(), e);
            return false;
        }
    }

    public void bA() {
        this.aU.clear();
        this.aV.clear();
        this.aW.clear();
        this.aX.clear();
        this.aY.clear();
        this.aZ.clear();
    }
}
